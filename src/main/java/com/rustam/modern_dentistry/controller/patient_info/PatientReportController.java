package com.rustam.modern_dentistry.controller.patient_info;

import com.rustam.modern_dentistry.dto.request.criteria.PatientReportCriteria;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import com.rustam.modern_dentistry.service.patient_info.PatientReportReadService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patient-report")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PatientReportController {

    PatientReportReadService patientReportReadService;

    @PostMapping(path = "/read")
    public ResponseEntity<Page<PatientReportReadResponse>> read(
            PatientReportCriteria criteria,
            Pageable pageable) {
        return new ResponseEntity<>(patientReportReadService.read(criteria, pageable), HttpStatus.OK);
    }
}
