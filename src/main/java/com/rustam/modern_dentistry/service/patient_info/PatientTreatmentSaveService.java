package com.rustam.modern_dentistry.service.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dao.entity.patient_info.patienttreatment.PatientTreatment;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientReportRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientTreatmentRepository;
import com.rustam.modern_dentistry.dto.request.save.PatientTreatmentSaveRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientTreatmentSaveService {

    PatientTreatmentRepository patientTreatmentRepository;
    PatientReportRepository patientReportRepository;

    @Transactional
    public void save(PatientTreatmentSaveRequest req) {
        List<PatientTreatment> patientTreatments = patientTreatmentRepository
                .findByIdInAndIsCheckedAndStatusInAndActionStatusIn(
                        req.getCheckedPlanIds(),
                        true,
                        List.of("A", "C"),
                        List.of("A", "C")
                );

        patientTreatments.forEach(patientTreatment -> {
            patientTreatment.setStatus("A");
            patientTreatment.setActionStatus("A");

            if (patientTreatment.getExecutedPlans() != null) {
                patientTreatment.getExecutedPlans().forEach(plan -> {
                    if (plan.getOpType() != null && plan.getOpType().getOpTypeItems() != null) {
                        plan.getOpType().getOpTypeItems().forEach(item -> {

                            BigDecimal basePrice = (item.getPrice() != null) ? item.getPrice().getPrice() : BigDecimal.ZERO;
                            BigDecimal finalPrice = patientTreatment.getTotalPrice() != null ? patientTreatment.getTotalPrice() : BigDecimal.ZERO;
                            BigDecimal discount = basePrice.subtract(finalPrice);

                            PatientReport report = PatientReport.builder()
                                    .patientId(patientTreatment.getPatientPlanMain() != null
                                            && patientTreatment.getPatientPlanMain().getPatient() != null
                                            ? patientTreatment.getPatientPlanMain().getPatient().getId() : null)
                                    .planDate(plan.getCreatedDate())
                                    .patientName(patientTreatment.getPatientPlanMain() != null && patientTreatment.getPatientPlanMain().getPatient() != null
                                            ? patientTreatment.getPatientPlanMain().getPatient().getName() + " "
                                            + patientTreatment.getPatientPlanMain().getPatient().getSurname() : null)
                                    .teethNo(plan.getToothId())
                                    .operationName(item.getOperationName())
                                    .planningDoctorName(plan.getCreatedBy())
                                    .price(basePrice)
                                    .discount(discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO)
                                    .finalPrice(finalPrice)
                                    .executionDate(Instant.now().toEpochMilli())
                                    .executionDoctorName(patientTreatment.getCreatedBy())
                                    .build();

                            patientReportRepository.save(report);
                        });
                    }
                });
            }
        });

        patientTreatmentRepository.saveAll(patientTreatments);
    }
}