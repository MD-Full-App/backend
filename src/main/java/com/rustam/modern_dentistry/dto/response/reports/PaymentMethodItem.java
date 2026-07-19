package com.rustam.modern_dentistry.dto.response.reports;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodItem {
    private String name;
    private int count;
    private BigDecimal amount;
    private String color;
}
