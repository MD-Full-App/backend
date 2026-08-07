package com.rustam.modern_dentistry.dto.response.reports;

import com.rustam.modern_dentistry.dto.response.read.TechnicianOrderResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class LaboratoryReportResponse {
    long totalOrders;
    BigDecimal totalAmount;
    BigDecimal avgAmount;
    long completedOrders;

    Map<String, Long> statusBreakdown;
    Map<String, Long> categoryBreakdown;
    Map<String, Long> technicianBreakdown;
    List<CollectionsChartItem> timelineData;

    List<TechnicianOrderResponse> content;
    int page;
    int totalPages;
    long totalElements;
}
