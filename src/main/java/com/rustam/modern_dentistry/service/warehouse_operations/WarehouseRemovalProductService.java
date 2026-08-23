package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.*;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.WarehouseRemovalProductRepository;
import com.rustam.modern_dentistry.dto.OutOfTheWarehouseDto;
import com.rustam.modern_dentistry.dto.record.ProcessedWarehouseRemoval;
import com.rustam.modern_dentistry.dto.request.create.WarehouseRemovalCreateRequest;
import com.rustam.modern_dentistry.dto.request.create.WarehouseRemovalProductCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.WarehouseRemovalProductSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.WarehouseRemovalProductRequest;
import com.rustam.modern_dentistry.dto.request.update.WarehouseRemovalProductUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.WarehouseRemovalCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.WarehouseRemovalProductReadResponse;
import com.rustam.modern_dentistry.exception.custom.AmountSendException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.warehouse_operations.WarehouseRemovalProductSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseRemovalProductService {

    WarehouseRemovalProductRepository warehouseRemovalProductRepository;
    WarehouseRemovalService warehouseRemovalService;
    UtilService utilService;
    OrderFromWarehouseProductService orderFromWarehouseProductService;
    WarehouseReceiptsService warehouseReceiptsService;

    @Transactional
    public WarehouseRemovalCreateResponse create(WarehouseRemovalCreateRequest request) {
        WarehouseRemoval warehouseRemoval = warehouseRemovalService.findById(request.getWarehouseRemovalId());

        ProcessedWarehouseRemoval processed = processWarehouseRemovalRequests(request, warehouseRemoval);

        return WarehouseRemovalCreateResponse.builder()
                .groupId(processed.groupId())
                .date(request.getDate())
                .time(request.getTime())
                .description(request.getDescription())
                .outOfTheWarehouseDtos(processed.dtos())
                .number(processed.dtos().size())
                .status(PendingStatus.WAITING)
                .build();
    }

    private ProcessedWarehouseRemoval processWarehouseRemovalRequests(WarehouseRemovalCreateRequest request, WarehouseRemoval warehouseRemoval) {
        List<OutOfTheWarehouseDto> dtos = new ArrayList<>();
        String groupId = utilService.generateGroupId();
        Integer number = request.getRequests().size();

        for (WarehouseRemovalProductCreateRequest requestDetail : request.getRequests()) {
            var matchedProduct = findAndValidateProduct(warehouseRemoval, requestDetail);
            warehouseProductQuantity(matchedProduct, requestDetail);

            WarehouseRemovalProduct warehouseRemovalProduct = addWarehouseRemovalProductEntity(
                    warehouseRemoval, matchedProduct, requestDetail, request, groupId, number
            );

            dtos.add(prepareOutOfTheWarehouseDto(warehouseRemovalProduct));
        }

        return new ProcessedWarehouseRemoval(groupId, dtos);
    }

    private OrderFromWarehouseProduct findAndValidateProduct(WarehouseRemoval warehouseRemoval, WarehouseRemovalProductCreateRequest requestDetail) {
        OrderFromWarehouse order = utilService.findByOrderFromWarehouseId(warehouseRemoval.getOrderFromWarehouse().getId());

        return order.getOrderFromWarehouseProducts().stream()
                .filter(product -> product.getId().equals(requestDetail.getOrderFromWarehouseProductId()))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Product with ID: " + requestDetail.getOrderFromWarehouseProductId() + " not found."));
    }

    private void warehouseProductQuantity(OrderFromWarehouseProduct matchedProduct, WarehouseRemovalProductCreateRequest requestDetail) {
        if (matchedProduct.getQuantity() < requestDetail.getCurrentExpenses()) {
            throw new AmountSendException("The amount sent is too much for product ID: " + requestDetail.getOrderFromWarehouseProductId());
        }

        long remainingQuantity = matchedProduct.getQuantity() - requestDetail.getCurrentExpenses();
        matchedProduct.setQuantity(remainingQuantity);
    }

    private OutOfTheWarehouseDto prepareOutOfTheWarehouseDto(WarehouseRemovalProduct warehouseRemovalProduct) {
        return OutOfTheWarehouseDto.builder()
                .id(warehouseRemovalProduct.getId())
                .orderFromWarehouseProductId(warehouseRemovalProduct.getOrderFromWarehouseProductId())
                .categoryName(warehouseRemovalProduct.getCategoryName())
                .productName(warehouseRemovalProduct.getProductName())
                .productDescription(warehouseRemovalProduct.getProductDescription())
                .orderQuantity(warehouseRemovalProduct.getOrderAmount())
                .remainingQuantity(warehouseRemovalProduct.getRemainingAmount())
                .sendQuantity(warehouseRemovalProduct.getSendAmount())
                .currentAmount(warehouseRemovalProduct.getCurrentAmount())
                .build();
    }

    private WarehouseRemovalProduct addWarehouseRemovalProductEntity(
            WarehouseRemoval warehouseRemoval,
            OrderFromWarehouseProduct matchedProduct,
            WarehouseRemovalProductCreateRequest requestDetail,
            WarehouseRemovalCreateRequest request,
            String groupId,
            Integer number
    ) {
        long currentSendAmount = calculateTotalSendAmount(warehouseRemoval.getId(), requestDetail);
        long remainingAmount = matchedProduct.getInitialQuantity() - currentSendAmount;

        Optional<WarehouseReceipts> optionalReceipts =
                warehouseReceiptsService.findByGroupId(groupId);

        WarehouseReceipts warehouseReceipts = optionalReceipts.orElseGet(() -> {
            WarehouseReceipts newReceipts = createAndSaveWarehouseReceipts(
                    warehouseRemoval, groupId, request
            );
            return warehouseReceiptsService.save(newReceipts);
        });

        WarehouseRemovalProduct warehouseRemovalProduct = buildWarehouseRemovalProduct(
                warehouseRemoval,
                matchedProduct,
                requestDetail,
                request,
                currentSendAmount,
                remainingAmount,
                groupId,
                number
        );

        warehouseRemovalProduct.setWarehouseReceipts(warehouseReceipts);

        warehouseRemovalProductRepository.save(warehouseRemovalProduct);

        updateWarehouseRemoval(warehouseRemoval, requestDetail.getCurrentExpenses());

        return warehouseRemovalProduct;
    }


    private WarehouseReceipts createAndSaveWarehouseReceipts(
            WarehouseRemoval warehouseRemoval,
            String groupId, WarehouseRemovalCreateRequest request) {

        long totalSendQuantity = request.getRequests().stream()
                .mapToLong(WarehouseRemovalProductCreateRequest::getCurrentExpenses).sum();

        return WarehouseReceipts.builder()
                .date(warehouseRemoval.getDate())
                .time(warehouseRemoval.getTime())
                .cabinet(warehouseRemoval.getCabinet())
                .personWhoPlacedOrder(warehouseRemoval.getPersonWhoPlacedOrder())
                .orderQuantity(warehouseRemoval.getOrderAmount())
                .sendQuantity(totalSendQuantity)
                .pendingStatus(PendingStatus.WAITING)
                .groupId(groupId)
                .warehouseRemovalProducts(new ArrayList<>())
                .build();
    }


    private long calculateTotalSendAmount(Long removalId, WarehouseRemovalProductCreateRequest requestDetail) {
        long previous = warehouseRemovalProductRepository
                .findAllByWarehouseRemovalIdAndOrderFromWarehouseProductId(removalId, requestDetail.getOrderFromWarehouseProductId())
                .stream()
                .mapToLong(WarehouseRemovalProduct::getCurrentAmount)
                .sum();

        return previous + requestDetail.getCurrentExpenses();
    }

    private WarehouseRemovalProduct buildWarehouseRemovalProduct(
            WarehouseRemoval warehouseRemoval,
            OrderFromWarehouseProduct matchedProduct,
            WarehouseRemovalProductCreateRequest requestDetail,
            WarehouseRemovalCreateRequest request,
            long currentSendAmount,
            long remainingAmount, String groupId, Integer number) {

        return WarehouseRemovalProduct.builder()
                .categoryId(matchedProduct.getCategoryId())
                .productId(matchedProduct.getProductId())
                .currentAmount(requestDetail.getCurrentExpenses())
                .orderAmount(matchedProduct.getInitialQuantity())
                .remainingAmount(remainingAmount)
                .sendAmount(currentSendAmount)
                .groupId(groupId)
                .number(number)
                .productName(matchedProduct.getProductName())
                .categoryName(matchedProduct.getCategoryName())
                .productDescription(Optional.ofNullable(request.getDescription()).orElse(matchedProduct.getProductTitle()))
                .orderFromWarehouseProductId(requestDetail.getOrderFromWarehouseProductId())
                .date(Optional.ofNullable(request.getDate()).orElse(warehouseRemoval.getDate()))
                .time(Optional.ofNullable(request.getTime()).orElse(warehouseRemoval.getTime()))
                .pendingStatus(PendingStatus.WAITING)
                .warehouseRemoval(warehouseRemoval)
                .build();
    }

    private void updateWarehouseRemoval(WarehouseRemoval warehouseRemoval, long currentExpense) {
        long updatedSendAmount = warehouseRemoval.getSendAmount() + currentExpense;
        warehouseRemoval.setSendAmount(updatedSendAmount);

        long updatedRemainingAmount = warehouseRemoval.getOrderFromWarehouse().getSumQuantity() - updatedSendAmount;
        warehouseRemoval.setRemainingAmount(updatedRemainingAmount);
    }

    @Transactional
    public void deleteWarehouseRemovalIdBasedOnWarehouseRemovalProduct(String groupId) {
        List<WarehouseRemovalProduct> productsToDelete = new ArrayList<>();

        if (groupId != null && !groupId.trim().isEmpty()) {
            productsToDelete.addAll(findAllByGroupId(groupId));
        }

        if (productsToDelete.isEmpty() && groupId != null) {
            try {
                Long numericId = Long.parseLong(groupId.trim());
                warehouseRemovalProductRepository.findById(numericId).ifPresent(productsToDelete::add);
                if (productsToDelete.isEmpty()) {
                    productsToDelete.addAll(warehouseRemovalProductRepository.findAllByWarehouseRemovalId(numericId));
                }
            } catch (NumberFormatException ignored) {}
        }

        if (productsToDelete.isEmpty()) {
            throw new NotFoundException("No WarehouseRemovalProduct found with groupId or id: " + groupId);
        }

        for (WarehouseRemovalProduct product : productsToDelete) {
            if (!PendingStatus.WAITING.equals(product.getPendingStatus())) {
                throw new IllegalStateException("Cannot delete product with ID " + product.getId()
                        + " because it is already " + product.getPendingStatus());
            }

            restoreProductToWarehouseInventory(product);

            WarehouseRemoval warehouseRemoval = product.getWarehouseRemoval();

            long updatedSendAmount = warehouseRemoval.getSendAmount() - product.getCurrentAmount();
            warehouseRemoval.setSendAmount(updatedSendAmount);

            long updatedRemainingAmount = warehouseRemoval.getOrderFromWarehouse().getSumQuantity() - updatedSendAmount;
            warehouseRemoval.setRemainingAmount(updatedRemainingAmount);

            warehouseRemoval.getWarehouseRemovalProducts().remove(product);

            warehouseRemovalProductRepository.delete(product);
        }
    }

    private void restoreProductToWarehouseInventory(WarehouseRemovalProduct warehouseRemovalProduct) {
        OrderFromWarehouseProduct orderFromWarehouseProduct = orderFromWarehouseProductService.findById(warehouseRemovalProduct.getOrderFromWarehouseProductId());

        long restoredQuantity = orderFromWarehouseProduct.getQuantity() + warehouseRemovalProduct.getCurrentAmount();
        orderFromWarehouseProduct.setQuantity(restoredQuantity);

        orderFromWarehouseProductService.saveOrderFromWarehouseProduct(orderFromWarehouseProduct);
    }

    @Transactional
    public List<WarehouseRemovalProductReadResponse> read() {
        List<WarehouseRemovalProduct> warehouseRemovalProducts = warehouseRemovalProductRepository.findAll();

        return warehouseRemovalProducts.stream()
                .map(this::mapToReadResponse)
                .collect(Collectors.toList());
    }

    private WarehouseRemovalProductReadResponse mapToReadResponse(WarehouseRemovalProduct warehouseRemovalProduct) {
        Long quantity = warehouseRemovalProduct.getCurrentAmount() != null
                ? warehouseRemovalProduct.getCurrentAmount()
                : (warehouseRemovalProduct.getSendAmount() != null ? warehouseRemovalProduct.getSendAmount() : 0L);

        return WarehouseRemovalProductReadResponse.builder()
                .warehouseRemovalId(warehouseRemovalProduct.getWarehouseRemoval() != null ? warehouseRemovalProduct.getWarehouseRemoval().getId() : null)
                .date(warehouseRemovalProduct.getDate())
                .time(warehouseRemovalProduct.getTime())
                .id(warehouseRemovalProduct.getId())
                .categoryId(warehouseRemovalProduct.getCategoryId())
                .categoryName(warehouseRemovalProduct.getCategoryName())
                .productId(warehouseRemovalProduct.getProductId())
                .productName(warehouseRemovalProduct.getProductName())
                .idNumber(warehouseRemovalProduct.getProductId() != null ? warehouseRemovalProduct.getProductId().toString() : (warehouseRemovalProduct.getId() != null ? warehouseRemovalProduct.getId().toString() : "-"))
                .quantity(quantity)
                .pendingStatus(warehouseRemovalProduct.getPendingStatus())
                .number(warehouseRemovalProduct.getNumber())
                .groupId(warehouseRemovalProduct.getGroupId())
                .build();
    }

    @Transactional
    public List<WarehouseRemovalProductReadResponse> search(WarehouseRemovalProductSearchRequest warehouseRemovalProductSearchRequest) {
        List<WarehouseRemovalProduct> warehouseRemovalProducts = warehouseRemovalProductRepository.findAll(WarehouseRemovalProductSpecification.filterBy(warehouseRemovalProductSearchRequest));
        return warehouseRemovalProducts.stream()
                .map(this::mapToReadResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WarehouseRemovalCreateResponse info(String groupId) {
        List<WarehouseRemovalProduct> products = new ArrayList<>();
        if (groupId != null && !groupId.trim().isEmpty()) {
            products.addAll(findAllByGroupId(groupId));
        }

        if (products.isEmpty() && groupId != null) {
            try {
                Long numericId = Long.parseLong(groupId.trim());
                warehouseRemovalProductRepository.findById(numericId).ifPresent(products::add);
                if (products.isEmpty()) {
                    products.addAll(warehouseRemovalProductRepository.findAllByWarehouseRemovalId(numericId));
                }
            } catch (NumberFormatException ignored) {}
        }

        if (products.isEmpty()) {
            throw new NotFoundException("No WarehouseRemovalProducts found for groupId or id: " + groupId);
        }

        WarehouseRemovalProduct sampleProduct = products.get(0);

        List<OutOfTheWarehouseDto> outOfTheWarehouseDtos = products.stream()
                .map(product -> OutOfTheWarehouseDto.builder()
                        .id(product.getId())
                        .orderFromWarehouseProductId(product.getOrderFromWarehouseProductId())
                        .categoryName(product.getCategoryName())
                        .productName(product.getProductName())
                        .productDescription(product.getProductDescription())
                        .orderQuantity(product.getOrderAmount())
                        .remainingQuantity(product.getRemainingAmount())
                        .sendQuantity(product.getSendAmount())
                        .currentAmount(product.getCurrentAmount())
                        .build())
                .collect(Collectors.toList());

        return WarehouseRemovalCreateResponse.builder()
                .groupId(sampleProduct.getGroupId())
                .date(sampleProduct.getDate())
                .time(sampleProduct.getTime())
                .description(sampleProduct.getProductDescription())
                .number(sampleProduct.getNumber())
                .status(sampleProduct.getPendingStatus())
                .outOfTheWarehouseDtos(outOfTheWarehouseDtos)
                .build();
    }


    public WarehouseRemovalProduct findById(Long id) {
        return warehouseRemovalProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseRemovalProduct with ID " + id + " not found."));
    }

    public List<WarehouseRemovalProduct> findAllByGroupId(String groupId) {
        return warehouseRemovalProductRepository.findAllByGroupId(groupId);
    }

    public List<WarehouseRemovalProduct> findAllByIdAndGroupIdAndOrderFromWarehouseProductId(Long id, String groupId, Long orderFromWarehouseProductId) {
        return warehouseRemovalProductRepository.findAllByIdAndGroupIdAndOrderFromWarehouseProductId(id, groupId, orderFromWarehouseProductId);
    }

    @Transactional
    public WarehouseRemovalCreateResponse update(WarehouseRemovalProductUpdateRequest request) {
        List<WarehouseRemovalProduct> existingProducts = findAllByGroupId(request.getGroupId());

        if (existingProducts.isEmpty()) {
            throw new NotFoundException("GroupId-yə uyğun məhsul tapılmadı: " + request.getGroupId());
        }

        WarehouseRemoval warehouseRemoval = existingProducts.get(0).getWarehouseRemoval();
        if (request.getDate() != null) {
            warehouseRemoval.setDate(request.getDate());
        }
        if (request.getTime() != null) {
            warehouseRemoval.setTime(request.getTime());
        }
        if (request.getDescription() != null && warehouseRemoval.getOrderFromWarehouse() != null) {
            warehouseRemoval.getOrderFromWarehouse().setDescription(request.getDescription());
        }

        List<OutOfTheWarehouseDto> updatedDtos = new ArrayList<>();

        if (request.getRequests() != null && !request.getRequests().isEmpty()) {
            validateRequestConsistency(request.getRequests());

            for (WarehouseRemovalProductRequest req : request.getRequests()) {
                WarehouseRemovalProduct matchedProduct = findMatchingProduct(existingProducts, req, request.getGroupId());

                OrderFromWarehouseProduct orderFromWarehouseProduct =
                        orderFromWarehouseProductService.findById(req.getOrderFromWarehouseProductId());

                long currentExpenses = Optional.ofNullable(req.getCurrentExpenses())
                        .orElse(matchedProduct.getCurrentAmount());

                long updatedSendAmount = updateCalculateTotalSendAmount(warehouseRemoval.getId(), currentExpenses, req);
                long remainingAmount = orderFromWarehouseProduct.getInitialQuantity() - updatedSendAmount;

                matchedProduct.setCurrentAmount(currentExpenses);
                matchedProduct.setSendAmount(updatedSendAmount);
                matchedProduct.setRemainingAmount(remainingAmount);
                if (request.getDate() != null) {
                    matchedProduct.setDate(request.getDate());
                }
                if (request.getTime() != null) {
                    matchedProduct.setTime(request.getTime());
                }
                if (request.getDescription() != null) {
                    matchedProduct.setProductDescription(request.getDescription());
                }

                updatedDtos.add(prepareOutOfTheWarehouseDto(matchedProduct));
                warehouseRemovalProductRepository.save(matchedProduct);

                updateWarehouseProductQuantity(orderFromWarehouseProduct, currentExpenses, matchedProduct);
            }
        } else {
            for (WarehouseRemovalProduct matchedProduct : existingProducts) {
                if (request.getDate() != null) {
                    matchedProduct.setDate(request.getDate());
                }
                if (request.getTime() != null) {
                    matchedProduct.setTime(request.getTime());
                }
                if (request.getDescription() != null) {
                    matchedProduct.setProductDescription(request.getDescription());
                }
                warehouseRemovalProductRepository.save(matchedProduct);
                updatedDtos.add(prepareOutOfTheWarehouseDto(matchedProduct));
            }
        }

        updateWarehouseRemovalSummary(existingProducts, warehouseRemoval);

        WarehouseRemovalProduct sample = existingProducts.get(0);
        return WarehouseRemovalCreateResponse.builder()
                .groupId(sample.getGroupId())
                .date(sample.getDate())
                .time(sample.getTime())
                .description(request.getDescription() != null ? request.getDescription() : sample.getProductDescription())
                .number(updatedDtos.size())
                .status(sample.getPendingStatus())
                .outOfTheWarehouseDtos(updatedDtos)
                .build();
    }

    private WarehouseRemovalProduct findMatchingProduct(List<WarehouseRemovalProduct> existingProducts,
                                                        WarehouseRemovalProductRequest req,
                                                        String groupId) {
        return existingProducts.stream()
                .filter(p -> p.getId().equals(req.getWarehouseRemovalProductId()) && p.getGroupId().equals(groupId)
                        && p.getOrderFromWarehouseProductId().equals(req.getOrderFromWarehouseProductId())
                )
                .findFirst()
                .orElseThrow(() -> new NotFoundException("ID, OrderFromWarehouseProductId və GroupId uyğun məhsul tapılmadı. ID: "
                        + req.getWarehouseRemovalProductId() + ", GroupId: " + groupId +
                        ", OrderFromWarehouseProductId: " + req.getOrderFromWarehouseProductId()));
    }

    private void validateRequestConsistency(List<WarehouseRemovalProductRequest> requests) {
        Map<Long, Long> productToOrderMap = new HashMap<>();

        for (WarehouseRemovalProductRequest req : requests) {
            Long productId = req.getWarehouseRemovalProductId();
            Long orderId = req.getOrderFromWarehouseProductId();

            if (productToOrderMap.containsKey(productId)) {
                Long existingOrderId = productToOrderMap.get(productId);
                if (!existingOrderId.equals(orderId)) {
                    throw new IllegalArgumentException(
                            "Məhsul ID " + productId + " eyni olduğu halda, OrderFromWarehouseProduct ID-ləri fərqlidir: "
                                    + existingOrderId + " və " + orderId
                    );
                }
            } else {
                productToOrderMap.put(productId, orderId);
            }
        }
    }

    private void updateWarehouseRemovalSummary(List<WarehouseRemovalProduct> existingProducts,
                                               WarehouseRemoval warehouseRemoval) {
        long totalSendAmount = existingProducts.stream()
                .mapToLong(WarehouseRemovalProduct::getSendAmount)
                .sum();

        warehouseRemoval.setSendAmount(totalSendAmount);

        long totalRemainingAmount = warehouseRemoval.getOrderFromWarehouse().getSumQuantity() - totalSendAmount;
        warehouseRemoval.setRemainingAmount(totalRemainingAmount);
    }


    private long updateCalculateTotalSendAmount(Long removalId, long currentExpenses, WarehouseRemovalProductRequest requestDetail) {
        List<WarehouseRemovalProduct> all = warehouseRemovalProductRepository.findAllByIdAndWarehouseRemovalIdAndOrderFromWarehouseProductId
                (requestDetail.getWarehouseRemovalProductId(), removalId, requestDetail.getOrderFromWarehouseProductId());

        long totalExceptCurrent = all.stream()
                .filter(p -> !p.getId().equals(requestDetail.getWarehouseRemovalProductId()))
                .mapToLong(WarehouseRemovalProduct::getCurrentAmount)
                .sum();

        return totalExceptCurrent + currentExpenses;
    }

    private void updateWarehouseProductQuantity(OrderFromWarehouseProduct matchedProduct,
                                                long currentExpenses,
                                                WarehouseRemovalProduct product) {

        long orderAmount = product.getOrderAmount();

        if (currentExpenses > orderAmount) {
            throw new IllegalArgumentException("Göndərilən miqdar sifariş miqdarını keçə bilməz.");
        }

        matchedProduct.setQuantity(product.getRemainingAmount());
        orderFromWarehouseProductService.saveOrderFromWarehouseProduct(matchedProduct);
    }

}
