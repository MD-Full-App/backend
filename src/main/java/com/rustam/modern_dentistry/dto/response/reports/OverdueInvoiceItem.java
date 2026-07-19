package com.rustam.modern_dentistry.dto.response.reports;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverdueInvoiceItem {
    private String id;
    private String client;
    private String dueDate;
    private int daysOverdue;
    private BigDecimal amount;
}
