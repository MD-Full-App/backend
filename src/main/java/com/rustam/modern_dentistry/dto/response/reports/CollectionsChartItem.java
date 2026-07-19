package com.rustam.modern_dentistry.dto.response.reports;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionsChartItem {
    private String date;
    private BigDecimal value;
}
