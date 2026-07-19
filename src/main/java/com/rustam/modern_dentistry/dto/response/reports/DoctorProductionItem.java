package com.rustam.modern_dentistry.dto.response.reports;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorProductionItem {
    private String name;
    private int treatments;
    private BigDecimal amount;
    private double pct;
}
