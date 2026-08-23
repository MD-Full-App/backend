package com.rustam.modern_dentistry.dao.repository;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientExaminations;
import com.rustam.modern_dentistry.dto.response.read.PatientExaminationsResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethOperationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PatientExaminationsRepository extends JpaRepository<PatientExaminations, Long> {
    @Query("SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false END FROM PatientExaminations pe " +
            "WHERE pe.patient.id = :patientId AND pe.toothNumber = :toothNumber AND pe.diagnosis = :diagnosis")
    boolean existsPatientExaminationsByPatientAndToothNumberAndDiagnosis(Long patientId, Long toothNumber,String diagnosis);

    List<PatientExaminations> findByPatient_Id(Long patientId);

    @Query("SELECT new com.rustam.modern_dentistry.dto.response.read.PatientExaminationsResponse(p.id, p.toothNumber,p.diagnosis,d.name) " +
            "FROM PatientExaminations p JOIN BaseUser d")
    List<PatientExaminationsResponse> findAllPatientExaminations();

    @Query("""
    SELECT new com.rustam.modern_dentistry.dto.response.read.PatientExaminationsResponse(
        pe.id, pe.toothNumber, pe.diagnosis, d.name
    )
    FROM PatientExaminations pe
    JOIN BaseUser d ON pe.doctorId = d.id
    WHERE pe.patientAppointmentDate = :date
""")
    List<PatientExaminationsResponse> findByAppointmentDate(LocalDate date);
}
