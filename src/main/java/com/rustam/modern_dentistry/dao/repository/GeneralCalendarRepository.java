package com.rustam.modern_dentistry.dao.repository;


import com.rustam.modern_dentistry.dao.entity.GeneralCalendar;
import com.rustam.modern_dentistry.dao.entity.enums.status.Appointment;
import com.rustam.modern_dentistry.dao.entity.enums.status.Room;
import com.rustam.modern_dentistry.dto.response.read.SelectingDoctorViewingPatientResponse;
import com.rustam.modern_dentistry.dto.response.read.SelectingPatientToReadResponse;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneralCalendarRepository extends JpaRepository<GeneralCalendar,Long> {

    long countByDateBetween(LocalDate start, LocalDate end);

    long countByDateBetweenAndAppointment(LocalDate start, LocalDate end, Appointment appointment);
    @Query("""
    SELECT new com.rustam.modern_dentistry.dto.response.read.SelectingDoctorViewingPatientResponse(
        g.patient.name, g.appointment, g.date, g.time, g.period, g.cabinet.cabinetName
    )
    FROM GeneralCalendar g
    JOIN g.patient p
    WHERE g.baseUser.id = :doctorId
""")
    List<SelectingDoctorViewingPatientResponse> findAllByDoctorId(UUID doctorId);

    @Query("SELECT new com.rustam.modern_dentistry.dto.response.read.SelectingPatientToReadResponse(" +
            "d.name, p.name, g.time, g.date ,g.cabinet.cabinetName, g.appointment) " +
            "FROM GeneralCalendar g " +
            "JOIN g.baseUser d " +
            "JOIN g.patient p " +
            "WHERE g.patient.id = :patientId AND g.appointment IN ('MEETING', 'CAME')")
    Optional<SelectingPatientToReadResponse> findByPatientId(Long patientId);

    @Query("""
    SELECT CASE 
        WHEN COUNT(g) > 0 AND MAX(g.appointment) = 'MEETING' THEN FALSE
        WHEN COUNT(g) > 0 AND MAX(g.appointment) IN ('CAME', 'CANCELED') THEN TRUE
        ELSE TRUE
    END 
    FROM GeneralCalendar g 
    WHERE g.patient.id = :id
""")
    boolean existsActivePatientById(Long id);

    @Query("""
    SELECT new com.rustam.modern_dentistry.dto.response.read.SelectingDoctorViewingPatientResponse(
        g.patient.name, g.appointment, g.date, g.time, g.period, g.cabinet.cabinetName
    )
    FROM GeneralCalendar g
    JOIN g.patient p
    WHERE g.cabinet.cabinetName = :cabinetName
""")
    List<SelectingDoctorViewingPatientResponse> findAllCabinetName(String cabinetName);

    @Transactional
    @Modifying
    @Query("DELETE FROM GeneralCalendar g WHERE g.patient.id = :patientId AND g.time = :time")
    GeneralCalendar deleteByPatientIdAndTime(Long patientId,LocalTime time);
}
