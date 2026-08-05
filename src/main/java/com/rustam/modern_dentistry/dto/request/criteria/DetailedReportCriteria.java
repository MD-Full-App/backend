package com.rustam.modern_dentistry.dto.request.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailedReportCriteria {
    String plannerDoctor;
    String executorDoctor;
    String operationName;
    String category;
    Long startDate;
    Long endDate;
}
