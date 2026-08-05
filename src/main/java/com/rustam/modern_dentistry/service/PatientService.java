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

        validationUtilService.validateUniqueFields(email,finCode);

        BaseUser doctor = utilService.findByBaseUserId(patientCreateRequest.getDoctorId());
        Patient patient = Patient.builder()
                .name(patientCreateRequest.getName())
                .surname(patientCreateRequest.getSurname())
                .patronymic(patientCreateRequest.getPatronymic())
                .finCode(finCode)
                .dateOfBirth(patientCreateRequest.getDateOfBirth())
                .phone(patientCreateRequest.getPhone())
                .email(email)
                .enabled(true)
                .baseUser(doctor)
                .homePhone(patientCreateRequest.getHomePhone())
                .workPhone(patientCreateRequest.getWorkPhone())
                .workAddress(patientCreateRequest.getWorkAddress())
                .homeAddress(patientCreateRequest.getHomeAddress())
                .genderStatus(patientCreateRequest.getGenderStatus())
                .priceCategory(priceCategoryService.findByName(patientCreateRequest.getPriceCategoryName()))
                .specializationCategory(specializationCategoryService.findByName(patientCreateRequest.getSpecializationName()))
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
        utilService.updateFieldIfPresent(request.getName(), patient::setName);
        utilService.updateFieldIfPresent(request.getSurname(), patient::setSurname);
        utilService.updateFieldIfPresent(request.getPatronymic(), patient::setPatronymic);
        utilService.updateFieldIfPresent(request.getFinCode(), patient::setFinCode);
        utilService.updateFieldIfPresent(request.getGenderStatus(), patient::setGenderStatus);
        utilService.updateFieldIfPresent(request.getDateOfBirth(), patient::setDateOfBirth);
        PriceCategory priceCategory = priceCategoryService.findByName(request.getPriceCategoryName());
        SpecializationCategory specializationCategory = specializationCategoryService.findByName(request.getSpecializationName());
        utilService.updateFieldIfPresent(priceCategory, patient::setPriceCategory);
        utilService.updateFieldIfPresent(specializationCategory, patient::setSpecializationCategory);

        if (request.getDoctorId() != null) {
            BaseUser doctor = utilService.findByBaseUserId(request.getDoctorId());
            patient.setBaseUser(doctor);
        }

        utilService.updateFieldIfPresent(request.getPhone(), patient::setPhone);
        utilService.updateFieldIfPresent(request.getWorkPhone(), patient::setWorkPhone);
        utilService.updateFieldIfPresent(request.getHomePhone(), patient::setHomePhone);
        utilService.updateFieldIfPresent(request.getHomeAddress(), patient::setHomeAddress);
        utilService.updateFieldIfPresent(request.getWorkAddress(), patient::setWorkAddress);
        utilService.updateFieldIfPresent(request.getEmail(), patient::setEmail);
    }


    public List<PatientReadResponse> read() {
        List<Patient> users = patientRepository.findAll();
        return patientMapper.toDtos(users);
    }

    @Transactional
    public PatientReadResponse readById(Long id) {
        Patient patient = utilService.findByPatientId(id);
        return patientMapper.toRead(patient);
    }

    public String delete(Long id) {
        Patient patient = utilService.findByPatientId(id);
        patientRepository.delete(patient);
        return "Qeydiyyatdan silindi";
    }

    public List<PatientReadResponse> search(PatientSearchRequest patientSearchRequest) {
        List<Patient> byNameAndSurnameAndFinCodeAndGenderStatusAndPhone =
                patientRepository.findAll(UserSpecification.filterBy(patientSearchRequest));
        return patientMapper.toDtos(byNameAndSurnameAndFinCodeAndGenderStatusAndPhone);
    }

    public InputStreamResource exportReservationsToExcel() {
        List<Patient> patients = patientRepository.findAll();
        var list = patients.stream().map(patientMapper::toExcelDto).toList();
        ByteArrayInputStream excelFile = ExcelUtil.dataToExcel(list, PatientExcelResponse.class);
        return new InputStreamResource(excelFile);
    }

}
