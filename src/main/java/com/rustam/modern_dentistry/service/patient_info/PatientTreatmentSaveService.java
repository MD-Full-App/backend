package com.rustam.modern_dentistry.service.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.Invoice;
import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dao.entity.patient_info.Payment;
import com.rustam.modern_dentistry.dao.entity.patient_info.patienttreatment.PatientTreatment;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dao.repository.BaseUserRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.InvoiceRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientReportRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientTreatmentRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PaymentRepository;
import com.rustam.modern_dentistry.dto.request.save.PatientTreatmentSaveRequest;
import com.rustam.modern_dentistry.util.UtilService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientTreatmentSaveService {

    PatientTreatmentRepository patientTreatmentRepository;
    PatientReportRepository patientReportRepository;
    InvoiceRepository invoiceRepository;
    PaymentRepository paymentRepository;
    BaseUserRepository baseUserRepository;
    UtilService utilService;

    @Transactional
    public void save(PatientTreatmentSaveRequest req) {
        List<PatientTreatment> patientTreatments = patientTreatmentRepository
                .findByExecutedPlans_IdInAndIsCheckedAndStatusInAndActionStatusIn(
                        req.getCheckedPlanIds(),
                        true,
                        List.of("A", "C"),
                        List.of("A", "C")
                );

        BigDecimal treatmentTotal = BigDecimal.ZERO;
        Patient currentPatient = null;
        BaseUser currentDoctor = null;

        for (PatientTreatment patientTreatment : patientTreatments) {
            patientTreatment.setStatus("A");
            patientTreatment.setActionStatus("A");

            if (patientTreatment.getPatientPlanMain() != null && patientTreatment.getPatientPlanMain().getPatient() != null) {
                currentPatient = patientTreatment.getPatientPlanMain().getPatient();
            }

            if (patientTreatment.getCreatedBy() != null) {
                try {
                    UUID docUuid = UUID.fromString(patientTreatment.getCreatedBy());
                    currentDoctor = baseUserRepository.findById(docUuid).orElse(null);
                } catch (Exception ignored) {}
            }

            if (patientTreatment.getExecutedPlans() != null) {
                for (var plan : patientTreatment.getExecutedPlans()) {
                    if (plan.getDetails() != null) {
                        for (var detail : plan.getDetails()) {
                            var item = detail.getOpTypeItem();
                            if (item != null) {
                                BigDecimal basePrice = (item.getAmount() != null) ? item.getAmount() : BigDecimal.ZERO;
                                BigDecimal finalPrice = patientTreatment.getTotalPrice() != null ? patientTreatment.getTotalPrice() : basePrice;
                                BigDecimal discount = basePrice.subtract(finalPrice);
                                treatmentTotal = treatmentTotal.add(finalPrice);

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
                            }
                        }
                    }
                }
            }
        }

        patientTreatmentRepository.saveAll(patientTreatments);

        // Automatically create Invoice and Payment if there is a positive amount
        if (treatmentTotal.compareTo(BigDecimal.ZERO) > 0 && currentPatient != null) {
            String invNum = "FAC-" + LocalDate.now().getYear() + "-" + String.format("%04d", (int)(Math.random() * 9000 + 1000));
            Invoice invoice = Invoice.builder()
                    .invoiceNumber(invNum)
                    .patient(currentPatient)
                    .doctor(currentDoctor)
                    .issueDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(10))
                    .totalAmount(treatmentTotal)
                    .vatRate(BigDecimal.ZERO)
                    .vatAmount(BigDecimal.ZERO)
                    .invoiceStatus("PAID")
                    .status("A")
                    .actionStatus("A")
                    .build();
            invoice = invoiceRepository.save(invoice);

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amount(treatmentTotal)
                    .paymentMethod("Kart")
                    .paymentDate(LocalDate.now())
                    .status("A")
                    .actionStatus("A")
                    .build();
            paymentRepository.save(payment);
        }
    }
}