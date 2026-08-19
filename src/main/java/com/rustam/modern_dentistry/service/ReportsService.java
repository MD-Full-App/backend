package com.rustam.modern_dentistry.service;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dao.entity.patient_info.Invoice;
import com.rustam.modern_dentistry.dao.entity.patient_info.Payment;
import com.rustam.modern_dentistry.dao.entity.enums.status.Appointment;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientReportRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.InvoiceRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PaymentRepository;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.repository.BaseUserRepository;
import com.rustam.modern_dentistry.dao.repository.GeneralCalendarRepository;
import com.rustam.modern_dentistry.dao.repository.PatientRepository;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import com.rustam.modern_dentistry.dao.repository.laboratory.DentalOrderRepository;
import com.rustam.modern_dentistry.dto.request.criteria.DetailedReportCriteria;
import com.rustam.modern_dentistry.dto.response.excel.DetailedReportExcelResponse;
import com.rustam.modern_dentistry.dto.response.excel.PaymentExcelResponse;
import com.rustam.modern_dentistry.dto.response.excel.LaboratoryReportExcelResponse;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import com.rustam.modern_dentistry.dto.response.read.TechnicianOrderResponse;
import com.rustam.modern_dentistry.dto.response.reports.*;
import com.rustam.modern_dentistry.mapper.laboratory.DentalOrderMapper;
import com.rustam.modern_dentistry.util.ExcelUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final PatientReportRepository patientReportRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BaseUserRepository baseUserRepository;
    private final GeneralCalendarRepository generalCalendarRepository;
    private final PatientRepository patientRepository;
    private final DentalOrderRepository dentalOrderRepository;
    private final DentalOrderMapper dentalOrderMapper;

    @Transactional(readOnly = true)
    public DashboardReportResponse getDashboardReport(String period, String fromDateStr, String toDateStr) {
        long[] range = getStartAndEndTimestamps(period, fromDateStr, toDateStr);
        long start = range[0];
        long end = range[1];

        LocalDate startDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate();

        // 1. Production & Treatments Count
        List<PatientReport> reports = patientReportRepository.findAllByExecutionDateBetween(start, end);
        BigDecimal production = reports.stream()
                .map(PatientReport::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int treatmentsCount = reports.size();

        // 2. Invoices & Total Invoiced
        List<Invoice> invoices = invoiceRepository.findAllByIssueDateBetween(startDate, endDate);
        BigDecimal totalInvoiced = invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Payments & Cash Collected
        List<Payment> payments = paymentRepository.findAllByPaymentDateBetween(startDate, endDate);
        BigDecimal cashCollected = payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int paymentsCount = payments.size();

        BigDecimal outstandingBalance = totalInvoiced.subtract(cashCollected);
        if (outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
            outstandingBalance = BigDecimal.ZERO;
        }

        BigDecimal avgTicket = paymentsCount > 0 
                ? cashCollected.divide(BigDecimal.valueOf(paymentsCount), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        // 4. New Patients & No-Show Rate
        long newPatientsCount = patientRepository.countByRegistrationDateBetween(startDate, endDate);
        long totalAppointments = generalCalendarRepository.countByDateBetween(startDate, endDate);
        double newPatientsPct = totalAppointments > 0 
                ? ((double) newPatientsCount / totalAppointments) * 100.0 
                : 0.0;

        long cancelAppointments = generalCalendarRepository.countByDateBetweenAndAppointment(startDate, endDate, Appointment.CANCELED);
        double noShowRate = totalAppointments > 0 
                ? ((double) cancelAppointments / totalAppointments) * 100.0 
                : 0.0;

        // 5. Doctors Production
        Map<String, List<PatientReport>> byDoctor = reports.stream()
                .filter(r -> r.getExecutionDoctorName() != null)
                .collect(Collectors.groupingBy(r -> resolveDoctorName(r.getExecutionDoctorName())));

        List<DoctorProductionItem> doctorsProduction = new ArrayList<>();
        BigDecimal totalDoctorAmount = byDoctor.values().stream()
                .flatMap(List::stream)
                .map(PatientReport::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map.Entry<String, List<PatientReport>> entry : byDoctor.entrySet()) {
            BigDecimal amount = entry.getValue().stream()
                    .map(PatientReport::getFinalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double pct = totalDoctorAmount.compareTo(BigDecimal.ZERO) > 0 
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(totalDoctorAmount, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            doctorsProduction.add(DoctorProductionItem.builder()
                    .name(entry.getKey())
                    .treatments(entry.getValue().size())
                    .amount(amount)
                    .pct(pct)
                    .build());
        }
        doctorsProduction.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        // 6. Payments Method Breakdown
        Map<String, List<Payment>> byMethod = payments.stream()
                .filter(p -> p.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(Payment::getPaymentMethod));

        List<PaymentMethodItem> paymentsMethod = new ArrayList<>();
        String[] methods = {"Kart", "Bank köçürməsi", "Nəğd", "Direct debit"};
        String[] colors = {"#3B82F6", "#1D4ED8", "#10B981", "#6B7280"};
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i];
            String color = colors[i];
            List<Payment> methodPayments = byMethod.getOrDefault(method, Collections.emptyList());
            BigDecimal amount = methodPayments.stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            paymentsMethod.add(new PaymentMethodItem(method, methodPayments.size(), amount, color));
        }

        // 7. Professionals Invoicing Breakdown
        Map<String, List<Invoice>> byProfessional = invoices.stream()
                .filter(i -> i.getDoctor() != null)
                .collect(Collectors.groupingBy(i -> i.getDoctor().getName() + " " + i.getDoctor().getSurname()));

        List<ProfessionalItem> professionals = byProfessional.entrySet().stream()
                .map(entry -> {
                    BigDecimal amount = entry.getValue().stream()
                            .map(Invoice::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ProfessionalItem(entry.getKey(), entry.getValue().size(), amount);
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());

        // 8. Collections Chart Grouping
        List<CollectionsChartItem> collectionsChart = getChartData(payments, period);

        // 9. Overdue Invoices & Aging Receivables
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoicesList = invoiceRepository.findOverdueInvoices(today);

        List<OverdueInvoiceItem> overdueInvoices = new ArrayList<>();
        BigDecimal agingReceivables = BigDecimal.ZERO;

        BigDecimal bucket030 = BigDecimal.ZERO;
        BigDecimal bucket3160 = BigDecimal.ZERO;
        BigDecimal bucket6190 = BigDecimal.ZERO;
        BigDecimal bucket90Plus = BigDecimal.ZERO;

        int p030 = 0;
        int p3160 = 0;
        int p6190 = 0;
        int p90Plus = 0;

        for (Invoice inv : overdueInvoicesList) {
            BigDecimal paidAmount = inv.getPayments() != null ? inv.getPayments().stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
            BigDecimal unpaidAmount = inv.getTotalAmount().subtract(paidAmount);
            
            if (unpaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            agingReceivables = agingReceivables.add(unpaidAmount);
            long daysOverdue = ChronoUnit.DAYS.between(inv.getDueDate(), today);
            
            if (overdueInvoices.size() < 10) {
                overdueInvoices.add(OverdueInvoiceItem.builder()
                        .id(inv.getInvoiceNumber())
                        .client(inv.getPatient().getName() + " " + inv.getPatient().getSurname())
                        .dueDate(inv.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", new Locale("az"))))
                        .daysOverdue((int) daysOverdue)
                        .amount(unpaidAmount)
                        .build());
            }

            if (daysOverdue <= 30) {
                bucket030 = bucket030.add(unpaidAmount);
                p030++;
            } else if (daysOverdue <= 60) {
                bucket3160 = bucket3160.add(unpaidAmount);
                p3160++;
            } else if (daysOverdue <= 90) {
                bucket6190 = bucket6190.add(unpaidAmount);
                p6190++;
            } else {
                bucket90Plus = bucket90Plus.add(unpaidAmount);
                p90Plus++;
            }
        }

        return DashboardReportResponse.builder()
                .cashCollected(cashCollected)
                .patientCredit(BigDecimal.ZERO)
                .production(production)
                .avgTicket(avgTicket)
                .paymentsCount(paymentsCount)
                .treatmentsCount(treatmentsCount)
                .totalInvoiced(totalInvoiced)
                .outstandingBalance(outstandingBalance)
                .overdueInvoicesCount(overdueInvoices.size())
                .collectionsTotal(cashCollected)
                .newPatients((int) newPatientsCount)
                .noShowRate(noShowRate)
                .agingReceivables(agingReceivables)
                .agingReceivables030(bucket030)
                .agingPatients030(p030)
                .agingReceivables3160(bucket3160)
                .agingPatients3160(p3160)
                .agingReceivables6190(bucket6190)
                .agingPatients6190(p6190)
                .agingReceivables90Plus(bucket90Plus)
                .agingPatients90Plus(p90Plus)
                .collectionsChart(collectionsChart)
                .doctorsProduction(doctorsProduction)
                .paymentsMethod(paymentsMethod)
                .overdueInvoices(overdueInvoices)
                .professionals(professionals)
                .build();
    }

    private Specification<PatientReport> buildDetailedReportSpec(DetailedReportCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria != null) {
                if (criteria.getPlannerDoctor() != null && !criteria.getPlannerDoctor().trim().isEmpty()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("planningDoctorName")),
                            "%" + criteria.getPlannerDoctor().trim().toLowerCase() + "%"
                    ));
                }
                if (criteria.getExecutorDoctor() != null && !criteria.getExecutorDoctor().trim().isEmpty()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("executionDoctorName")),
                            "%" + criteria.getExecutorDoctor().trim().toLowerCase() + "%"
                    ));
                }
                if (criteria.getOperationName() != null && !criteria.getOperationName().trim().isEmpty()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("operationName")),
                            "%" + criteria.getOperationName().trim().toLowerCase() + "%"
                    ));
                }
                if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("operationName")),
                            "%" + criteria.getCategory().trim().toLowerCase() + "%"
                    ));
                }
                if (criteria.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("executionDate"), criteria.getStartDate()));
                }
                if (criteria.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("executionDate"), criteria.getEndDate()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public DetailedReportsResponse getDetailedReports(DetailedReportCriteria criteria, Pageable pageable) {
        Specification<PatientReport> spec = buildDetailedReportSpec(criteria);

        Page<PatientReport> page = patientReportRepository.findAll(spec, pageable);
        List<PatientReport> allMatched = patientReportRepository.findAll(spec);

        BigDecimal totalRawPrice = allMatched.stream()
                .map(PatientReport::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = allMatched.stream()
                .map(PatientReport::getDiscount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetPrice = allMatched.stream()
                .map(PatientReport::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PatientReportReadResponse> mappedContent = page.getContent().stream()
                .map(report -> PatientReportReadResponse.builder()
                        .planDate(report.getPlanDate())
                        .patientName(report.getPatientName())
                        .teethNo(report.getTeethNo())
                        .operationName(report.getOperationName())
                        .planningDoctorName(resolveDoctorName(report.getPlanningDoctorName()))
                        .price(report.getPrice())
                        .discount(report.getDiscount())
                        .finalPrice(report.getFinalPrice())
                        .executionDate(report.getExecutionDate())
                        .executionDoctorName(resolveDoctorName(report.getExecutionDoctorName()))
                        .build())
                .collect(Collectors.toList());

        return DetailedReportsResponse.builder()
                .content(mappedContent)
                .number(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .totalRawPrice(totalRawPrice)
                .totalDiscount(totalDiscount)
                .totalNetPrice(totalNetPrice)
                .build();
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportDetailedReportsExcel(DetailedReportCriteria criteria) {
        Specification<PatientReport> spec = buildDetailedReportSpec(criteria);
        List<PatientReport> reports = patientReportRepository.findAll(spec);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

        List<DetailedReportExcelResponse> excelData = reports.stream()
                .map(r -> DetailedReportExcelResponse.builder()
                        .planDate(r.getPlanDate() != null ? formatter.format(Instant.ofEpochMilli(r.getPlanDate())) : "")
                        .patientName(r.getPatientName() != null ? r.getPatientName() : "")
                        .teethNo(r.getTeethNo() != null ? String.valueOf(r.getTeethNo()) : "")
                        .operationName(r.getOperationName() != null ? r.getOperationName() : "")
                        .planningDoctorName(r.getPlanningDoctorName() != null ? resolveDoctorName(r.getPlanningDoctorName()) : "")
                        .price(r.getPrice() != null ? r.getPrice() : BigDecimal.ZERO)
                        .discount(r.getDiscount() != null ? r.getDiscount() : BigDecimal.ZERO)
                        .finalPrice(r.getFinalPrice() != null ? r.getFinalPrice() : BigDecimal.ZERO)
                        .executionDate(r.getExecutionDate() != null ? formatter.format(Instant.ofEpochMilli(r.getExecutionDate())) : "")
                        .executionDoctorName(r.getExecutionDoctorName() != null ? resolveDoctorName(r.getExecutionDoctorName()) : "")
                        .build())
                .collect(Collectors.toList());

        return ExcelUtil.dataToExcel(excelData, DetailedReportExcelResponse.class);
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportPaymentsExcel(String period, String fromDateStr, String toDateStr) {
        long[] range = getStartAndEndTimestamps(period, fromDateStr, toDateStr);
        LocalDate startDate = Instant.ofEpochMilli(range[0]).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(range[1]).atZone(ZoneId.systemDefault()).toLocalDate();

        List<Payment> payments = paymentRepository.findAllByPaymentDateBetween(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        List<PaymentExcelResponse> excelData = payments.stream()
                .map(p -> {
                    String patName = "";
                    String docName = "";
                    if (p.getInvoice() != null) {
                        if (p.getInvoice().getPatient() != null) {
                            patName = (p.getInvoice().getPatient().getName() != null ? p.getInvoice().getPatient().getName() : "") + " " +
                                      (p.getInvoice().getPatient().getSurname() != null ? p.getInvoice().getPatient().getSurname() : "");
                        }
                        if (p.getInvoice().getDoctor() != null) {
                            docName = (p.getInvoice().getDoctor().getName() != null ? p.getInvoice().getDoctor().getName() : "") + " " +
                                      (p.getInvoice().getDoctor().getSurname() != null ? p.getInvoice().getDoctor().getSurname() : "");
                        }
                    }
                    return PaymentExcelResponse.builder()
                            .invoiceNumber(p.getInvoice() != null && p.getInvoice().getInvoiceNumber() != null ? p.getInvoice().getInvoiceNumber() : "")
                            .paymentDate(p.getPaymentDate() != null ? p.getPaymentDate().format(formatter) : "")
                            .patientName(patName.trim())
                            .doctorName(docName.trim())
                            .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod() : "")
                            .amount(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());

        return ExcelUtil.dataToExcel(excelData, PaymentExcelResponse.class);
    }

    private List<CollectionsChartItem> getChartData(List<Payment> payments, String period) {
        DateTimeFormatter formatter;
        if ("bu_hefte".equalsIgnoreCase(period)) {
            formatter = DateTimeFormatter.ofPattern("EEE", new Locale("az"));
        } else if ("bu_il".equalsIgnoreCase(period)) {
            formatter = DateTimeFormatter.ofPattern("MMM", new Locale("az"));
        } else {
            formatter = DateTimeFormatter.ofPattern("MMM dd", new Locale("az"));
        }

        Map<String, BigDecimal> grouped = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentDate().format(formatter),
                        TreeMap::new,
                        Collectors.mapping(
                                Payment::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return grouped.entrySet().stream()
                .map(e -> new CollectionsChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String trimmed = dateStr.trim();
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception ignored) {}

        String[] patterns = {"yyyy-MM-dd", "dd.MM.yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }

        try {
            long epochMilli = Long.parseLong(trimmed);
            return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ignored) {}

        return null;
    }

    private long[] getStartAndEndTimestamps(String period, String fromDateStr, String toDateStr) {
        LocalDate from = parseDateSafely(fromDateStr);
        LocalDate to = parseDateSafely(toDateStr);

        if (from != null || to != null) {
            if (from == null) from = LocalDate.now().minusDays(30);
            if (to == null) to = LocalDate.now();
            long start = from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long end = to.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return new long[]{start, end};
        }

        LocalDate now = LocalDate.now();
        long start;
        long end = now.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        switch (period != null ? period.toLowerCase() : "bu_ay") {
            case "all":
                start = Instant.EPOCH.toEpochMilli();
                break;
            case "bu_hefte":
                LocalDate monday = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                start = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                break;
            case "bu_il":
                LocalDate firstDayOfYear = now.with(java.time.temporal.TemporalAdjusters.firstDayOfYear());
                start = firstDayOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                break;
            case "bu_ay":
            default:
                LocalDate firstDayOfMonth = now.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
                start = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                break;
        }

        return new long[]{start, end};
    }

    private Specification<DentalOrder> buildLaboratoryOrderSpec(
            String period, String fromDateStr, String toDateStr,
            String status, String category, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            long[] range = getStartAndEndTimestamps(period, fromDateStr, toDateStr);
            LocalDate start = Instant.ofEpochMilli(range[0]).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = Instant.ofEpochMilli(range[1]).atZone(ZoneId.systemDefault()).toLocalDate();
            predicates.add(cb.between(root.get("checkDate"), start, end));

            if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
                try {
                    predicates.add(cb.equal(root.get("dentalWorkStatus"), com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus.valueOf(status.trim().toUpperCase())));
                } catch (Exception ignored) {}
            }

            if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category)) {
                try {
                    predicates.add(cb.equal(root.get("dentalWorkType"), com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType.valueOf(category.trim().toUpperCase())));
                } catch (Exception ignored) {}
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate patientName = cb.like(cb.lower(root.get("patient").get("name")), searchPattern);
                Predicate patientSurname = cb.like(cb.lower(root.get("patient").get("surname")), searchPattern);
                Predicate doctorName = cb.like(cb.lower(root.get("baseUser").get("name")), searchPattern);
                Predicate doctorSurname = cb.like(cb.lower(root.get("baseUser").get("surname")), searchPattern);
                Predicate techName = cb.like(cb.lower(root.get("technician").get("name")), searchPattern);
                Predicate techSurname = cb.like(cb.lower(root.get("technician").get("surname")), searchPattern);
                Predicate desc = cb.like(cb.lower(root.get("description")), searchPattern);

                predicates.add(cb.or(patientName, patientSurname, doctorName, doctorSurname, techName, techSurname, desc));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public LaboratoryReportResponse getLaboratoryReport(
            String period, String fromDateStr, String toDateStr,
            String status, String category, String search,
            int pageVal, int sizeVal) {

        Specification<DentalOrder> spec = buildLaboratoryOrderSpec(period, fromDateStr, toDateStr, status, category, search);

        List<DentalOrder> allMatched = dentalOrderRepository.findAll(spec);

        Pageable pageable = PageRequest.of(pageVal, sizeVal, Sort.by("checkDate").descending());
        Page<DentalOrder> page = dentalOrderRepository.findAll(spec, pageable);

        page.getContent().forEach(order -> {
            if (order.getToothDetails() != null) {
                order.getToothDetails().size();
            }
            if (order.getTeethList() != null) {
                order.getTeethList().size();
            }
        });

        long totalOrders = allMatched.size();
        BigDecimal totalAmount = allMatched.stream()
                .map(DentalOrder::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAmount = totalOrders > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long completedOrders = allMatched.stream()
                .filter(o -> o.getDentalWorkStatus() == com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus.READY
                        || o.getDentalWorkStatus() == com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus.RECEIVED_FROM_TECHNICIAN
                        || o.getDentalWorkStatus() == com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus.SENT_TO_DOCTOR)
                .count();

        Map<String, Long> statusBreakdown = allMatched.stream()
                .filter(o -> o.getDentalWorkStatus() != null)
                .collect(Collectors.groupingBy(o -> o.getDentalWorkStatus().name(), Collectors.counting()));

        Map<String, Long> categoryBreakdown = allMatched.stream()
                .filter(o -> o.getDentalWorkType() != null)
                .collect(Collectors.groupingBy(o -> o.getDentalWorkType().name(), Collectors.counting()));

        Map<String, Long> technicianBreakdown = allMatched.stream()
                .filter(o -> o.getTechnician() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getTechnician().getName() + " " + o.getTechnician().getSurname(),
                        Collectors.counting()
                ));

        List<CollectionsChartItem> timelineData = getLaboratoryTimelineData(allMatched, period);

        List<TechnicianOrderResponse> content = page.getContent().stream()
                .map(dentalOrderMapper::toResponse)
                .collect(Collectors.toList());

        return LaboratoryReportResponse.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .avgAmount(avgAmount)
                .completedOrders(completedOrders)
                .statusBreakdown(statusBreakdown)
                .categoryBreakdown(categoryBreakdown)
                .technicianBreakdown(technicianBreakdown)
                .timelineData(timelineData)
                .content(content)
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    private List<CollectionsChartItem> getLaboratoryTimelineData(List<DentalOrder> orders, String period) {
        DateTimeFormatter formatter;
        if ("bu_hefte".equalsIgnoreCase(period)) {
            formatter = DateTimeFormatter.ofPattern("EEE", new Locale("az"));
        } else if ("bu_il".equalsIgnoreCase(period)) {
            formatter = DateTimeFormatter.ofPattern("MMM", new Locale("az"));
        } else {
            formatter = DateTimeFormatter.ofPattern("MMM dd", new Locale("az"));
        }

        Map<String, Long> grouped = orders.stream()
                .filter(o -> o.getCheckDate() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getCheckDate().format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new CollectionsChartItem(e.getKey(), BigDecimal.valueOf(e.getValue())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportLaboratoryReportsExcel(
            String period, String fromDateStr, String toDateStr,
            String status, String category, String search) {

        Specification<DentalOrder> spec = buildLaboratoryOrderSpec(period, fromDateStr, toDateStr, status, category, search);
        List<DentalOrder> orders = dentalOrderRepository.findAll(spec);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        List<LaboratoryReportExcelResponse> excelData = orders.stream()
                .map(o -> LaboratoryReportExcelResponse.builder()
                        .id(o.getId())
                        .checkDate(o.getCheckDate() != null ? o.getCheckDate().format(formatter) : "")
                        .deliveryDate(o.getDeliveryDate() != null ? o.getDeliveryDate().format(formatter) : "")
                        .doctorName(o.getBaseUser() != null ? (o.getBaseUser().getName() + " " + o.getBaseUser().getSurname()) : "")
                        .technicianName(o.getTechnician() != null ? (o.getTechnician().getName() + " " + o.getTechnician().getSurname()) : "")
                        .patientName(o.getPatient() != null ? (o.getPatient().getName() + " " + o.getPatient().getSurname()) : "")
                        .dentalWorkType(o.getDentalWorkType() != null ? o.getDentalWorkType().name() : "")
                        .dentalWorkStatus(o.getDentalWorkStatus() != null ? o.getDentalWorkStatus().name() : "")
                        .price(o.getPrice() != null ? o.getPrice() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        return ExcelUtil.dataToExcel(excelData, LaboratoryReportExcelResponse.class);
    }

    private String resolveDoctorName(String input) {
        if (input == null || input.trim().isEmpty()) return "-";
        try {
            UUID uuid = UUID.fromString(input);
            Optional<BaseUser> userOpt = baseUserRepository.findById(uuid);
            if (userOpt.isPresent()) {
                BaseUser user = userOpt.get();
                return user.getName() + " " + user.getSurname();
            }
        } catch (IllegalArgumentException e) {
            // Not a UUID, return as-is
            return input;
        }
        return input;
    }
}
