package com.rustam.modern_dentistry.dto.response.read;

import com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseRemovalProductReadResponse {

    Long warehouseRemovalId;
    Long id;
    LocalDate date;
    LocalTime time;
    Integer number;
    PendingStatus pendingStatus;
    String groupId;

    Long categoryId;
    String categoryName;
    Long productId;
    String productName;
    String idNumber;
    Long quantity;
}
