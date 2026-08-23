package com.rustam.modern_dentistry.dao.repository.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemovalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WarehouseRemovalProductRepository extends JpaRepository<WarehouseRemovalProduct,Long> , JpaSpecificationExecutor<WarehouseRemovalProduct> {
    List<WarehouseRemovalProduct> findAllByIdAndWarehouseRemovalIdAndOrderFromWarehouseProductId(Long id,Long warehouseRemovalId, Long orderFromWarehouseProductId);

    List<WarehouseRemovalProduct> findAllByIdAndGroupIdAndOrderFromWarehouseProductId(Long id, String groupId, Long orderFromWarehouseProductId);

    List<WarehouseRemovalProduct> findAllByGroupId(String groupId);

    List<WarehouseRemovalProduct> findAllByWarehouseRemovalIdAndOrderFromWarehouseProductId(Long removalId, Long orderFromWarehouseProductId);

    List<WarehouseRemovalProduct> findAllByWarehouseRemovalId(Long warehouseRemovalId);
}
