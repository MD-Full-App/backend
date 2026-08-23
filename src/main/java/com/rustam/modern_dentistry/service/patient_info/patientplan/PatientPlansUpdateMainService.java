package com.rustam.modern_dentistry.service.patient_info.patientplan;

import com.rustam.modern_dentistry.dao.entity.patient_info.patientplan.PatientPlanMain;
import com.rustam.modern_dentistry.dao.repository.patient_info.patientplan.PatientPlanMainRepository;
import com.rustam.modern_dentistry.dto.request.update.PatientPlansMainUpdateRequest;
import com.rustam.modern_dentistry.dto.response.read.PatientPlansMainResponse;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.patient_info.patientplan.PatientPlansMainMapper;
import com.rustam.modern_dentistry.util.DateTimeUtil;
import com.rustam.modern_dentistry.util.UtilService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PatientPlansUpdateMainService {

    PatientPlanMainRepository patientPlanMainRepository;
    UtilService utilService;
    PatientPlansMainMapper patientPlansMainMapper;

    public PatientPlansMainResponse update(PatientPlansMainUpdateRequest req) {
        PatientPlanMain patientPlanMain = findByIdAndStatusAndActionStatus(req.getId());
        return patientPlansMainMapper.toDto(mapperUpdated(patientPlanMain,req));
    }

    private PatientPlanMain mapperUpdated(PatientPlanMain patientPlanMain, PatientPlansMainUpdateRequest req) {
        utilService.updateFieldIfPresent(req.getPlanName(),patientPlanMain::setPlanName);
        utilService.updateFieldIfPresent(req.getKey(),patientPlanMain::setKey);
        patientPlanMain.setUpdatedBy(utilService.getCurrentUserId());
        patientPlanMain.setUpdatedDate(DateTimeUtil.toEpochMilli(LocalDateTime.now()));
        return patientPlanMainRepository.save(patientPlanMain);
    }

    public PatientPlanMain findByIdAndStatusAndActionStatus(UUID id) {
        return patientPlanMainRepository.findByIdAndStatusInAndActionStatusIn(id, List.of("A","C"),List.of("A","C"))
                .orElseThrow(() -> new NotFoundException("patient plan main does not found"));
    }

}
