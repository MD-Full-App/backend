package com.rustam.modern_dentistry.service.patient_info.insurance;

import com.rustam.modern_dentistry.dao.entity.patient_info.insurance.PatientInsuranceBalance;
import com.rustam.modern_dentistry.dao.repository.patient_info.insurance.PatientInsuranceBalanceRepository;
import com.rustam.modern_dentistry.dto.request.create.PatInsuranceBalanceCreateReq;
import com.rustam.modern_dentistry.dto.request.update.PatInsuranceBalanceUpdateReq;
import com.rustam.modern_dentistry.dto.response.read.PatInsuranceBalanceReadResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.patient_info.insurance.PatientInsuranceBalanceMapper;
import com.rustam.modern_dentistry.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static com.rustam.modern_dentistry.dao.entity.enums.status.Status.ACTIVE;
import static com.rustam.modern_dentistry.dao.entity.enums.status.Status.PASSIVE;
import static com.rustam.modern_dentistry.util.constants.Directory.getUrl;
import static com.rustam.modern_dentistry.util.constants.Directory.pathPatInsuranceBalance;

@Service
@RequiredArgsConstructor
public class PatientInsuranceBalanceService {
    private final FileService fileService;
    private final PatientInsuranceService patientInsuranceService;
    private final PatientInsuranceBalanceMapper patientInsuranceBalanceMapper;
    private final PatientInsuranceBalanceRepository patientInsuranceBalanceRepository;

    @Transactional
    public void create(PatInsuranceBalanceCreateReq request, MultipartFile file) {
        checkDate(request.getDate(), request.getPatientInsuranceId());
        var patientInsurance = patientInsuranceService.getPatientInsuranceById(request.getPatientInsuranceId());
        var entity = patientInsuranceBalanceMapper.toEntity(request);
        if (file != null && !file.isEmpty()) {
            var newFileName = fileService.getNewFileName(file, "insurance_balance_");
            fileService.checkFileIfExist(file);
            fileService.writeFile(file, pathPatInsuranceBalance, newFileName);
            entity.setFileName(newFileName);
        }
        entity.setPatientInsurance(patientInsurance);

        patientInsuranceBalanceRepository.save(entity);
    }

    public List<PatInsuranceBalanceReadResponse> read(Long patientInsuranceId) {
        List<PatientInsuranceBalance> patientInsurances;
        if (patientInsuranceId != null) {
            patientInsurances = patientInsuranceBalanceRepository.findAllByPatientInsurance_Id(patientInsuranceId);
        } else {
            patientInsurances = patientInsuranceBalanceRepository.findAll();
        }
        return patientInsurances.stream()
                .map(patientInsuranceBalanceMapper::toReadDto)
                .toList();
    }

    public PatInsuranceBalanceReadResponse readById(Long id) {
        var patientInsurance = getPatientInsurance(id);
        var response = patientInsuranceBalanceMapper.toReadDto(patientInsurance);
        if (patientInsurance.getFileName() != null) {
            response.setUrl(getUrl(pathPatInsuranceBalance, patientInsurance.getFileName()));
        }
        return response;
    }

    @Transactional
    public void update(Long id, PatInsuranceBalanceUpdateReq request, MultipartFile file) {
        var patientInsurance = getPatientInsurance(id);
        if (!patientInsurance.getDate().equals(request.getDate()))
            checkDate(request.getDate(), request.getPatientInsuranceId());
        String newFileName = patientInsurance.getFileName();
        if (file != null && !file.isEmpty()) {
            newFileName = fileService.getNewFileName(file, "insurance_balance_");
            fileService.updateFile(file, pathPatInsuranceBalance, patientInsurance.getFileName(), newFileName);
        }
        patientInsuranceBalanceMapper.update(patientInsurance, request, newFileName);
        patientInsuranceBalanceRepository.save(patientInsurance);
    }

    public void updateStatus(Long id) {
        var patientInsurance = getPatientInsurance(id);
        patientInsurance.setStatus(patientInsurance.getStatus() == ACTIVE ? PASSIVE : ACTIVE);
        patientInsuranceBalanceRepository.save(patientInsurance);
    }

    @Transactional
    public void delete(Long id) {
        var patientInsurance = getPatientInsurance(id);
        var fullPath = pathPatInsuranceBalance + "/" + patientInsurance.getFileName();
        patientInsuranceBalanceRepository.delete(patientInsurance);
        fileService.deleteFile(fullPath);
    }

    private PatientInsuranceBalance getPatientInsurance(Long id) {
        return patientInsuranceBalanceRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Bu ID-də qalıq məbləği tapımadı:" + id)
        );
    }

    private void checkDate(LocalDate date, Long patientInsuranceId) {
        var result = patientInsuranceBalanceRepository.existsByDateAndPatientInsurance_Id(date, patientInsuranceId);
        if (result) throw new ExistsException("Bu tarix artıq əlavə edilib.");
    }
}