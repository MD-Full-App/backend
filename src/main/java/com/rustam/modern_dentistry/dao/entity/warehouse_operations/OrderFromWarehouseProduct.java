package com.rustam.modern_dentistry.dao.entity.warehouse_operations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "order_from_warehouse_product")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFromWarehouseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_from_warehouse_id")
    @JsonBackReference
    OrderFromWarehouse orderFromWarehouse;

    @Column(name = "category_id")
    Long categoryId;

    @Column(name = "initial_quantity")
    Long initialQuantity;

    @Column(name = "category_name")
    String categoryName;

    @Column(name = "product_name")
    String productName;

    @Column(name = "product_title")
    String productTitle;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "warehouse_entry_id")
    Long warehouseEntryId;

    @Column(name = "warehouse_entry_product_id")
    Long warehouseEntryProductId;

    Long quantity;

    @Column(name = "price")
    BigDecimal price;
}
