package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.OrderFromWarehouse;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.OrderFromWarehouseProduct;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemoval;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemovalProduct;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.WarehouseRemovalRepository;
import com.rustam.modern_dentistry.dto.OutOfTheWarehouseDto;
import com.rustam.modern_dentistry.dto.request.create.WarehouseRemovalCreateRequest;
import com.rustam.modern_dentistry.dto.request.create.WarehouseRemovalProductCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.WarehouseRemovalProductSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.WarehouseRemovalProductUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.WarehouseRemovalCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.WarehouseRemovalProductResponse;
import com.rustam.modern_dentistry.dto.response.read.WarehouseRemovalReadResponse;
import com.rustam.modern_dentistry.exception.custom.AmountSendException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.warehouse_operations.WarehouseRemovalMapper;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.warehouse_operations.WarehouseRemovalSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseRemovalService {

    WarehouseRemovalRepository warehouseRemovalRepository;
    WarehouseRemovalMapper warehouseRemovalMapper;

    public void save(WarehouseRemoval warehouseRemoval) {
        warehouseRemovalRepository.save(warehouseRemoval);
    }

    public WarehouseRemoval findById(Long id) {
        return warehouseRemovalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such warehouse removal found."));
    }

    @Transactional
    public List<WarehouseRemovalReadResponse> read() {
        List<WarehouseRemoval> warehouseRemovals = warehouseRemovalRepository.findAll();
        List<WarehouseRemovalReadResponse> responses = warehouseRemovalMapper.toDtos(warehouseRemovals);
        for (int i = 0; i < warehouseRemovals.size(); i++) {
            fillMissingRemovalData(warehouseRemovals.get(i), responses.get(i));
        }
        return responses;
    }

    @Transactional
    public List<WarehouseRemovalReadResponse> search(WarehouseRemovalProductSearchRequest warehouseRemovalSearchRequest) {
        List<WarehouseRemoval> warehouseRemovals = warehouseRemovalRepository.findAll(WarehouseRemovalSpecification.filterBy(warehouseRemovalSearchRequest));
        List<WarehouseRemovalReadResponse> responses = warehouseRemovalMapper.toDtos(warehouseRemovals);
        for (int i = 0; i < warehouseRemovals.size(); i++) {
            fillMissingRemovalData(warehouseRemovals.get(i), responses.get(i));
        }
        return responses;
    }

    @Transactional
    public WarehouseRemovalReadResponse update(Long id, WarehouseRemovalProductUpdateRequest request) {
        WarehouseRemoval warehouseRemoval = findById(id);
        if (request.getDate() != null) {
            warehouseRemoval.setDate(request.getDate());
        }
        if (request.getTime() != null) {
            warehouseRemoval.setTime(request.getTime());
        }
        if (request.getDescription() != null && warehouseRemoval.getOrderFromWarehouse() != null) {
            warehouseRemoval.getOrderFromWarehouse().setDescription(request.getDescription());
        }
        warehouseRemovalRepository.save(warehouseRemoval);
        return info(id);
    }

    @Transactional
    public void delete(Long id) {
        WarehouseRemoval warehouseRemoval = findById(id);
        warehouseRemovalRepository.delete(warehouseRemoval);
    }

    @Transactional
    public WarehouseRemovalReadResponse info(Long id) {
        WarehouseRemoval warehouseRemoval = findById(id);
        WarehouseRemovalReadResponse response = warehouseRemovalMapper.toDto(warehouseRemoval);
        fillMissingRemovalData(warehouseRemoval, response);
        return response;
    }

    private void fillMissingRemovalData(WarehouseRemoval entity, WarehouseRemovalReadResponse dto) {
        if (dto == null || entity == null) return;

        if (dto.getId() == null) {
            dto.setId(entity.getId());
        }

        if (dto.getDescription() == null && entity.getOrderFromWarehouse() != null) {
            dto.setDescription(entity.getOrderFromWarehouse().getDescription());
        }

        if (dto.getStatus() == null) {
            dto.setStatus(PendingStatus.WAITING);
        }

        if ((dto.getWarehouseRemovalProducts() == null || dto.getWarehouseRemovalProducts().isEmpty())
                && entity.getOrderFromWarehouse() != null
                && entity.getOrderFromWarehouse().getOrderFromWarehouseProducts() != null) {

            List<WarehouseRemovalProductResponse> productResponses = entity.getOrderFromWarehouse().getOrderFromWarehouseProducts().stream()
                    .map(p -> WarehouseRemovalProductResponse.builder()
                            .id(p.getId())
                            .orderFromWarehouseProductId(p.getId())
                            .categoryId(p.getCategoryId())
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .categoryName(p.getCategoryName())
                            .productDescription(p.getProductTitle())
                            .orderAmount(p.getInitialQuantity() != null ? p.getInitialQuantity() : p.getQuantity())
                            .remainingAmount(p.getQuantity())
                            .sendAmount(p.getInitialQuantity() != null ? Math.max(0, p.getInitialQuantity() - p.getQuantity()) : 0L)
                            .currentAmount(0L)
                            .price(p.getPrice())
                            .pendingStatus(PendingStatus.WAITING)
                            .date(entity.getDate())
                            .time(entity.getTime())
                            .build())
                    .collect(Collectors.toList());

            dto.setWarehouseRemovalProducts(productResponses);
        }
    }
}