package com.rustam.modern_dentistry.dto.response.read;

import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus;
import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType;
import com.rustam.modern_dentistry.dao.entity.laboratory.OrderDentureInfo;
import jakarta.persistence.Embedded;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class TechnicianOrderResponse {
    Long id;
    LocalDate checkDate;
    LocalDate deliveryDate;
    String description;
    BigDecimal price;

    @Embedded
    OrderDentureInfo orderDentureInfo;

    DentalWorkType dentalWorkType;
    DentalWorkStatus dentalWorkStatus;

    List<DentalOrderToothDetailResponse> toothDetails;
    List<DentalOrderTeethListResponse> teethList;

    Boolean isBridge;
    Integer startTooth;
    Integer endTooth;

    String doctor;
    String technician;
    String patient;

    List<String> urls;
}
