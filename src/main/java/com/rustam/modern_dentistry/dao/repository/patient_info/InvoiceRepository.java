package com.rustam.modern_dentistry.dao.repository.patient_info;

import com.rustam.modern_dentistry.dao.entity.patient_info.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    List<Invoice> findAllByIssueDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.invoiceStatus <> 'PAID'")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.invoiceStatus <> 'PAID' AND i.issueDate BETWEEN :start AND :end")
    List<Invoice> findOverdueInvoicesInPeriod(@Param("currentDate") LocalDate currentDate, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
