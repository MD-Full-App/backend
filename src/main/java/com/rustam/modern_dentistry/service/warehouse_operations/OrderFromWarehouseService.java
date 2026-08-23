package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.*;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.OrderFromWarehouseRepository;
import com.rustam.modern_dentistry.dto.request.create.OrderFromWarehouseCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.OrderFromWarehouseSearchRequest;
import com.rustam.modern_dentistry.dto.request.read.RoomStockRequest;
import com.rustam.modern_dentistry.dto.request.update.OrderFromWarehouseProductUpdateRequest;
import com.rustam.modern_dentistry.dto.request.update.OrderFromWarehouseUpdateRequest;
import com.rustam.modern_dentistry.dto.response.OrderProductStockProjection;
import com.rustam.modern_dentistry.dto.response.read.*;
import com.rustam.modern_dentistry.exception.custom.ProductDoesnotQuantityThatMuchException;
import com.rustam.modern_dentistry.mapper.warehouse_operations.OrderFromWarehouseMapper;
import com.rustam.modern_dentistry.service.settings.CabinetService;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.warehouse_operations.OrderFromWarehouseSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class OrderFromWarehouseService {

    OrderFromWarehouseRepository orderFromWarehouseRepository;
    UtilService utilService;
    WarehouseEntryProductService warehouseEntryProductService;
    OrderFromWarehouseMapper orderFromWarehouseMapper;
    WarehouseRemovalService warehouseRemovalService;
    CabinetService cabinetService;

    public OrderFromWarehouse findById(Long id) {
        return utilService.findByOrderFromWarehouseId(id);
    }

    @Transactional
    public OrderFromWarehouseResponse create(OrderFromWarehouseCreateRequest request) {
        String personWhoPlacedOrder = request.getPersonWhoPlacedOrder() != null && !request.getPersonWhoPlacedOrder().isBlank()
                ? request.getPersonWhoPlacedOrder()
                : utilService.getCurrentUserId();
        OrderFromWarehouse orderFromWarehouse = OrderFromWarehouse.builder()
                .date(request.getDate())
                .time(request.getTime())
                .description(request.getDescription())
                .cabinet(cabinetService.findByCabinetName(request.getCabinetName()))
                .personWhoPlacedOrder(personWhoPlacedOrder)
                .build();

        List<OrderFromWarehouseProduct> orderFromWarehouseProducts = buildOrderFromWarehouseProducts(request,orderFromWarehouse);
        orderFromWarehouse.setNumber(orderFromWarehouseProducts.size());
        orderFromWarehouse.setOrderFromWarehouseProducts(orderFromWarehouseProducts);
        orderFromWarehouse.setSumQuantity(calculateSumQuantity(orderFromWarehouseProducts));
        orderFromWarehouseRepository.save(orderFromWarehouse);
        buildWarehouseExit(orderFromWarehouse);
        return buildWarehouseEntryResponse(orderFromWarehouse, orderFromWarehouseProducts);
    }

    private OrderFromWarehouseResponse buildWarehouseEntryResponse(OrderFromWarehouse orderFromWarehouse, List<OrderFromWarehouseProduct> orderFromWarehouseProducts) {
        List<OrderFromWarehouseProductResponse> productResponses = orderFromWarehouseProducts.stream()
                .<OrderFromWarehouseProductResponse>map(p -> {
                    BigDecimal price = p.getPrice();
                    if (price == null) {
                        price = warehouseEntryProductService.findPriceForProduct(
                                p.getWarehouseEntryProductId(),
                                p.getWarehouseEntryId(),
                                p.getProductId(),
                                p.getProductName()
                        );
                    }
                    return OrderFromWarehouseProductResponse.builder()
                            .id(p.getId())
                            .categoryId(p.getCategoryId())
                            .productId(p.getProductId())
                            .warehouseEntryId(p.getWarehouseEntryId())
                            .warehouseEntryProductId(p.getWarehouseEntryProductId())
                            .warehouseEntryProductName(p.getProductName() != null ? p.getProductName() : p.getProductTitle())
                            .categoryName(p.getCategoryName())
                            .productName(p.getProductName())
                            .productTitle(p.getProductTitle())
                            .quantity(p.getQuantity())
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderFromWarehouseResponse.builder()
                .id(orderFromWarehouse.getId())
                .date(orderFromWarehouse.getDate())
                .time(orderFromWarehouse.getTime())
                .description(orderFromWarehouse.getDescription())
                .orderFromWarehouseProductResponses(productResponses)
                .cabinetName(orderFromWarehouse.getCabinet() != null ? orderFromWarehouse.getCabinet().getCabinetName() : null)
                .personWhoPlacedOrder(orderFromWarehouse.getPersonWhoPlacedOrder())
                .number(orderFromWarehouse.getNumber())
                .quantity(orderFromWarehouse.getSumQuantity())
                .build();
    }

    private Long calculateSumQuantity(List<OrderFromWarehouseProduct> products) {
        return products.stream()
                .mapToLong(OrderFromWarehouseProduct::getQuantity)
                .sum();
    }

    private List<OrderFromWarehouseProduct> buildOrderFromWarehouseProducts(OrderFromWarehouseCreateRequest request, OrderFromWarehouse orderFromWarehouse) {
        return request.getOrderFromWarehouseProductRequests().stream()
                .map(dto -> {
                    WarehouseEntryProduct warehouseEntryProduct = null;
                    try {
                        warehouseEntryProduct = warehouseEntryProductService.findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                                dto.getWarehouseEntryProductId(), dto.getWarehouseEntryId(), dto.getCategoryId(), dto.getProductId());
                        if (warehouseEntryProduct != null && warehouseEntryProduct.getId() != null) {
                            warehouseEntryProductService.decreaseProductQuantity(warehouseEntryProduct.getId(), dto.getQuantity());
                        }
                    } catch (Exception e) {
                        log.warn("WarehouseEntryProduct lookup failed for create item: {}", e.getMessage());
                    }

                    BigDecimal price = dto.getPrice();
                    if (price == null && warehouseEntryProduct != null) {
                        price = warehouseEntryProduct.getPrice();
                    }
                    if (price == null) {
                        price = warehouseEntryProductService.findPriceForProduct(
                                dto.getWarehouseEntryProductId(),
                                dto.getWarehouseEntryId(),
                                dto.getProductId(),
                                warehouseEntryProduct != null ? warehouseEntryProduct.getProductName() : null
                        );
                    }

                    return OrderFromWarehouseProduct.builder()
                            .categoryId(dto.getCategoryId())
                            .productId(dto.getProductId())
                            .quantity(dto.getQuantity())
                            .price(price)
                            .productName(warehouseEntryProduct != null ? warehouseEntryProduct.getProductName() : "Product " + dto.getProductId())
                            .categoryName(warehouseEntryProduct != null ? warehouseEntryProduct.getCategoryName() : "Category " + dto.getCategoryId())
                            .initialQuantity(dto.getQuantity())
                            .productTitle(warehouseEntryProduct != null ? warehouseEntryProduct.getProductTitle() : null)
                            .warehouseEntryId(dto.getWarehouseEntryId())
                            .warehouseEntryProductId(dto.getWarehouseEntryProductId())
                            .orderFromWarehouse(orderFromWarehouse)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void buildWarehouseExit(OrderFromWarehouse orderFromWarehouse) {
        WarehouseRemoval warehouseRemoval = WarehouseRemoval.builder()
                .date(orderFromWarehouse.getDate())
                .time(orderFromWarehouse.getTime())
                .cabinet(orderFromWarehouse.getCabinet())
                .personWhoPlacedOrder(orderFromWarehouse.getPersonWhoPlacedOrder())
                .orderFromWarehouse(orderFromWarehouse)
                .number(orderFromWarehouse.getNumber())
                .orderAmount(orderFromWarehouse.getSumQuantity())
                .remainingAmount(orderFromWarehouse.getSumQuantity())
                .sendAmount(0L)
                .build();
        warehouseRemovalService.save(warehouseRemoval);
    }

    public List<OrderFromWarehouseReadResponse> read() {
        List<OrderFromWarehouse> entries = orderFromWarehouseRepository.findAllWithCabinets();
        return toDtos(entries);
    }

    public List<OrderFromWarehouseReadResponse> toDtos(List<OrderFromWarehouse> entries) {
        return entries.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public OrderFromWarehouseReadResponse toDto(OrderFromWarehouse entry) {
        com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus status = com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus.WAITING;
        Long sendAmount = 0L;
        if (entry.getWarehouseRemoval() != null && entry.getWarehouseRemoval().getSendAmount() != null) {
            sendAmount = entry.getWarehouseRemoval().getSendAmount();
        }

        return OrderFromWarehouseReadResponse.builder()
                .id(entry.getId())
                .date(entry.getDate())
                .time(entry.getTime())
                .cabinetName(entry.getCabinet() != null ? entry.getCabinet().getCabinetName() : null)
                .description(entry.getDescription())
                .personWhoPlacedOrder(entry.getPersonWhoPlacedOrder())
                .number(entry.getNumber())
                .sumQuantity(entry.getSumQuantity())
                .sendAmount(sendAmount)
                .incomingQuantity(sendAmount)
                .pendingStatus(status)
                .build();
    }



    @Transactional
    public OrderFromWarehouseResponse info(Long id) {
        OrderFromWarehouse orderFromWarehouse = findById(id);
        return buildWarehouseEntryResponse(orderFromWarehouse, orderFromWarehouse.getOrderFromWarehouseProducts());
    }


//    public OrderFromWarehouse findByOrderFromWarehouseProductId(Long id){
//        return orderFromWarehouseRepository.findByIdWithProducts(id)
//                .orElseThrow(() -> new RuntimeException("OrderFromWarehouse not found"));
//    }

    public List<OrderFromWarehouseReadResponse> search(OrderFromWarehouseSearchRequest orderFromWarehouseSearchRequest) {
        List<OrderFromWarehouse> orderFromWarehouses = orderFromWarehouseRepository.findAll(OrderFromWarehouseSpecification.filterBy(orderFromWarehouseSearchRequest));
        return orderFromWarehouseMapper.toDtos(orderFromWarehouses);
    }

    @Transactional
    public OrderFromWarehouseResponse update(OrderFromWarehouseUpdateRequest orderFromWarehouseUpdateRequest) {
        OrderFromWarehouse orderFromWarehouse = findById(orderFromWarehouseUpdateRequest.getId());
        utilService.updateFieldIfPresent(orderFromWarehouseUpdateRequest.getDate(), orderFromWarehouse::setDate);
        utilService.updateFieldIfPresent(orderFromWarehouseUpdateRequest.getTime(), orderFromWarehouse::setTime);
        utilService.updateFieldIfPresent(orderFromWarehouseUpdateRequest.getDescription(), orderFromWarehouse::setDescription);
        utilService.updateFieldIfPresent(orderFromWarehouseUpdateRequest.getPersonWhoPlacedOrder(), orderFromWarehouse::setPersonWhoPlacedOrder);
        utilService.updateFieldIfPresent(cabinetService.findByCabinetName(orderFromWarehouseUpdateRequest.getCabinetName()), orderFromWarehouse::setCabinet);
        if (hasProducts(orderFromWarehouseUpdateRequest)){
            List<OrderFromWarehouseProduct> entryProducts = buildWarehouseEntryUpdateProducts(orderFromWarehouseUpdateRequest, orderFromWarehouse);
            orderFromWarehouse.getOrderFromWarehouseProducts().clear();
            orderFromWarehouse.getOrderFromWarehouseProducts().addAll(entryProducts);
            orderFromWarehouse.setNumber(entryProducts.size());
            orderFromWarehouse.setSumQuantity(calculateSumQuantity(entryProducts));
        }
        WarehouseRemoval warehouseRemoval = warehouseRemovalService.findById(orderFromWarehouse.getWarehouseRemoval().getId());
        buildWarehouseUpdateExit(warehouseRemoval,orderFromWarehouse);
        orderFromWarehouseRepository.save(orderFromWarehouse);
        return buildWarehouseEntryResponse(orderFromWarehouse, orderFromWarehouse.getOrderFromWarehouseProducts());
    }

    private void buildWarehouseUpdateExit(WarehouseRemoval warehouseRemoval, OrderFromWarehouse orderFromWarehouse) {
        warehouseRemoval.setOrderAmount(orderFromWarehouse.getSumQuantity());
        warehouseRemoval.setRemainingAmount(orderFromWarehouse.getSumQuantity());
        warehouseRemoval.setDate(orderFromWarehouse.getDate());
        warehouseRemoval.setTime(orderFromWarehouse.getTime());
        warehouseRemoval.setCabinet(orderFromWarehouse.getCabinet());
        warehouseRemoval.setSendAmount(0L);
        warehouseRemoval.setOrderFromWarehouse(orderFromWarehouse);
        warehouseRemoval.setNumber(orderFromWarehouse.getOrderFromWarehouseProducts().size());
        warehouseRemovalService.save(warehouseRemoval);
    }

    private List<OrderFromWarehouseProduct> buildWarehouseEntryUpdateProducts(OrderFromWarehouseUpdateRequest orderFromWarehouseUpdateRequest, OrderFromWarehouse orderFromWarehouse) {
        Map<Long, OrderFromWarehouseProduct> existingProductsMap = orderFromWarehouse.getOrderFromWarehouseProducts()
                .stream()
                .collect(Collectors.toMap(OrderFromWarehouseProduct::getId, Function.identity()));

        List<OrderFromWarehouseProduct> finalList = new ArrayList<>();

        Set<Long> updatedIds = new HashSet<>();

        for (OrderFromWarehouseProductUpdateRequest dto : orderFromWarehouseUpdateRequest.getOrderFromWarehouseProductUpdateRequests()) {
            WarehouseEntryProduct warehouseEntryProduct = null;
            try {
                warehouseEntryProduct = warehouseEntryProductService.findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                        dto.getWarehouseEntryProductId(), dto.getWarehouseEntryId(), dto.getCategoryId(), dto.getProductId());
            } catch (Exception e) {
                log.warn("WarehouseEntryProduct lookup failed during update: {}", e.getMessage());
            }

            if (dto.getOrderFromWarehouseProductId() != null && existingProductsMap.containsKey(dto.getOrderFromWarehouseProductId())) {
                OrderFromWarehouseProduct existing = existingProductsMap.get(dto.getOrderFromWarehouseProductId());

                if (warehouseEntryProduct != null && dto.getWarehouseEntryProductId() != null) {
                    try {
                        Long wepIdToIncrease = existing.getWarehouseEntryProductId() != null ? existing.getWarehouseEntryProductId() : dto.getWarehouseEntryProductId();
                        warehouseEntryProductService.increaseProductQuantity(wepIdToIncrease, existing.getQuantity());
                        warehouseEntryProductService.decreaseProductQuantity(dto.getWarehouseEntryProductId(), dto.getQuantity());
                    } catch (Exception e) {
                        log.warn("Could not update quantities for warehouseEntryProductId: {}", e.getMessage());
                    }
                }

                utilService.updateFieldIfPresent(dto.getCategoryId(), existing::setCategoryId);
                utilService.updateFieldIfPresent(dto.getProductId(), existing::setProductId);
                utilService.updateFieldIfPresent(dto.getQuantity(), existing::setQuantity);
                utilService.updateFieldIfPresent(dto.getWarehouseEntryProductId(), existing::setWarehouseEntryProductId);
                utilService.updateFieldIfPresent(dto.getWarehouseEntryId(), existing::setWarehouseEntryId);
                utilService.updateFieldIfPresent(dto.getQuantity(), existing::setInitialQuantity);

                BigDecimal price = dto.getPrice();
                if (price == null && warehouseEntryProduct != null) {
                    price = warehouseEntryProduct.getPrice();
                }
                if (price == null) {
                    price = existing.getPrice();
                }
                if (price == null) {
                    price = warehouseEntryProductService.findPriceForProduct(
                            dto.getWarehouseEntryProductId(),
                            dto.getWarehouseEntryId(),
                            dto.getProductId(),
                            existing.getProductName()
                    );
                }
                existing.setPrice(price);

                if (warehouseEntryProduct != null) {
                    existing.setProductName(warehouseEntryProduct.getProductName());
                    existing.setCategoryName(warehouseEntryProduct.getCategoryName());
                    existing.setProductTitle(warehouseEntryProduct.getProductTitle());
                }

                finalList.add(existing);
                updatedIds.add(dto.getOrderFromWarehouseProductId());

            } else {
                if (dto.getOrderFromWarehouseProductId() != null && !existingProductsMap.containsKey(dto.getOrderFromWarehouseProductId())) {
                    throw new ProductDoesnotQuantityThatMuchException("OrderFromWarehouseProduct with id " + dto.getOrderFromWarehouseProductId() + " not found in this OrderFromWarehouse!");
                }

                if (warehouseEntryProduct != null && warehouseEntryProduct.getId() != null) {
                    try {
                        warehouseEntryProductService.decreaseProductQuantity(warehouseEntryProduct.getId(), dto.getQuantity());
                    } catch (Exception e) {
                        log.warn("Could not decrease product quantity: {}", e.getMessage());
                    }
                }

                BigDecimal price = dto.getPrice();
                if (price == null && warehouseEntryProduct != null) {
                    price = warehouseEntryProduct.getPrice();
                }
                if (price == null) {
                    price = warehouseEntryProductService.findPriceForProduct(
                            dto.getWarehouseEntryProductId(),
                            dto.getWarehouseEntryId(),
                            dto.getProductId(),
                            warehouseEntryProduct != null ? warehouseEntryProduct.getProductName() : null
                    );
                }

                OrderFromWarehouseProduct newProduct = OrderFromWarehouseProduct.builder()
                        .categoryId(dto.getCategoryId())
                        .productId(dto.getProductId())
                        .quantity(dto.getQuantity())
                        .price(price)
                        .productName(warehouseEntryProduct != null ? warehouseEntryProduct.getProductName() : "Product " + dto.getProductId())
                        .categoryName(warehouseEntryProduct != null ? warehouseEntryProduct.getCategoryName() : "Category " + dto.getCategoryId())
                        .initialQuantity(dto.getQuantity())
                        .productTitle(warehouseEntryProduct != null ? warehouseEntryProduct.getProductTitle() : null)
                        .orderFromWarehouse(orderFromWarehouse)
                        .warehouseEntryId(dto.getWarehouseEntryId())
                        .warehouseEntryProductId(dto.getWarehouseEntryProductId())
                        .build();

                finalList.add(newProduct);
            }

        }

        for (OrderFromWarehouseProduct old : existingProductsMap.values()) {
            if (!updatedIds.contains(old.getId())) {
                if (old.getWarehouseEntryProductId() != null) {
                    try {
                        warehouseEntryProductService.increaseProductQuantity(old.getWarehouseEntryProductId(), old.getQuantity());
                    } catch (Exception e) {
                        log.warn("Could not restore quantity for deleted order product: {}", e.getMessage());
                    }
                }
            }
        }

        return finalList;
    }

    private boolean hasProducts(OrderFromWarehouseUpdateRequest request) {
        return request.getOrderFromWarehouseProductUpdateRequests() != null &&
                !request.getOrderFromWarehouseProductUpdateRequests().isEmpty();
    }

    @Transactional
    public void delete(Long id) {
        OrderFromWarehouse orderFromWarehouse = findById(id);

        List<OrderFromWarehouseProduct> products = orderFromWarehouse.getOrderFromWarehouseProducts();
        for (OrderFromWarehouseProduct product : products) {
            WarehouseEntryProduct warehouseEntryProduct = warehouseEntryProductService.findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                    product.getWarehouseEntryProductId(),
                    product.getWarehouseEntryId(),
                    product.getCategoryId(),
                    product.getProductId()
            );

            warehouseEntryProductService.increaseProductQuantity(warehouseEntryProduct.getId(), product.getQuantity());
        }
        orderFromWarehouseRepository.delete(orderFromWarehouse);
    }


    @Transactional
    public void deleteOrderFromWarehouseProduct(Long id, Long productId) {
        OrderFromWarehouse orderFromWarehouse = findById(id);

        orderFromWarehouse.getOrderFromWarehouseProducts().removeIf(p -> p.getId().equals(productId));

        orderFromWarehouse.setNumber(orderFromWarehouse.getOrderFromWarehouseProducts().size());

        orderFromWarehouseRepository.save(orderFromWarehouse);
    }

    @Transactional
    public List<RoomStockResponse> search(RoomStockRequest request){
        List<OrderProductStockProjection> products = orderFromWarehouseRepository.searchOrderRoomStockProducts(
                request.getCabinetName(),request.getCategoryName(),
                request.getProductName(),request.getProductNo()
        );

        return products.stream()
                .map(p -> new RoomStockResponse(
                        p.getCategoryName(),
                        p.getProductName(),
                        p.getProductCode(),
                        p.getEntryQuantity(),
                        p.getUsedQuantity(),
                        p.getRemainingQuantity()
                ))
                .toList();
    }

    public List<OrderFromWarehouse> findByProductId(Long productId) {
        return orderFromWarehouseRepository.findAllById(Collections.singleton(productId));
    }

    public void save(OrderFromWarehouse orderFromWarehouse) {
        orderFromWarehouseRepository.save(orderFromWarehouse);
    }
}