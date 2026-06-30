package com.rustam.modern_dentistry.dao.entity.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rustam.modern_dentistry.dao.entity.WorkersWorkSchedule;
import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "base_users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"permissions", "workSchedules"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseUser {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    UUID id;
    String name;
    String surname;
    @Column(name = "phone")
    String phone;
    @Column(name = "phone2")
    String phone2;
    @Column(name = "phone3")
    String phone3;
    @Column(name = "home_phone")
    String homePhone;
    String email;
    @Column(unique = true, name = "fin_code")
    String finCode;
    String username;
    String patronymic;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender_status")
    GenderStatus genderStatus;
    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;
    String password;
    Boolean enabled;
    @Column(name = "color_code")
    String colorCode;
    String degree;
    String address;
    Integer experience;
    @Column(name = "profile_photo_path")
    String profilePhotoPath;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "authorities",
            joinColumns = @JoinColumn(name = "base_user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonIgnore
    Set<Permission> permissions;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL,orphanRemoval = true ,fetch = LAZY)
    Set<WorkersWorkSchedule> workSchedules;

    public String getId() {
        if (id != null) {
            return id.toString();
        }
        return null;
    }
}
