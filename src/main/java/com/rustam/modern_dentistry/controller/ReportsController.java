package com.rustam.modern_dentistry.controller;

import com.rustam.modern_dentistry.dto.request.criteria.DetailedReportCriteria;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import com.rustam.modern_dentistry.dto.response.reports.DashboardReportResponse;
import com.rustam.modern_dentistry.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<PatientReportReadResponse>> getDetailedReports(
            @RequestBody DetailedReportCriteria criteria,
            Pageable pageable) {
        return new ResponseEntity<>(reportsService.getDetailedReports(criteria, pageable), HttpStatus.OK);
    }
}
