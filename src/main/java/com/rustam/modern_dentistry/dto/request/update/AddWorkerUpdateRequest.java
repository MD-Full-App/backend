package com.rustam.modern_dentistry.dto.request.update;

import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddWorkerUpdateRequest {
    @NotNull
    UUID id;
    String username;
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_]).{8,}$",
            message = "Şifrə minimum 8 simvol olmalı, böyük/kiçik hərf, rəqəm və xüsusi simvol içerməlidir."
    )
    String password;
    String name;
    String surname;
    String patronymic;
    @Pattern(
            regexp = "^$|^[a-zA-Z0-9]{7}$",
            message = "FIN kod yalnız hərflər və rəqəmlərdən ibarət 7 simvol olmalıdır."
    )
    String finCode;
    String colorCode;
    GenderStatus genderStatus;
    LocalDate dateOfBirth;
    String degree;
    @Pattern(regexp = "\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}", message = "Please enter your phone number in the format (000)-000-00-00.")
    String phone;
    @Pattern(regexp = "\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}", message = "Please enter your phone number in the format (000)-000-00-00.")
    String phone2;
    @Pattern(regexp = "\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}", message = "Please enter your phone number in the format (000)-000-00-00.")
    String phone3;
    @Pattern(regexp = "\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}", message = "Please enter your phone number in the format (000)-000-00-00.")
    String homePhone;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Enter a valid email address.")
    String email;
    String address;
    Integer experience;
    Set<String> permissions;
}
