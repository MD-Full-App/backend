package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.DeletionFromWarehouse;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.DeletionFromWarehouseProduct;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseEntry;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseEntryProduct;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.DeletionFromWarehouseRepository;
import com.rustam.modern_dentistry.dto.request.create.DeletionFromWarehouseCreateRequest;
import com.rustam.modern_dentistry.dto.request.create.DeletionFromWarehouseProductRequest;
import com.rustam.modern_dentistry.dto.request.read.DeletionFromWarehouseSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.DeletionFromWarehouseUpdateRequest;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseProductResponse;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseReadResponse;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseResponse;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.warehouse_operations.DeletionFromWarehouseMapper;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.warehouse_operations.DeletionFromWarehouseSpecification;
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
public class DeletionFromWarehouseService {

    DeletionFromWarehouseRepository deletionFromWarehouseRepository;
    WarehouseEntryProductService warehouseEntryProductService;
    DeletionFromWarehouseMapper deletionFromWarehouseMapper;
    WarehouseEntryService warehouseEntryService;
    UtilService utilService;
    DeletionFromWarehouseProductService deletionFromWarehouseProductService;

    @Transactional
    public DeletionFromWarehouseResponse create(DeletionFromWarehouseCreateRequest request) {
        DeletionFromWarehouse deletion = DeletionFromWarehouse.builder()
                .date(request.getDate())
                .time(request.getTime())
                .description(request.getDescription())
                .build();

        List<DeletionFromWarehouseProduct> deletionProducts = new ArrayList<>();

        for (DeletionFromWarehouseProductRequest productRequest : request.getDeletionFromWarehouseProductRequests()) {
            WarehouseEntryProduct entryProduct = warehouseEntryProductService
                    .findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                            productRequest.getWarehouseEntryProductId(),
                            productRequest.getWarehouseEntryId(),
                            productRequest.getCategoryId(),
                            productRequest.getProductId()
                    );

            if (entryProduct.getQuantity() < productRequest.getQuantity()) {
                throw new NotFoundException("Not enough stock for product: " + entryProduct.getProductName());
            }

            DeletionFromWarehouseProduct deletionProduct = DeletionFromWarehouseProduct.builder()
                    .price(entryProduct.getPrice())
                    .usedQuantity(entryProduct.getUsedQuantity())
                    .productId(productRequest.getProductId())
                    .categoryId(productRequest.getCategoryId())
                    .quantity(productRequest.getQuantity())
                    .warehouseEntryId(entryProduct.getWarehouseEntry().getId())
                    .warehouseEntryProductId(entryProduct.getId())
                    .categoryId(entryProduct.getCategoryId())
                    .productId(entryProduct.getProductId())
                    .productName(entryProduct.getProductName())
                    .categoryName(entryProduct.getCategoryName())
                    .productTitle(entryProduct.getProductTitle())
                    .deletionFromWarehouse(deletion)
                    .build();

            deletionProducts.add(deletionProduct);

            if (entryProduct.getQuantity().equals(productRequest.getQuantity())) {
                warehouseEntryProductService.delete(entryProduct);
            } else {
                entryProduct.setQuantity(entryProduct.getQuantity() - productRequest.getQuantity());
            }
        }

        deletion.setDeletionFromWarehouseProducts(deletionProducts);
        deletion.setNumber(deletionProducts.size());
        deletionFromWarehouseRepository.save(deletion);

