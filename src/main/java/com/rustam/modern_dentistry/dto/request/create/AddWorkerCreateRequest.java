package com.rustam.modern_dentistry.dto.request.create;

import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddWorkerCreateRequest {
    @NotBlank(message = "İstifadəçi Ad boş ola bilməz")
    @Size(min = 3, max = 20, message = "İstifadəçi Adı 3-20 simvol arasında olmalıdır")
    String username;
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_]).{8,}$",
            message = "Şifrə minimum 8 simvol olmalı, böyük/kiçik hərf, rəqəm və xüsusi simvol içerməlidir."
    )
    String password;
    @NotBlank(message = "Ad boş ola bilməz")
    @Size(min = 3, max = 20, message = "Ad 3-20 simvol arasında olmalıdır")
    String name;
    @NotBlank(message = "Soyad boş ola bilməz")
    @Size(min = 3, max = 20, message = "Soyad 3-20 simvol arasında olmalıdır")
    String surname;
    @NotBlank(message = "Ata Adı boş ola bilməz")
    @Size(min = 3, max = 20, message = "Ata Adı 3-20 simvol arasında olmalıdır")
    String patronymic;
    @Pattern(
            regexp = "^$|^[a-zA-Z0-9]{7}$",
            message = "FIN kod yalnız hərflər və rəqəmlərdən ibarət 7 simvol olmalıdır."
    )
    String finCode;
    String colorCode;
    @NotNull
    GenderStatus genderStatus;
    @NotNull
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
