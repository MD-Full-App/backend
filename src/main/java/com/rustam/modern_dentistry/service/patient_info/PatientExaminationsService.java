package com.rustam.modern_dentistry.service.patient_info;

import com.rustam.modern_dentistry.dao.entity.Examination;
import com.rustam.modern_dentistry.dao.entity.patient_info.PatientExaminations;
import com.rustam.modern_dentistry.dao.entity.settings.teeth.TeethExamination;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dto.request.read.RequestToSeeTheExaminations;
import com.rustam.modern_dentistry.dto.response.read.SelectingPatientToReadResponse;
import com.rustam.modern_dentistry.dao.repository.PatientExaminationsRepository;
import com.rustam.modern_dentistry.dto.request.create.PatientExaminationsCreateRequest;
import com.rustam.modern_dentistry.dto.request.create.PatientExaminationsUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.PatientExaminationsCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.ExaminationResponse;
import com.rustam.modern_dentistry.dto.response.read.PatientExaminationsResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.service.GeneralCalendarService;
import com.rustam.modern_dentistry.service.settings.ExaminationService;
import com.rustam.modern_dentistry.service.settings.teeth.TeethService;
import com.rustam.modern_dentistry.util.UtilService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientExaminationsService {

    PatientExaminationsRepository patientExaminationsRepository;
    ExaminationService examinationService;
    TeethService teethService;
    UtilService utilService;
    GeneralCalendarService generalCalendarService;

    public List<ExaminationResponse> readExaminations() {
        return examinationService.read();
    }

    @org.springframework.transaction.annotation.Transactional
    public PatientExaminationsCreateResponse create(PatientExaminationsCreateRequest request) {
        Patient patient = utilService.findByPatientId(request.getPatientId());
        Examination examination = examinationService.findById(request.getExaminationId());
        String currentUserId = utilService.getCurrentUserId();
        UUID doctorUuid = null;
        try {
            if (currentUserId != null && !currentUserId.isBlank()) {
                doctorUuid = UUID.fromString(currentUserId);
            }
        } catch (Exception ignored) {}

        PatientExaminations pe = PatientExaminations.builder()
                .patient(patient)
                .toothNumber(request.getToothId())
                .diagnosis(examination.getTypeName())
                .doctorId(doctorUuid)
                .patientAppointmentDate(java.time.LocalDate.now())
                .build();
        patientExaminationsRepository.save(pe);

        return PatientExaminationsCreateResponse.builder()
                .patientId(request.getPatientId())
                .toothNo(List.of(request.getToothId()))
                .diagnosis(examination.getTypeName())
                .doctorId(currentUserId)
                .build();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PatientExaminationsResponse> readByPatientId(Long patientId) {
        List<PatientExaminations> list = patientExaminationsRepository.findByPatient_Id(patientId);
        return list.stream().map(pe -> {
            String doctorName = null;
            if (pe.getDoctorId() != null) {
                try {
                    com.rustam.modern_dentistry.dao.entity.users.BaseUser doctor = utilService.findByBaseUserId(pe.getDoctorId().toString());
                    if (doctor != null) {
                        doctorName = (doctor.getName() != null ? doctor.getName() : "") + " " + (doctor.getSurname() != null ? doctor.getSurname() : "");
                    }
                } catch (Exception ignored) {}
            }
            return PatientExaminationsResponse.builder()
                    .id(pe.getId())
                    .toothNo(pe.getToothNumber())
                    .diagnosis(pe.getDiagnosis())
                    .doctorName(doctorName)
                    .build();
        }).toList();
    }

    public PatientExaminations findById(Long id) {
        return patientExaminationsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("This patient does not have these tests."));
    }

    public PatientExaminationsCreateResponse update(PatientExaminationsUpdateRequest patientExaminationsUpdateRequest) {
        PatientExaminations patientExaminations = findById(patientExaminationsUpdateRequest.getId());
        Patient patient = utilService.findByPatientId(patientExaminationsUpdateRequest.getPatientId());
        Examination examination = examinationService.findById(patientExaminationsUpdateRequest.getExaminationId());
        boolean existsPatientExaminationsByPatientAndToothNumber = patientExaminationsRepository.existsPatientExaminationsByPatientAndToothNumberAndDiagnosis(patientExaminationsUpdateRequest.getPatientId(), patientExaminationsUpdateRequest.getToothNumber(),examination.getTypeName());
        if (existsPatientExaminationsByPatientAndToothNumber) {
            throw new ExistsException("These examinations are available for this patient.");
        }
        List<Long> teethNo = new ArrayList<>();
//        patientExaminationsUpdateRequest.getToothNumber().forEach(toothNumber -> {
//            patientExaminations.setPatient(patient);
//            patientExaminations.setToothNumber(toothNumber);
//            patientExaminations.setDiagnosis(examination.getTypeName());
//            patientExaminationsRepository.save(patientExaminations);
//            teethNo.add(toothNumber);
//        });
        return PatientExaminationsCreateResponse.builder()
                .patientId(patientExaminationsUpdateRequest.getPatientId())
                .toothNo(teethNo)
                .diagnosis(examination.getTypeName())
                .build();
    }

//    public List<PatientExaminationsResponse> read() {
//        return patientExaminationsRepository.findAllPatientExaminations();
//    }

    public void delete(Long id) {
        PatientExaminations patientExaminations = findById(id);
        patientExaminationsRepository.delete(patientExaminations);
    }

    public List<PatientExaminationsResponse> seeHistoricalElectionDentalExaminations(RequestToSeeTheExaminations requestToSeeTheExaminations) {
        return patientExaminationsRepository.findByAppointmentDate(requestToSeeTheExaminations.getDate());
    }

}
