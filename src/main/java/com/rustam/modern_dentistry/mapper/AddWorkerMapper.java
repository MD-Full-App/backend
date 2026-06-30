package com.rustam.modern_dentistry.mapper;

import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dto.request.create.AddWorkerCreateRequest;
import com.rustam.modern_dentistry.dto.request.update.AddWorkerUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.AddWorkerCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.AddWorkerReadResponse;
import com.rustam.modern_dentistry.dto.response.read.AddWorkerReadStatusResponse;
import com.rustam.modern_dentistry.dto.response.update.AddWorkerUpdateResponse;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AddWorkerMapper {
    
    @Mapping(target = "permissions", source = "permissions")
    AddWorkerReadResponse toResponse(BaseUser baseUser);

    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return Set.of();
        return permissions.stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
    }
    List<AddWorkerReadResponse> toResponses(List<BaseUser> users);

    List<AddWorkerReadStatusResponse> toPermissionResponses(List<BaseUser> all);

    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "colorCode", source = "colorCode")
    @Mapping(target = "experience", source = "experience")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "degree", source = "degree")
    @Mapping(target = "phone2", source = "phone2")
    @Mapping(target = "phone3", source = "phone3")
    @Mapping(target = "homePhone", source = "homePhone")
    BaseUser dtoToEntity(@MappingTarget BaseUser baseUser, AddWorkerCreateRequest dto);

    default Set<Permission> map(Set<String> permissionNames) {
        if (permissionNames == null) {
            return new HashSet<>();
        }
        return permissionNames.stream()
                .map(name -> Permission.builder()
                        .permissionName(name)
                        .build())
                .collect(Collectors.toSet());
    }

    AddWorkerCreateResponse entityToDto(BaseUser baseUser);

    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "colorCode", source = "colorCode")
    @Mapping(target = "experience", source = "experience")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "degree", source = "degree")
    @Mapping(target = "phone2", source = "phone2")
    @Mapping(target = "phone3", source = "phone3")
    @Mapping(target = "homePhone", source = "homePhone")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BaseUser dtoToEntityUpdate(@MappingTarget BaseUser baseUser, AddWorkerUpdateRequest dto);

    AddWorkerUpdateResponse updateDtoToResponse(BaseUser entityUpdate);
}
