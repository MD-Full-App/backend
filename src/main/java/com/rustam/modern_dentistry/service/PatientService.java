package com.rustam.modern_dentistry.service;

import com.rustam.modern_dentistry.dao.entity.settings.PriceCategory;
import com.rustam.modern_dentistry.dao.entity.settings.SpecializationCategory;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dao.repository.PatientRepository;
import com.rustam.modern_dentistry.dto.request.create.PatientCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.PatientSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.PatientUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.PatientCreateResponse;
import com.rustam.modern_dentistry.dto.response.excel.PatientExcelResponse;
import com.rustam.modern_dentistry.dto.response.read.PatientReadResponse;
import com.rustam.modern_dentistry.dto.response.update.PatientUpdateResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.mapper.PatientMapper;
import com.rustam.modern_dentistry.service.settings.PriceCategoryService;
import com.rustam.modern_dentistry.service.settings.SpecializationCategoryService;
import com.rustam.modern_dentistry.util.ExcelUtil;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.ValidationUtilService;
import com.rustam.modern_dentistry.util.specification.UserSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PatientService {

    PatientRepository patientRepository;
    UtilService utilService;
    PatientMapper patientMapper;
    ModelMapper modelMapper;
    PriceCategoryService priceCategoryService;
    ValidationUtilService validationUtilService;
    SpecializationCategoryService specializationCategoryService;

    public PatientCreateResponse create(PatientCreateRequest patientCreateRequest) {
        String email = StringUtils.trimToNull(patientCreateRequest.getEmail());
        String finCode = StringUtils.trimToNull(patientCreateRequest.getFinCode());

        validationUtilService.validateUniqueFields(email, finCode);

        BaseUser doctor = null;
        if (patientCreateRequest.getDoctorId() != null && !patientCreateRequest.getDoctorId().isBlank()) {
            doctor = utilService.findByBaseUserId(patientCreateRequest.getDoctorId());
        }

        PriceCategory priceCategory = null;
        if (patientCreateRequest.getPriceCategoryName() != null && !patientCreateRequest.getPriceCategoryName().isBlank()) {
            try {
                priceCategory = priceCategoryService.findByName(patientCreateRequest.getPriceCategoryName());
            } catch (Exception ignored) {}
        }

        SpecializationCategory specializationCategory = null;
        if (patientCreateRequest.getSpecializationName() != null && !patientCreateRequest.getSpecializationName().isBlank()) {
            try {
                specializationCategory = specializationCategoryService.findByName(patientCreateRequest.getSpecializationName());
            } catch (Exception ignored) {}
        }

        Patient patient = Patient.builder()
                .name(patientCreateRequest.getName() != null ? patientCreateRequest.getName().trim() : null)
                .surname(patientCreateRequest.getSurname() != null ? patientCreateRequest.getSurname().trim() : null)
                .patronymic(patientCreateRequest.getPatronymic() != null ? patientCreateRequest.getPatronymic().trim() : null)
                .finCode(finCode)
                .dateOfBirth(patientCreateRequest.getDateOfBirth())
                .phone(patientCreateRequest.getPhone() != null ? patientCreateRequest.getPhone().trim() : null)
                .email(email)
                .enabled(true)
                .baseUser(doctor)
                .homePhone(StringUtils.trimToNull(patientCreateRequest.getHomePhone()))
                .workPhone(StringUtils.trimToNull(patientCreateRequest.getWorkPhone()))
                .workAddress(StringUtils.trimToNull(patientCreateRequest.getWorkAddress()))
                .homeAddress(StringUtils.trimToNull(patientCreateRequest.getHomeAddress()))
                .genderStatus(patientCreateRequest.getGenderStatus())
                .priceCategory(priceCategory)
                .specializationCategory(specializationCategory)
                .registrationDate(LocalDate.now())
                .build();
        patientRepository.save(patient);
        PatientCreateResponse patientCreateResponse = new PatientCreateResponse();
        modelMapper.map(patient, patientCreateResponse);
        return patientCreateResponse;
    }

    public PatientUpdateResponse update(PatientUpdateRequest patientUpdateRequest) {
        Patient patient = utilService.findByPatientId(patientUpdateRequest.getPatientId());
        updatePatientFromRequest(patient, patientUpdateRequest);
        patientRepository.save(patient);
        return patientMapper.toUpdatePatient(patient);
    }

    private void updatePatientFromRequest(Patient patient, PatientUpdateRequest request) {
        utilService.updateFieldIfPresent(request.getName() != null ? request.getName().trim() : null, patient::setName);
        utilService.updateFieldIfPresent(request.getSurname() != null ? request.getSurname().trim() : null, patient::setSurname);
        utilService.updateFieldIfPresent(request.getPatronymic() != null ? request.getPatronymic().trim() : null, patient::setPatronymic);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getFinCode()), patient::setFinCode);
        utilService.updateFieldIfPresent(request.getGenderStatus(), patient::setGenderStatus);
        utilService.updateFieldIfPresent(request.getDateOfBirth(), patient::setDateOfBirth);
        
        if (request.getPriceCategoryName() != null && !request.getPriceCategoryName().isBlank()) {
            try {
                PriceCategory priceCategory = priceCategoryService.findByName(request.getPriceCategoryName());
                patient.setPriceCategory(priceCategory);
            } catch (Exception ignored) {}
        }
        if (request.getSpecializationName() != null && !request.getSpecializationName().isBlank()) {
            try {
                SpecializationCategory specializationCategory = specializationCategoryService.findByName(request.getSpecializationName());
                patient.setSpecializationCategory(specializationCategory);
            } catch (Exception ignored) {}
        }

        if (request.getDoctorId() != null && !request.getDoctorId().isBlank()) {
            BaseUser doctor = utilService.findByBaseUserId(request.getDoctorId());
            patient.setBaseUser(doctor);
        }

        utilService.updateFieldIfPresent(request.getPhone() != null ? request.getPhone().trim() : null, patient::setPhone);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getWorkPhone()), patient::setWorkPhone);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getHomePhone()), patient::setHomePhone);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getHomeAddress()), patient::setHomeAddress);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getWorkAddress()), patient::setWorkAddress);
        utilService.updateFieldIfPresent(StringUtils.trimToNull(request.getEmail()), patient::setEmail);
    }


    @Transactional(readOnly = true)
    public List<PatientReadResponse> read() {
        List<Patient> users = patientRepository.findAll();
        return patientMapper.toDtos(users);
    }

    @Transactional(readOnly = true)
    public PatientReadResponse readById(Long id) {
        Patient patient = utilService.findByPatientId(id);
        return patientMapper.toRead(patient);
    }

    @Transactional
    public String delete(Long id) {
        Patient patient = utilService.findByPatientId(id);
        patientRepository.delete(patient);
        return "Qeydiyyatdan silindi";
    }

    @Transactional(readOnly = true)
    public List<PatientReadResponse> search(PatientSearchRequest patientSearchRequest) {
        List<Patient> byNameAndSurnameAndFinCodeAndGenderStatusAndPhone =
                patientRepository.findAll(UserSpecification.filterBy(patientSearchRequest));
        return patientMapper.toDtos(byNameAndSurnameAndFinCodeAndGenderStatusAndPhone);
    }

    @Transactional(readOnly = true)
    public InputStreamResource exportReservationsToExcel() {
        List<Patient> patients = patientRepository.findAll();
        var list = patients.stream().map(patientMapper::toExcelDto).toList();
        ByteArrayInputStream excelFile = ExcelUtil.dataToExcel(list, PatientExcelResponse.class);
        return new InputStreamResource(excelFile);
    }

}
