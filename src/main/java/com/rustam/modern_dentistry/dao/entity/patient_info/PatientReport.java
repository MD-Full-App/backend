package com.rustam.modern_dentistry.dao.entity.patient_info;

import com.rustam.modern_dentistry.dao.entity.CoreEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "patient_report")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class PatientReport extends CoreEntity {

    @Column(name = "plan_date")
    Long planDate;
    @Column(name = "patient_name")
    String patientName;
    @Column(name = "teeth_no")
    Long teethNo;
    @Column(name = "operation_name")
    String operationName;
    @Column(name = "planning_doctor_name")
    String planningDoctorName;
    @Column(name = "price")
    BigDecimal price;
    @Column(name = "discount")
    BigDecimal discount;
    @Column(name = "final_price")
    BigDecimal finalPrice;
    @Column(name = "execution_date")
    Long executionDate;
    @Column(name = "execution_doctor_name")
    String executionDoctorName;
}
