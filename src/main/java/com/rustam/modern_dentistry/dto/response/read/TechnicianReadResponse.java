package com.rustam.modern_dentistry.dto.response.read;

import com.rustam.modern_dentistry.dao.entity.enums.status.GenderStatus;
import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TechnicianReadResponse {
    String id;
    String username;
    String name;
    String surname;
    String patronymic;
    LocalDate birthDate;
    LocalDate dateOfBirth;
    String phone;
    String phone2;
    String homePhone;
    String address;
    String finCode;
    GenderStatus genderStatus;
    Status status;
}
