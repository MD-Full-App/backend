package com.rustam.modern_dentistry.dao.entity.settings.teeth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rustam.modern_dentistry.dao.entity.enums.status.ToothLocation;
import com.rustam.modern_dentistry.dao.entity.enums.status.ToothType;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teeth")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Teeth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long toothNo;

    @Enumerated(EnumType.STRING)
    ToothType toothType;

    @Enumerated(EnumType.STRING)
    ToothLocation toothLocation;

    @Builder.Default
    @OneToMany(mappedBy = "teeth", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<TeethExamination> toothExaminations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "teeth", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<TeethOperation> toothOperations = new ArrayList<>();

    @ManyToMany(mappedBy = "teethList")
    @JsonIgnore
    List<DentalOrder> orders;
}
