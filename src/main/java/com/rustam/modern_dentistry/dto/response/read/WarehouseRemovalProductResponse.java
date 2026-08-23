package com.rustam.modern_dentistry.dto.response.read;

import com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemoval;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseRemovalProductResponse {
    Long id;
    LocalDate date;
    LocalTime time;
    Long categoryId;
    Long productId;
    Long currentAmount;
    Long orderAmount;
    Long sendAmount;
    Long remainingAmount;

    String productName;
    String categoryName;
    String productDescription;
    PendingStatus pendingStatus;

    Integer number;
    String groupId;

    Long orderFromWarehouseProductId;
    java.math.BigDecimal price;

}
