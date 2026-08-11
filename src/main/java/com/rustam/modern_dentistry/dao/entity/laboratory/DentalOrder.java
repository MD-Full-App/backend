package com.rustam.modern_dentistry.dao.entity.laboratory;

import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus;
import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType;
import com.rustam.modern_dentistry.dao.entity.settings.teeth.Teeth;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dao.entity.users.Technician;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dental_order")
public class DentalOrder {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;
    LocalDate checkDate;
    LocalDate deliveryDate;
    String description;
    BigDecimal price;

    Boolean isBridge;
    Integer startTooth;
    Integer endTooth;

    @Embedded
    OrderDentureInfo orderDentureInfo;

    @Enumerated(STRING)
    DentalWorkType dentalWorkType;

    @Enumerated(STRING)
    DentalWorkStatus dentalWorkStatus;

    @OneToMany(mappedBy = "dentalOrder", cascade = ALL, orphanRemoval = true)
    List<DentalOrderToothDetail> toothDetails;

    @ManyToMany
    @JoinTable(
            name = "dental_order_teeth",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "tooth_id")
    )
    List<Teeth> teethList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_user_id")
    BaseUser baseUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    Patient patient;

    @ElementCollection
    @CollectionTable(
            name = "dental_order_image_paths",
            joinColumns = @JoinColumn(name = "dental_order_id")
    )
    @Column(name = "image_path")
    List<String> imagePaths;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DentalOrder that = (DentalOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
