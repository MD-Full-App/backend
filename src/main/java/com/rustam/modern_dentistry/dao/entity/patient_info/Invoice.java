package com.rustam.modern_dentistry.dao.entity.patient_info;

import com.rustam.modern_dentistry.dao.entity.CoreEntity;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "invoices")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class Invoice extends CoreEntity {

    @Column(name = "invoice_number", unique = true, nullable = false)
    String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    BaseUser doctor;

    @Column(name = "issue_date", nullable = false)
    LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    LocalDate dueDate;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Column(name = "vat_rate", nullable = false)
    BigDecimal vatRate;

    @Column(name = "vat_amount", nullable = false)
    BigDecimal vatAmount;

    @Column(name = "invoice_status", nullable = false)
    String invoiceStatus;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    List<Payment> payments;
}
