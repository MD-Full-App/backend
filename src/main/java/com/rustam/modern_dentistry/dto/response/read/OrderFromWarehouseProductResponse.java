package com.rustam.modern_dentistry.dto.response.read;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFromWarehouseProductResponse {

    Long id;
    Long categoryId;
    Long productId;
    Long warehouseEntryId;
    Long warehouseEntryProductId;
    String warehouseEntryProductName;
    String categoryName;
    String productName;
    String productTitle;
    Long quantity;
    BigDecimal price;
}
