package com.rustam.modern_dentistry.dao.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
@SuperBuilder
@MappedSuperclass
@FieldNameConstants
public class CoreEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    UUID id;

    @Column(name = "created_by")
    String createdBy;
    @Column(name = "updated_by")
    String updatedBy;
    @Column(name = "deleted_by")
    String deletedBy;
    @Column(name = "created_date")
    Long createdDate;
    @Column(name = "updated_date")
    Long updatedDate;
    @Column(name = "deleted_date")
    Long deletedDate;
    @Column(name = "status")
    String status;
    @Column(name = "action_status")
    String actionStatus;

}
