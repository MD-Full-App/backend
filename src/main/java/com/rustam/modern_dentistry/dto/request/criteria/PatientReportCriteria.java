package com.rustam.modern_dentistry.dto.request.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientReportCriteria {
    String operationName;
    Long teethNo;
    Long startDate;
    Long endDate;
}