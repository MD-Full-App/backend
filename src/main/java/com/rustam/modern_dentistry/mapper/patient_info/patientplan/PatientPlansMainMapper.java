package com.rustam.modern_dentistry.mapper.patient_info.patientplan;

import com.rustam.modern_dentistry.dao.entity.patient_info.patientplan.PatientPlanMain;
import com.rustam.modern_dentistry.dto.response.read.PatientPlansMainResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PatientPlansMainMapper {
    @Mapping(target = "insuranceId", source = "insuranceCompany.id")
    @Mapping(target = "isSave", expression = "java(isActionStatusA(patientPlanMain))")
    PatientPlansMainResponse toDto(PatientPlanMain patientPlanMain);

    List<PatientPlansMainResponse> toDtos(List<PatientPlanMain> all);

    List<String> toStrings(List<String> companyName);

    default Boolean isActionStatusA(PatientPlanMain patientPlanMain) {
        return patientPlanMain.getActionStatus() != null &&
                "A".equals(patientPlanMain.getActionStatus());
    }
}
