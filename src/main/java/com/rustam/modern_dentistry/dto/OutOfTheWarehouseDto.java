package com.rustam.modern_dentistry.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutOfTheWarehouseDto {

    String categoryName;
    String productName;
    String productDescription;
    Long sendQuantity;
    Long orderQuantity;
    Long remainingQuantity;
    Long currentAmount;
    Long orderFromWarehouseProductId;
    Long id;
    java.math.BigDecimal price;
}
