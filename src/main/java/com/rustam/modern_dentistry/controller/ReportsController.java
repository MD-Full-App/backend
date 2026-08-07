package com.rustam.modern_dentistry.controller;

import com.rustam.modern_dentistry.dto.request.criteria.DetailedReportCriteria;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import com.rustam.modern_dentistry.dto.response.reports.DashboardReportResponse;
import com.rustam.modern_dentistry.dto.response.reports.DetailedReportsResponse;
import com.rustam.modern_dentistry.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportResponse> getDashboard(
            @RequestParam(value = "period", defaultValue = "bu_ay") String period,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {
        return new ResponseEntity<>(reportsService.getDashboardReport(period, fromDate, toDate), HttpStatus.OK);
    }

    @PostMapping("/detailed")
    public ResponseEntity<DetailedReportsResponse> getDetailedReports(
            @RequestBody DetailedReportCriteria criteria,
            Pageable pageable) {
        return new ResponseEntity<>(reportsService.getDetailedReports(criteria, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/export/detailed", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Resource> exportDetailed(
            @RequestBody(required = false) DetailedReportCriteria criteria,
            @RequestParam(value = "plannerDoctor", required = false) String plannerDoctor,
            @RequestParam(value = "executorDoctor", required = false) String executorDoctor,
            @RequestParam(value = "operationName", required = false) String operationName,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate", required = false) Long startDate,
            @RequestParam(value = "endDate", required = false) Long endDate) {
        if (criteria == null) {
            criteria = new DetailedReportCriteria(plannerDoctor, executorDoctor, operationName, category, startDate, endDate);
        }
        ByteArrayInputStream in = reportsService.exportDetailedReportsExcel(criteria);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=detailed_reports.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @RequestMapping(value = "/export/payments", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Resource> exportPayments(
            @RequestParam(value = "period", defaultValue = "bu_ay") String period,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {
        ByteArrayInputStream in = reportsService.exportPaymentsExcel(period, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=payments_report.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/laboratory")
    public ResponseEntity<com.rustam.modern_dentistry.dto.response.reports.LaboratoryReportResponse> getLaboratoryReport(
            @RequestParam(value = "period", defaultValue = "bu_ay") String period,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(reportsService.getLaboratoryReport(period, fromDate, toDate, status, category, search, page, size));
    }

    @RequestMapping(value = "/export/laboratory", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Resource> exportLaboratory(
            @RequestParam(value = "period", defaultValue = "bu_ay") String period,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search) {
        ByteArrayInputStream in = reportsService.exportLaboratoryReportsExcel(period, fromDate, toDate, status, category, search);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=laboratory_reports.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
