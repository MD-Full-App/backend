package com.rustam.modern_dentistry.dao.entity.warehouse_operations;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rustam.modern_dentistry.dao.entity.enums.status.PendingStatus;
import com.rustam.modern_dentistry.dao.entity.enums.status.Room;
import com.rustam.modern_dentistry.dao.entity.settings.Cabinet;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouse_receipts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseReceipts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate date;

    LocalTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cabinet_id")
    Cabinet cabinet;

    String personWhoPlacedOrder;

    Long orderQuantity;

    Long sendQuantity;

    @Enumerated(EnumType.STRING)
    PendingStatus pendingStatus;

    String groupId;

    @Builder.Default
    @OneToMany(mappedBy = "warehouseReceipts", cascade = CascadeType.ALL)
    List<WarehouseRemovalProduct> warehouseRemovalProducts = new ArrayList<>();

}
