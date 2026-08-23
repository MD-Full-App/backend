package com.rustam.modern_dentistry.dao.repository;

import com.rustam.modern_dentistry.dao.entity.users.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    long countByRegistrationDateBetween(LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"reservations", "generalCalendars", "examinations"})
    Optional<Patient> findById(Long id);

    @EntityGraph(attributePaths = {"baseUser", "priceCategory", "specializationCategory", "patientBlacklist"})
    List<Patient> findAll();
    
    boolean existsByEmail(String email);

    boolean existsByFinCode(String finCode);
}
