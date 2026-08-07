package com.rustam.modern_dentistry.dto.response.read;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@Builder
@FieldDefaults(level = PRIVATE)
public class DentalOrderToothDetailResponse {
    Long colorId;
    String colorName;
    Long metalId;
    String metalName;
    Long ceramicId;
    String ceramicName;
    /**
     * Dişin hissəsi: "CROWN", "MIDDLE", "GUM" — optional (köhnə qeydlər üçün null)
     */
    String toothSection;
}

