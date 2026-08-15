package com.rustam.modern_dentistry.dto.request;

import com.rustam.modern_dentistry.dao.entity.laboratory.OrderDentureInfo;
import com.rustam.modern_dentistry.dto.request.create.DentalOrderToothDetailIds;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.rustam.modern_dentistry.util.constants.ValidationErrorMessage.*;

@Getter
@Setter
public class DentalOrderCreateReq {
    @NotNull(message = VALIDATION_DATE)
    LocalDate checkDate;
    @NotNull(message = VALIDATION_DATE)
    LocalDate deliveryDate;

    String description;

    @NotNull(message = VALIDATION_LAB_DENTAL_WORK_TYPE)
    String dentalWorkType;

    OrderDentureInfo orderDentureInfo;

    List<DentalOrderToothDetailIds> toothDetailIds;
    List<Long> teethList;

    Boolean isBridge;
    Integer startTooth;
    Integer endTooth;

    @NotNull(message = VALIDATION_DOCTOR_ID)
    String doctorId;
    @NotNull(message = VALIDATION_TECHNICIAN_ID)
    UUID technicianId;
    @NotNull(message = VALIDATION_PATIENT_ID)
    Long patientId;

    List<String> files;

}
