package com.rustam.modern_dentistry.dao.entity.laboratory;

import com.rustam.modern_dentistry.dao.entity.settings.Ceramic;
import com.rustam.modern_dentistry.dao.entity.settings.Color;
import com.rustam.modern_dentistry.dao.entity.settings.Metal;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dental_order_tooth_detail")
public class DentalOrderToothDetail {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;

    @ManyToOne(fetch = LAZY)
    Color color;

    @ManyToOne(fetch = LAZY)
    Metal metal;

    @ManyToOne(fetch = LAZY)
    Ceramic ceramic;

    /**
     * Dişin hansı hissəsinə aid rəng: CROWN (üst/tac), MIDDLE (orta), GUM (diş eti)
     * Optional — mövcud qeydlər üçün null ola bilər.
     */
    @Column(name = "tooth_section")
    String toothSection;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "dental_order_id", referencedColumnName = "id")
    DentalOrder dentalOrder;
}
