package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.OrderFromWarehouse;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseReceipts;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemoval;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemovalProduct;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.OrderFromWarehouseRepository;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.WarehouseReceiptsRepository;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.WarehouseRemovalRepository;
import com.rustam.modern_dentistry.dto.OutOfTheWarehouseDto;
import com.rustam.modern_dentistry.dto.request.read.WarehouseReceiptsRequest;
import com.rustam.modern_dentistry.dto.request.update.WarehouseReceiptsStatusUpdateRequest;
import com.rustam.modern_dentistry.dto.response.read.WarehouseReceiptsInfoResponse;
import com.rustam.modern_dentistry.dto.response.read.WarehouseReceiptsResponse;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.warehouse_operations.WarehouseReceiptsMapper;
import com.rustam.modern_dentistry.util.specification.warehouse_operations.WarehouseReceiptsSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class WarehouseReceiptsService {

    WarehouseReceiptsRepository warehouseReceiptsRepository;
    WarehouseReceiptsMapper warehouseReceiptsMapper;
    OrderFromWarehouseRepository orderFromWarehouseRepository;
    WarehouseRemovalRepository warehouseRemovalRepository;
    WarehouseEntryProductService warehouseEntryProductService;

    @Transactional
    public List<WarehouseReceiptsResponse> search(WarehouseReceiptsRequest warehouseReceiptsRequest) {
        List<WarehouseReceipts> warehouseReceipts = warehouseReceiptsRepository.findAll();
        java.util.Map<Long, WarehouseReceipts> receiptMap = warehouseReceipts.stream()
                .filter(r -> r.getId() != null)
                .collect(java.util.stream.Collectors.toMap(WarehouseReceipts::getId, java.util.function.Function.identity(), (a, b) -> a));

        List<OrderFromWarehouse> orders = orderFromWarehouseRepository.findAll();

        java.util.Map<Long, WarehouseReceiptsResponse> resultMap = new java.util.LinkedHashMap<>();

        for (OrderFromWarehouse order : orders) {
            Long orderId = order.getId();
            WarehouseReceipts receipt = receiptMap.get(orderId);

            Long sendAmount = 0L;
            if (receipt != null && receipt.getSendQuantity() != null) {
                sendAmount = receipt.getSendQuantity();
            } else if (order.getWarehouseRemoval() != null && order.getWarehouseRemoval().getSendAmount() != null) {
                sendAmount = order.getWarehouseRemoval().getSendAmount();
            }

            com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus status = com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus.WAITING;
            if (receipt != null && receipt.getPendingStatus() != null) {
                status = receipt.getPendingStatus();
            }

            WarehouseReceiptsResponse response = WarehouseReceiptsResponse.builder()
                    .id(orderId)
                    .date(order.getDate())
                    .time(order.getTime())
                    .cabinetName(order.getCabinet() != null ? order.getCabinet().getCabinetName() : null)
                    .personWhoPlacedOrder(order.getPersonWhoPlacedOrder())
                    .orderQuantity(order.getSumQuantity())
                    .sendQuantity(sendAmount)
                    .pendingStatus(status)
                    .build();

            resultMap.put(orderId, response);
        }

        for (WarehouseReceipts receipt : warehouseReceipts) {
            if (!resultMap.containsKey(receipt.getId())) {
                resultMap.put(receipt.getId(), warehouseReceiptsMapper.toDto(receipt));
            }
        }

        List<WarehouseReceiptsResponse> list = new java.util.ArrayList<>(resultMap.values());

        if (warehouseReceiptsRequest != null) {
            if (warehouseReceiptsRequest.getPendingStatus() != null) {
                list = list.stream()
                        .filter(r -> r.getPendingStatus() == warehouseReceiptsRequest.getPendingStatus())
                        .collect(java.util.stream.Collectors.toList());
            }
            if (warehouseReceiptsRequest.getDate() != null) {
                list = list.stream()
                        .filter(r -> warehouseReceiptsRequest.getDate().equals(r.getDate()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        return list;
    }

    public WarehouseReceipts findById(Long warehouseReceiptsId) {
        return warehouseReceiptsRepository.findById(warehouseReceiptsId)
                .orElseThrow(() -> new NotFoundException("No such warehouse receipts found."));
    }

    @Transactional
    public WarehouseReceiptsResponse update(WarehouseReceiptsStatusUpdateRequest warehouseReceiptsStatusUpdateRequest) {
        Optional<WarehouseReceipts> optionalReceipts = warehouseReceiptsRepository.findById(warehouseReceiptsStatusUpdateRequest.getId());
        if (optionalReceipts.isPresent()) {
            WarehouseReceipts warehouseReceipts = optionalReceipts.get();
            warehouseReceipts.setPendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus());
            if (warehouseReceipts.getWarehouseRemovalProducts() != null) {
                for (WarehouseRemovalProduct warehouseRemovalProducts : warehouseReceipts.getWarehouseRemovalProducts()){
                    warehouseRemovalProducts.setPendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus());
                }
            }
            warehouseReceiptsRepository.save(warehouseReceipts);
            return warehouseReceiptsMapper.toDto(warehouseReceipts);
        }

        Optional<OrderFromWarehouse> optionalOrder = orderFromWarehouseRepository.findById(warehouseReceiptsStatusUpdateRequest.getId());
        if (optionalOrder.isPresent()) {
            OrderFromWarehouse order = optionalOrder.get();
            if (order.getWarehouseRemoval() != null && order.getWarehouseRemoval().getWarehouseRemovalProducts() != null) {
                for (WarehouseRemovalProduct p : order.getWarehouseRemoval().getWarehouseRemovalProducts()) {
                    p.setPendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus());
                }
                warehouseRemovalRepository.save(order.getWarehouseRemoval());
            }

            WarehouseReceipts receipt = warehouseReceiptsRepository.findById(order.getId()).orElse(null);
            if (receipt == null) {
                receipt = WarehouseReceipts.builder()
                        .id(order.getId())
                        .date(order.getDate())
                        .time(order.getTime())
                        .cabinet(order.getCabinet())
                        .personWhoPlacedOrder(order.getPersonWhoPlacedOrder())
                        .orderQuantity(order.getSumQuantity())
                        .sendQuantity(order.getWarehouseRemoval() != null ? order.getWarehouseRemoval().getSendAmount() : 0L)
                        .pendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus())
                        .build();
            } else {
                receipt.setPendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus());
            }
            warehouseReceiptsRepository.save(receipt);

            return WarehouseReceiptsResponse.builder()
                    .id(order.getId())
                    .date(order.getDate())
                    .time(order.getTime())
                    .cabinetName(order.getCabinet() != null ? order.getCabinet().getCabinetName() : null)
                    .personWhoPlacedOrder(order.getPersonWhoPlacedOrder())
                    .orderQuantity(order.getSumQuantity())
                    .sendQuantity(order.getWarehouseRemoval() != null ? order.getWarehouseRemoval().getSendAmount() : 0L)
                    .pendingStatus(warehouseReceiptsStatusUpdateRequest.getStatus())
                    .build();
        }

        throw new NotFoundException("No warehouse receipt or order found with ID: " + warehouseReceiptsStatusUpdateRequest.getId());
    }

    public WarehouseReceipts save(WarehouseReceipts warehouseReceipts) {
       return warehouseReceiptsRepository.save(warehouseReceipts);
    }

    public Optional<WarehouseReceipts> findByGroupId(String groupId) {
        return warehouseReceiptsRepository.findByGroupId(groupId);
    }

    @Transactional
    public WarehouseReceiptsInfoResponse info(Long id) {
        Optional<WarehouseReceipts> optionalReceipts = warehouseReceiptsRepository.findById(id);
        if (optionalReceipts.isPresent()) {
            WarehouseReceipts wr = optionalReceipts.get();
            if (wr.getWarehouseRemovalProducts() != null && !wr.getWarehouseRemovalProducts().isEmpty()) {
                return buildToResponse(wr);
            }
        }

        Optional<OrderFromWarehouse> optionalOrder = orderFromWarehouseRepository.findById(id);
        if (optionalOrder.isPresent()) {
            OrderFromWarehouse order = optionalOrder.get();
            List<OutOfTheWarehouseDto> dtos = order.getOrderFromWarehouseProducts().stream()
                    .map(p -> {
                        java.math.BigDecimal price = p.getPrice();
                        if (price == null) {
                            price = warehouseEntryProductService.findPriceForProduct(
                                    p.getWarehouseEntryProductId(),
                                    p.getWarehouseEntryId(),
                                    p.getProductId(),
                                    p.getProductName()
                            );
                        }
                        return OutOfTheWarehouseDto.builder()
                                .id(p.getId())
                                .orderFromWarehouseProductId(p.getId())
                                .categoryName(p.getCategoryName())
                                .productName(p.getProductName())
                                .productDescription(p.getProductTitle())
                                .orderQuantity(p.getInitialQuantity())
                                .sendQuantity(p.getInitialQuantity() - p.getQuantity())
                                .remainingQuantity(p.getQuantity())
                                .currentAmount(p.getQuantity())
                                .price(price)
                                .build();
                    })
                    .toList();

            Long sendAmt = order.getWarehouseRemoval() != null ? order.getWarehouseRemoval().getSendAmount() : 0L;
            com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus status = com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus.WAITING;
            if (optionalReceipts.isPresent() && optionalReceipts.get().getPendingStatus() != null) {
                status = optionalReceipts.get().getPendingStatus();
            } else if (order.getWarehouseRemoval() != null && order.getWarehouseRemoval().getWarehouseRemovalProducts() != null && !order.getWarehouseRemoval().getWarehouseRemovalProducts().isEmpty()) {
                status = order.getWarehouseRemoval().getWarehouseRemovalProducts().get(0).getPendingStatus();
            }

            return WarehouseReceiptsInfoResponse.builder()
                    .id(order.getId())
                    .orderQuantity(order.getSumQuantity())
                    .date(order.getDate())
                    .time(order.getTime())
                    .cabinetName(order.getCabinet() != null ? order.getCabinet().getCabinetName() : null)
                    .personWhoPlacedOrder(order.getPersonWhoPlacedOrder())
                    .incomingQuantity(sendAmt)
                    .pendingStatus(status)
                    .description(order.getDescription())
                    .outOfTheWarehouseDtos(dtos)
                    .build();
        }

        Optional<WarehouseRemoval> optionalRemoval = warehouseRemovalRepository.findById(id);
        if (optionalRemoval.isPresent()) {
            WarehouseRemoval removal = optionalRemoval.get();
            return info(removal.getOrderFromWarehouse().getId());
        }

        throw new NotFoundException("No warehouse receipt or order found with ID: " + id);
    }

    private WarehouseReceiptsInfoResponse buildToResponse(WarehouseReceipts warehouseReceipts) {
        WarehouseReceiptsInfoResponse warehouseReceiptsInfoResponse = buildWarehouseReceiptsBaseInfo(warehouseReceipts);
        List<OutOfTheWarehouseDto> outOfTheWarehouseDtoList = buildOutOfTheWarehouseDtoList(warehouseReceipts);
        warehouseReceiptsInfoResponse.setOutOfTheWarehouseDtos(outOfTheWarehouseDtoList);

        warehouseReceiptsInfoResponse.setDescription(
                warehouseReceipts.getWarehouseRemovalProducts().stream()
                        .map(WarehouseRemovalProduct::getProductDescription)
                        .reduce((first, second) -> second)
                        .orElse(null)
        );

        return warehouseReceiptsInfoResponse;
    }

    private WarehouseReceiptsInfoResponse buildWarehouseReceiptsBaseInfo(WarehouseReceipts warehouseReceipts) {
        return WarehouseReceiptsInfoResponse.builder()
                .id(warehouseReceipts.getId())
                .orderQuantity(warehouseReceipts.getOrderQuantity())
                .date(warehouseReceipts.getDate())
                .cabinetName(warehouseReceipts.getCabinet() != null ?
                        warehouseReceipts.getCabinet().getCabinetName() : null)
                .time(warehouseReceipts.getTime())
                .personWhoPlacedOrder(warehouseReceipts.getPersonWhoPlacedOrder())
                .incomingQuantity(warehouseReceipts.getSendQuantity())
                .pendingStatus(warehouseReceipts.getPendingStatus())
                .build();
    }

    private List<OutOfTheWarehouseDto> buildOutOfTheWarehouseDtoList(WarehouseReceipts warehouseReceipts) {
        return warehouseReceipts.getWarehouseRemovalProducts().stream()
                .map(this::buildOutOfTheWarehouseDto)
                .toList();
    }

    private OutOfTheWarehouseDto buildOutOfTheWarehouseDto(WarehouseRemovalProduct warehouseRemovalProduct) {
        return OutOfTheWarehouseDto.builder()
                .id(warehouseRemovalProduct.getId())
                .orderFromWarehouseProductId(warehouseRemovalProduct.getOrderFromWarehouseProductId())
                .categoryName(warehouseRemovalProduct.getCategoryName())
                .productName(warehouseRemovalProduct.getProductName())
                .productDescription(warehouseRemovalProduct.getProductDescription())
                .sendQuantity(warehouseRemovalProduct.getSendAmount())
                .orderQuantity(warehouseRemovalProduct.getOrderAmount())
                .remainingQuantity(warehouseRemovalProduct.getRemainingAmount())
                .currentAmount(warehouseRemovalProduct.getCurrentAmount())
                .build();
    }
}
