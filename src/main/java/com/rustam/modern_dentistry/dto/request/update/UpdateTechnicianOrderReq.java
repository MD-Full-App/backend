package com.rustam.modern_dentistry.dto.request.update;

import com.rustam.modern_dentistry.dao.entity.laboratory.OrderDentureInfo;
import com.rustam.modern_dentistry.dto.request.create.DentalOrderToothDetailIds;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@Builder
@FieldDefaults(level = PRIVATE)
public class UpdateTechnicianOrderReq {
    Long id;
    LocalDate checkDate;
    LocalDate deliveryDate;
    String description;

    String orderType;
    // String orderStatus; Auto set PENDING in mapper

    OrderDentureInfo orderDentureInfo;

    List<DentalOrderToothDetailIds> toothDetailIds;
    List<Long> teethList;

    Boolean isBridge;
    Integer startTooth;
    Integer endTooth;

    String doctorId;
    UUID technicianId;
    Long patientId;

    List<String> files;

    List<String> deleteFiles;
}
