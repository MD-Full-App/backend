package com.rustam.modern_dentistry.dao.entity.patient_info.insurance;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.settings.InsuranceCompany;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "patient_insurance ")
public class PatientInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String policyNumber;
    LocalDate expirationDate;
    BigDecimal deductibleAmount;
    BigDecimal annualMaxAmount;

    @Enumerated(STRING)
    Status status;

    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "insurance_company_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    InsuranceCompany insuranceCompany;

    @OneToMany(mappedBy = "patientInsurance", cascade = CascadeType.REMOVE)
    List<PatientInsuranceBalance> balances;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}
