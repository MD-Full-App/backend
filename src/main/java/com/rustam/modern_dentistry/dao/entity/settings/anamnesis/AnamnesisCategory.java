package com.rustam.modern_dentistry.dao.entity.settings.anamnesis;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anamnesis_categories")
@FieldDefaults(level = PRIVATE)
public class AnamnesisCategory {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;
    Status status;

    @Builder.Default
    @OneToMany(
            cascade = ALL,
            mappedBy = "anamnesisCategory",
            orphanRemoval = true
    )
    List<AnamnesisList> anamnesisList =  new ArrayList<>();
}
