package com.rustam.modern_dentistry.dto.request.create;

import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.enums.status.SpecializationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientCreateRequest {
    @NotBlank(message = "Ad boş ola bilməz")
    @Pattern(
            regexp = "^[a-zA-ZƏəIıİiÇçŞşĞğÖöÜü]+$",
            message = "Ad yalnız hərflərdən ibarət olmalıdır (rəqəm və boşluq istifadə edilə bilməz)"
    )
    @Size(min = 3, max = 20, message = "Ad 3-20 simvol arasında olmalıdır")
    String name;
    @NotBlank(message = "Soyad boş ola bilməz")
    @Pattern(
            regexp = "^[a-zA-ZƏəIıİiÇçŞşĞğÖöÜü]+$",
            message = "Soyad yalnız hərflərdən ibarət olmalıdır (rəqəm və boşluq istifadə edilə bilməz)"
    )
    @Size(min = 3, max = 20, message = "Soyad 3-20 simvol arasında olmalıdır")
    String surname;
    @NotBlank(message = "Ata adı boş ola bilməz")
    @Pattern(
            regexp = "^[a-zA-ZƏəIıİiÇçŞşĞğÖöÜü]+$",
            message = "Ata adı yalnız hərflərdən ibarət olmalıdır (rəqəm və boşluq istifadə edilə bilməz)"
    )
    @Size(min = 3, max = 20, message = "Ata adı 3-20 simvol arasında olmalıdır")
    String patronymic;
    @Pattern(
            regexp = "^$|^[A-Z0-9]{7}$",
            message = "FIN kod yalnız böyük hərflər və rəqəmlərdən ibarət 7 simvol olmalıdır."
    )
    String finCode;
    @NotNull(message = "Zəhmət olmasa cinsiyyəti daxil edin")
    GenderStatus genderStatus;
    LocalDate dateOfBirth;
    @NotNull(message = "Zəhmət olmasa qiymət kateqoriyasını daxil edin")
    String priceCategoryName;
    String specializationName;
    @NotNull(message = "Zəhmət olmasa həkimi seçin")
    String doctorId; //doctor_id -sine gore gedib tapacaq
    @Pattern(
            regexp = "^\\((?!000)\\d{3}\\)-(?!000-00-00)(?!000-0000)(?!0000000)\\d{3}-(?:\\d{2}-\\d{2}|\\d{4})$",
            message = "Düzgün telefon nömrəsi daxil edin (məs: (050)-123-45-67). 000 ilə başlayan və ya saxta nömrələr qəbul edilmir."
    )
    String phone;
    @Pattern(
            regexp = "^$|^\\((?!000)\\d{3}\\)-(?!000-00-00)(?!000-0000)(?!0000000)\\d{3}-(?:\\d{2}-\\d{2}|\\d{4})$",
            message = "Düzgün iş nömrəsi daxil edin (məs: (012)-123-45-67)."
    )
    String workPhone;
    @Pattern(
            regexp = "^$|^\\((?!000)\\d{3}\\)-(?!000-00-00)(?!000-0000)(?!0000000)\\d{3}-(?:\\d{2}-\\d{2}|\\d{4})$",
            message = "Düzgün ev telefonu daxil edin (məs: (012)-123-45-67)."
    )
    String homePhone;
    String homeAddress;
    String workAddress;
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Düzgün e-poçt ünvanı daxil edin.")
    String email;
}
