package com.rustam.modern_dentistry.dao.repository.patient_info;


import com.rustam.modern_dentistry.dao.entity.patient_info.patienttreatment.PatientTreatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientTreatmentRepository extends JpaRepository<PatientTreatment, Long> {
    List<PatientTreatment> findByExecutedPlans_IdInAndIsCheckedAndStatusInAndActionStatusIn(
        List<UUID> ids,
        boolean isChecked,
        List<String> statuses,
        List<String> actionStatuses
    );
}