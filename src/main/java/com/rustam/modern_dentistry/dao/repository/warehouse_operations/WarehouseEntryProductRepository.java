package com.rustam.modern_dentistry.dao.repository.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseEntryProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseEntryProductRepository extends JpaRepository<WarehouseEntryProduct,Long> {

    List<WarehouseEntryProduct> findAllByIdAndWarehouseEntryIdAndCategoryIdAndProductId(Long id,Long warehouseEntryId, Long categoryId, Long productId);

    List<WarehouseEntryProduct> findByWarehouseEntryIdAndProductId(Long warehouseEntryId, Long productId);

    List<WarehouseEntryProduct> findByProductId(Long productId);

    List<WarehouseEntryProduct> findByProductName(String productName);
}
