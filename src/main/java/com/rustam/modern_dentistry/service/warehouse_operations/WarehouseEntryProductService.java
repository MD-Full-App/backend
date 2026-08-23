package com.rustam.modern_dentistry.service.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseEntryProduct;
import com.rustam.modern_dentistry.dao.repository.warehouse_operations.WarehouseEntryProductRepository;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;

import com.rustam.modern_dentistry.exception.custom.ProductDoesnotQuantityThatMuchException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class WarehouseEntryProductService {

    WarehouseEntryProductRepository warehouseEntryProductRepository;

    public void decreaseProductQuantity(Long productId, long quantityToDecrease) {
        WarehouseEntryProduct warehouseEntryProduct = findById(productId);

        if (warehouseEntryProduct.getQuantity() < quantityToDecrease) {
            throw new ProductDoesnotQuantityThatMuchException("Anbarda kifayət qədər məhsul yoxdur");
        }

        updateProductQuantities(warehouseEntryProduct, quantityToDecrease);
    }

    public WarehouseEntryProduct findById(Long productId) {
        return warehouseEntryProductRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("such warehouse entry product not found"));
    }

    public WarehouseEntryProduct findByIdOrNull(Long productId) {
        return warehouseEntryProductRepository.findById(productId).orElse(null);
    }


    private void updateProductQuantities(WarehouseEntryProduct product, long quantityToDecrease) {
        long currentQuantity = product.getQuantity();

        product.setQuantity(currentQuantity - quantityToDecrease);

        product.setUsedQuantity(product.getUsedQuantity() + quantityToDecrease);

        warehouseEntryProductRepository.save(product);
    }

    public void increaseProductQuantity(Long productId, long quantityToIncrease) {
        WarehouseEntryProduct warehouseEntryProduct = findById(productId);
        warehouseEntryProduct.setQuantity(warehouseEntryProduct.getQuantity() + quantityToIncrease);
        warehouseEntryProduct.setUsedQuantity(warehouseEntryProduct.getUsedQuantity() - quantityToIncrease);
        warehouseEntryProductRepository.save(warehouseEntryProduct);
    }

    public BigDecimal findPriceForProduct(Long warehouseEntryProductId, Long warehouseEntryId, Long productId, String productName) {
        if (warehouseEntryProductId != null) {
            WarehouseEntryProduct wep = findByIdOrNull(warehouseEntryProductId);
            if (wep != null && wep.getPrice() != null) {
                return wep.getPrice();
            }
        }
        if (warehouseEntryId != null && productId != null) {
            List<WarehouseEntryProduct> list = warehouseEntryProductRepository.findByWarehouseEntryIdAndProductId(warehouseEntryId, productId);
            for (WarehouseEntryProduct wep : list) {
                if (wep.getPrice() != null) {
                    return wep.getPrice();
                }
            }
        }
        if (productId != null) {
            List<WarehouseEntryProduct> list = warehouseEntryProductRepository.findByProductId(productId);
            for (WarehouseEntryProduct wep : list) {
                if (wep.getPrice() != null) {
                    return wep.getPrice();
                }
            }
        }
        if (productName != null && !productName.isBlank()) {
            List<WarehouseEntryProduct> list = warehouseEntryProductRepository.findByProductName(productName);
            for (WarehouseEntryProduct wep : list) {
                if (wep.getPrice() != null) {
                    return wep.getPrice();
                }
            }
        }
        return null;
    }

    public WarehouseEntryProduct findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(Long id, Long warehouseEntryId, Long categoryId, Long productId) {
        if (id != null && warehouseEntryId != null && categoryId != null && productId != null) {
            List<WarehouseEntryProduct> products =
                    warehouseEntryProductRepository.findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(
                            id, warehouseEntryId, categoryId, productId);
            if (!products.isEmpty()) {
                return products.get(0);
            }
        }
        if (id != null) {
            WarehouseEntryProduct wep = findByIdOrNull(id);
            if (wep != null) {
                return wep;
            }
        }
        if (warehouseEntryId != null && productId != null) {
            List<WarehouseEntryProduct> list = warehouseEntryProductRepository.findByWarehouseEntryIdAndProductId(warehouseEntryId, productId);
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        if (productId != null) {
            List<WarehouseEntryProduct> list = warehouseEntryProductRepository.findByProductId(productId);
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        throw new NotFoundException("No matching WarehouseEntryProduct found");
    }

    public void delete(WarehouseEntryProduct entryProduct) {
        warehouseEntryProductRepository.delete(entryProduct);
    }

    public void save(WarehouseEntryProduct entryProduct) {
        warehouseEntryProductRepository.save(entryProduct);
    }
}
