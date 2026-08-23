package com.rustam.modern_dentistry.mapper;

import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dto.response.excel.PatientExcelResponse;
import com.rustam.modern_dentistry.dto.response.read.PatientReadResponse;
import com.rustam.modern_dentistry.dto.response.update.PatientUpdateResponse;
import org.mapstruct.*;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PatientMapper {
    PatientUpdateResponse toUpdatePatient(Patient patient);

    List<PatientReadResponse> toDtos(List<Patient> users);

    @Mapping(target = "doctorName", expression = "java(getDoctorName(user))")
    @Mapping(target = "isBlocked", expression = "java(getBlacklist(user))")
    PatientExcelResponse toExcelDto(Patient user);

    @Mapping(target = "isBlocked", expression = "java(patient.getPatientBlacklist() != null)")
    @Mapping(target = "baseUser", expression = "java(mapDoctorToString(patient.getBaseUser()))")
    @Mapping(target = "priceCategoryName", expression = "java(patient.getPriceCategory() != null ? patient.getPriceCategory().getName() : null)")
    @Mapping(target = "specializationCategoryName", expression = "java(patient.getSpecializationCategory() != null ? patient.getSpecializationCategory().getName() : null)")
    PatientReadResponse toRead(Patient patient);

    @Named("mapDoctorToString")
    default String mapDoctorToString(BaseUser baseUser) {
        if (baseUser == null) {
            return null;
        }
        return baseUser.getId();
    }


    default String getDoctorName(Patient patient) {
        return patient.getBaseUser().getName() + " " + patient.getBaseUser().getSurname();
    }

    default Boolean getBlacklist(Patient patient) {
        return patient.getPatientBlacklist() != null;
    }
}
