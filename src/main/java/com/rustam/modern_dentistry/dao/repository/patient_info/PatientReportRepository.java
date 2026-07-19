package com.rustam.modern_dentistry.dao.repository.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PatientReportRepository extends JpaRepository<PatientReport, UUID>, JpaSpecificationExecutor<PatientReport> {

    java.util.List<PatientReport> findAllByExecutionDateBetween(Long start, Long end);
    java.util.List<PatientReport> findAllByPlanDateBetween(Long start, Long end);

    @Query("SELECT new com.rustam.modern_dentistry.dto.response.read.PatientReportReadResponse(" +
            "  r.planDate, " +
            "  r.patientName, " +
            "  r.teethNo, " +
            "  r.operationName, " +
            "  r.planningDoctorName, " +
            "  r.price, " +
            "  r.discount, " +
            "  r.finalPrice, " +
            "  r.executionDate, " +
            "  r.executionDoctorName" +
            ") " +
            "FROM PatientReport r " +
            "WHERE (:operationName IS NULL OR r.operationName LIKE CONCAT('%', :operationName, '%')) " +
            "AND (:teethNo IS NULL OR r.teethNo = :teethNo) " +
            "AND (:startDate IS NULL OR r.executionDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.executionDate <= :endDate)")
    Page<PatientReportReadResponse> fetchPatientReports(
            String operationName,
            Long teethNo,
            Long startDate,
            Long endDate,
            Pageable pageable);
}