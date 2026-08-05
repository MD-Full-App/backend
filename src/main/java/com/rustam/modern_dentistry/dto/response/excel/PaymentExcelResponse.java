package com.rustam.modern_dentistry.dto.response.excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentExcelResponse {

    @JsonProperty("Faktura No")
    private String invoiceNumber;

    @JsonProperty("Tarix")
    private String paymentDate;

    @JsonProperty("Pasiyent")
    private String patientName;

    @JsonProperty("Həkim")
    private String doctorName;

    @JsonProperty("Ödəniş üsulu")
    private String paymentMethod;

    @JsonProperty("Məbləğ (AZN)")
    private BigDecimal amount;
}
