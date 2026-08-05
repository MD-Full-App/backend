package com.rustam.modern_dentistry.dto.response.reports;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportResponse {
    private BigDecimal cashCollected;
    private BigDecimal patientCredit;
    private BigDecimal production;
    private BigDecimal avgTicket;
    private int paymentsCount;
    private int treatmentsCount;
    private BigDecimal totalInvoiced;
    private BigDecimal outstandingBalance;
    private int overdueInvoicesCount;
    private BigDecimal collectionsTotal;
    private int newPatients;
    private double noShowRate;
    private BigDecimal agingReceivables;
    private BigDecimal agingReceivables030;
    private int agingPatients030;
    private BigDecimal agingReceivables3160;
    private int agingPatients3160;
    private BigDecimal agingReceivables6190;
    private int agingPatients6190;
    private BigDecimal agingReceivables90Plus;
    private int agingPatients90Plus;

    private List<CollectionsChartItem> collectionsChart;
    private List<DoctorProductionItem> doctorsProduction;
    private List<PaymentMethodItem> paymentsMethod;
    private List<OverdueInvoiceItem> overdueInvoices;
    private List<ProfessionalItem> professionals;
}
