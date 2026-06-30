package com.rustam.modern_dentistry.dto.response.read;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientReportReadResponse {

    Long planDate;
    String patientName;
    Long teethNo;
    String operationName;
    String planningDoctorName;
    BigDecimal price;
    BigDecimal discount;
    BigDecimal finalPrice;
    Long executionDate;
    String executionDoctorName;
}
