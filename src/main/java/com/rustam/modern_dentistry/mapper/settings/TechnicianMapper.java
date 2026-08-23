package com.rustam.modern_dentistry.mapper.settings;

import com.rustam.modern_dentistry.dao.entity.users.Technician;
import com.rustam.modern_dentistry.dto.request.create.TechnicianCreateRequest;
import com.rustam.modern_dentistry.dto.request.update.TechnicianUpdateRequest;
import com.rustam.modern_dentistry.dto.response.excel.TechnicianExcelResponse;
import com.rustam.modern_dentistry.dto.response.read.TechnicianReadResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TechnicianMapper {
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    Technician toEntity(TechnicianCreateRequest technicianCreateRequest);

    @Mapping(target = "birthDate", source = "dateOfBirth")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    TechnicianReadResponse toReadDto(Technician technician);

    @Mapping(target = "password", ignore = true)
    void update(@MappingTarget Technician technician, TechnicianUpdateRequest request);

    TechnicianExcelResponse toExcelDto(Technician technician);
}
