package com.rustam.modern_dentistry.service.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientReportRepository;
import com.rustam.modern_dentistry.dto.request.criteria.PatientReportCriteria;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import com.rustam.modern_dentistry.util.specification.patientinfo.PatientReportSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientReportReadService {

    PatientReportRepository patientReportRepository;

    @Transactional(readOnly = true)
    public Page<PatientReportReadResponse> read(PatientReportCriteria criteria, Pageable pageable) {

        Specification<PatientReport> spec = PatientReportSpecification.getReports(criteria);

        Page<PatientReport> reportPage = patientReportRepository.findAll(spec, pageable);

        return reportPage.map(report -> PatientReportReadResponse.builder()
                .planDate(report.getPlanDate())
                .patientName(report.getPatientName())
                .teethNo(report.getTeethNo())
                .operationName(report.getOperationName())
                .planningDoctorName(report.getPlanningDoctorName())
                .price(report.getPrice())
                .discount(report.getDiscount())
                .finalPrice(report.getFinalPrice())
                .executionDate(report.getExecutionDate())
                .executionDoctorName(report.getExecutionDoctorName())
                .build());
    }
}