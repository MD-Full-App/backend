package com.rustam.modern_dentistry.dto.request.create;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DentalOrderToothDetailIds {
    Long colorId;
    Long metalId;
    Long ceramicId;
    /**
     * Dişin hissəsi: "CROWN", "MIDDLE", "GUM" — optional
     */
    String toothSection;
}