        return deletionFromWarehouseMapper.toDto(deletion);
    }

    @Transactional
    public List<DeletionFromWarehouseResponse> read() {
        return deletionFromWarehouseMapper.toDtos(deletionFromWarehouseRepository.findAll());
    }

    @Transactional
    public List<DeletionFromWarehouseResponse> search(DeletionFromWarehouseSearchRequest deletionFromWarehouseSearchRequest) {
        return deletionFromWarehouseMapper.toDtos(
                deletionFromWarehouseRepository.findAll(DeletionFromWarehouseSpecification.filterBy(deletionFromWarehouseSearchRequest))
        );
    }

    @Transactional
    public DeletionFromWarehouseReadResponse info(Long id) {
        DeletionFromWarehouse deletionFromWarehouse = findById(id);
        List<DeletionFromWarehouseProductResponse> deletionFromWarehouseProductResponses =
                buildDeletionFromWarehouseProductResponse(deletionFromWarehouse.getDeletionFromWarehouseProducts());
        return DeletionFromWarehouseReadResponse.builder()
                .date(deletionFromWarehouse.getDate())
                .time(deletionFromWarehouse.getTime())
                .deletionFromWarehouseProductResponses(deletionFromWarehouseProductResponses)
                .number(deletionFromWarehouse.getNumber())
                .description(deletionFromWarehouse.getDescription())
                .build();
    }

    private List<DeletionFromWarehouseProductResponse> buildDeletionFromWarehouseProductResponse(List<DeletionFromWarehouseProduct> deletionFromWarehouseProducts) {
        return deletionFromWarehouseProducts.stream()
                .map(p -> {
                    DeletionFromWarehouseProductResponse res = deletionFromWarehouseMapper.toResponse(p);
                    WarehouseEntryProduct entryProduct = warehouseEntryProductService.findByIdOrNull(p.getWarehouseEntryProductId());
                    res.setAvailableQuantity(entryProduct != null ? entryProduct.getQuantity() : p.getQuantity());
                    return res;
                })
                .toList();
    }

    private DeletionFromWarehouse findById(Long id) {
        return deletionFromWarehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No such deletion from warehouse found."));
    }

    @Transactional
    public void delete(Long id) {
        DeletionFromWarehouse deletion = findById(id);
        restoreProductsToWarehouse(deletion.getDeletionFromWarehouseProducts());
        deletionFromWarehouseRepository.delete(deletion);
    }

    private void restoreProductsToWarehouse(List<DeletionFromWarehouseProduct> deletedProducts) {
        for (DeletionFromWarehouseProduct product : deletedProducts) {
            WarehouseEntryProduct entryProduct = warehouseEntryProductService
                    .findByIdOrNull(product.getWarehouseEntryProductId());

            if (entryProduct != null) {
                increaseQuantity(entryProduct, product.getQuantity());
            } else {
                createNewEntryProduct(product);
            }
        }
    }

    private void increaseQuantity(WarehouseEntryProduct entryProduct, Long quantity) {
        entryProduct.setQuantity(entryProduct.getQuantity() + quantity);
        warehouseEntryProductService.save(entryProduct);
    }

    private void createNewEntryProduct(DeletionFromWarehouseProduct product) {
        WarehouseEntry warehouseEntry = warehouseEntryService.findById(product.getWarehouseEntryId());
        WarehouseEntryProduct newEntry = WarehouseEntryProduct.builder()
                .warehouseEntry(warehouseEntry)
                .categoryId(product.getCategoryId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .categoryName(product.getCategoryName())
                .productTitle(product.getProductTitle())
                .quantity(product.getQuantity())
                .usedQuantity(product.getUsedQuantity())
                .price(product.getPrice())
                .build();

        warehouseEntryProductService.save(newEntry);
    }

    @Transactional
    public DeletionFromWarehouseReadResponse update(DeletionFromWarehouseUpdateRequest request) {
        DeletionFromWarehouse existing = findById(request.getDeletionFromWarehouseId());

        restoreProductsToWarehouse(existing.getDeletionFromWarehouseProducts());

        updateMainFields(request, existing);

        if (existing.getDeletionFromWarehouseProducts() != null) {
            existing.getDeletionFromWarehouseProducts().clear();
        } else {
            existing.setDeletionFromWarehouseProducts(new ArrayList<>());
        }

        List<DeletionFromWarehouseProduct> newDeletionProducts = new ArrayList<>();

        if (request.getDeletionFromWarehouseProductRequests() != null) {
            for (DeletionFromWarehouseProductRequest productRequest : request.getDeletionFromWarehouseProductRequests()) {
                WarehouseEntryProduct entryProduct = warehouseEntryProductService
                        .findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                                productRequest.getWarehouseEntryProductId(),
                                productRequest.getWarehouseEntryId(),
                                productRequest.getCategoryId(),
                                productRequest.getProductId()
                        );

                if (entryProduct.getQuantity() < productRequest.getQuantity()) {
                    throw new NotFoundException("Not enough stock for product: " + entryProduct.getProductName());
                }

                DeletionFromWarehouseProduct deletionProduct = DeletionFromWarehouseProduct.builder()
                        .price(entryProduct.getPrice())
                        .usedQuantity(entryProduct.getUsedQuantity())
                        .productId(productRequest.getProductId())
                        .categoryId(productRequest.getCategoryId())
                        .quantity(productRequest.getQuantity())
                        .warehouseEntryId(entryProduct.getWarehouseEntry().getId())
                        .warehouseEntryProductId(entryProduct.getId())
                        .productName(entryProduct.getProductName())
                        .categoryName(entryProduct.getCategoryName())
                        .productTitle(entryProduct.getProductTitle())
                        .deletionFromWarehouse(existing)
                        .build();

                newDeletionProducts.add(deletionProduct);

                if (entryProduct.getQuantity().equals(productRequest.getQuantity())) {
                    warehouseEntryProductService.delete(entryProduct);
                } else {
                    entryProduct.setQuantity(entryProduct.getQuantity() - productRequest.getQuantity());
                }
            }
        }

        existing.getDeletionFromWarehouseProducts().addAll(newDeletionProducts);
        existing.setNumber(newDeletionProducts.size());
        deletionFromWarehouseRepository.save(existing);

        List<DeletionFromWarehouseProductResponse> productResponses =
                buildDeletionFromWarehouseProductResponse(newDeletionProducts);

        return DeletionFromWarehouseReadResponse.builder()
                .date(existing.getDate())
                .time(existing.getTime())
                .description(existing.getDescription())
                .number(existing.getNumber())
                .deletionFromWarehouseProductResponses(productResponses)
                .build();
    }



    public void updateProductFields(DeletionFromWarehouseProductRequest req,
                                    DeletionFromWarehouseProduct product,
                                    WarehouseEntryProduct entry) {
        utilService.updateFieldIfPresent(req.getQuantity(), product::setQuantity);
        utilService.updateFieldIfPresent(req.getProductId(), product::setProductId);
        utilService.updateFieldIfPresent(req.getCategoryId(), product::setCategoryId);
        utilService.updateFieldIfPresent(req.getWarehouseEntryId(), product::setWarehouseEntryId);
        utilService.updateFieldIfPresent(req.getWarehouseEntryProductId(), product::setWarehouseEntryProductId);

        utilService.updateFieldIfPresent(entry.getProductName(), product::setProductName);
        utilService.updateFieldIfPresent(entry.getCategoryName(), product::setCategoryName);
        utilService.updateFieldIfPresent(entry.getProductTitle(), product::setProductTitle);
        utilService.updateFieldIfPresent(entry.getPrice(), product::setPrice);
        utilService.updateFieldIfPresent(entry.getUsedQuantity(), product::setUsedQuantity);
    }

    public void updateMainFields(DeletionFromWarehouseUpdateRequest req, DeletionFromWarehouse entity) {
        utilService.updateFieldIfPresent(req.getDate(), entity::setDate);
        utilService.updateFieldIfPresent(req.getTime(), entity::setTime);
        utilService.updateFieldIfPresent(req.getDescription(), entity::setDescription);
    }
}
