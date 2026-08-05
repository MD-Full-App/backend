package com.rustam.modern_dentistry.dao.entity.patient_info;

import com.rustam.modern_dentistry.dao.entity.CoreEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class Payment extends CoreEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    @Column(name = "payment_date", nullable = false)
    LocalDate paymentDate;
}
