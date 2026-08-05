package com.rustam.modern_dentistry.dto.response.reports;

import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedReportsResponse {
    private List<PatientReportReadResponse> content;
    private int number;
    private int totalPages;
    private long totalElements;
    private BigDecimal totalRawPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalNetPrice;
}
