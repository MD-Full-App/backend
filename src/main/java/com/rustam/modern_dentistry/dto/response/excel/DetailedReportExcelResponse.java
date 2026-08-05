package com.rustam.modern_dentistry.dto.response.excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedReportExcelResponse {

    @JsonProperty("Plan tarixi")
    private String planDate;

    @JsonProperty("Pasiyent")
    private String patientName;

    @JsonProperty("Diş №")
    private String teethNo;

    @JsonProperty("Əməliyyat")
    private String operationName;

    @JsonProperty("Planlayan həkim")
    private String planningDoctorName;

    @JsonProperty("Qiymət (AZN)")
    private BigDecimal price;

    @JsonProperty("Endirim (AZN)")
    private BigDecimal discount;

    @JsonProperty("Yekun (AZN)")
    private BigDecimal finalPrice;

    @JsonProperty("İcra tarixi")
    private String executionDate;

    @JsonProperty("İcraçı həkim")
    private String executionDoctorName;
}
