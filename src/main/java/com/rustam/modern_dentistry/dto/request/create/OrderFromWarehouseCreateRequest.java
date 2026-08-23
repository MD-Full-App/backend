package com.rustam.modern_dentistry.dto.request.create;

import com.rustam.modern_dentistry.dao.entity.enums.status.Room;
import com.rustam.modern_dentistry.dto.request.read.OrderFromWarehouseProductRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFromWarehouseCreateRequest {

    @NotNull
    LocalDate date;

    @NotNull
    LocalTime time;

    @NotNull
    String cabinetName;

    String personWhoPlacedOrder;

    List<OrderFromWarehouseProductRequest> orderFromWarehouseProductRequests;

    String description;
}
