package com.rustam.modern_dentistry.dao.repository.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    List<Payment> findAllByPaymentDateBetween(LocalDate start, LocalDate end);
}
