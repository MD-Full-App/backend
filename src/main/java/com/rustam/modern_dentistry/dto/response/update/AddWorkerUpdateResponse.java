package com.rustam.modern_dentistry.dto.response.update;

import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddWorkerUpdateResponse {
    String username;
    String password;
    String name;
    String surname;
    String patronymic;
    String finCode;
    String colorCode;
    boolean enabled;
    GenderStatus genderStatus;
    LocalDate dateOfBirth;
    String degree;
    String phone;
    String phone2;
    String phone3;
    String homePhone;
    String email;
    String address;
    Integer experience;
    String profilePhotoPath;
    Set<String> permissions;
}
