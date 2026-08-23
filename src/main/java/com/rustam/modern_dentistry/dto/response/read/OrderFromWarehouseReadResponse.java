package com.rustam.modern_dentistry.dto.response.read;

import com.rustam.modern_dentistry.dao.entity.enums.status.Room;
import com.rustam.modern_dentistry.dao.entity.settings.Cabinet;
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
public class OrderFromWarehouseReadResponse {

    Long id;

    LocalDate date;

    LocalTime time;

    String cabinetName;

    String description;

    String personWhoPlacedOrder;

    Integer number;

    Long sumQuantity;

    com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus pendingStatus;

    Long sendAmount;

    Long incomingQuantity;
}
