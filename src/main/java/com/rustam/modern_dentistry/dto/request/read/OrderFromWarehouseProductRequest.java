package com.rustam.modern_dentistry.dto.request.read;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFromWarehouseProductRequest {

    Long warehouseEntryId;
    Long warehouseEntryProductId;
    Long categoryId;
    Long productId;
    Long quantity;
    BigDecimal price;
}
