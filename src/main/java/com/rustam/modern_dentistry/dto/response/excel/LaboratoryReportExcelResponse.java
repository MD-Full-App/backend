package com.rustam.modern_dentistry.dto.response.excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratoryReportExcelResponse {

    @JsonProperty("Sifariş №")
    private Long id;

    @JsonProperty("Giriş tarixi")
    private String checkDate;

    @JsonProperty("Təhvil tarixi")
    private String deliveryDate;

    @JsonProperty("Həkim")
    private String doctorName;

    @JsonProperty("Texnik")
    private String technicianName;

    @JsonProperty("Pasiyent")
    private String patientName;

    @JsonProperty("İşin növü")
    private String dentalWorkType;

    @JsonProperty("Status")
    private String dentalWorkStatus;

    @JsonProperty("Məbləğ (AZN)")
    private BigDecimal price;
}
