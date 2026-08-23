package com.rustam.modern_dentistry.dto.response.read;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeletionFromWarehouseProductResponse {

    Long warehouseEntryProductId;

    Long warehouseEntryId;

    Long categoryId;

    String categoryName;

    String productName;

    String productTitle;

    Long productId;

    Long quantity;

    Long availableQuantity;
}
