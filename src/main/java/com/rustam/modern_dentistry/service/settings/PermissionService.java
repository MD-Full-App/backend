package com.rustam.modern_dentistry.service.settings;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.settings.permission.ModulePermissionEntity;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import com.rustam.modern_dentistry.dao.repository.settings.PermissionRepository;
import com.rustam.modern_dentistry.dto.ModulePermission;
import com.rustam.modern_dentistry.dto.request.create.PermissionCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.PermissionSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.ModulePermissionUpdateRequest;
import com.rustam.modern_dentistry.dto.request.update.PermissionStatusUpdatedRequest;
import com.rustam.modern_dentistry.dto.request.update.PermissionUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.PermissionCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.InfoPermissionResponse;
import com.rustam.modern_dentistry.dto.response.read.PermissionResponse;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.settings.permission.PermissionMapper;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.settings.permission.PermissionSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    UtilService utilService;

    public PermissionResponse create(PermissionCreateRequest permissionCreateRequest) {
        Permission permission = new Permission();
        permission.setPermissionName(permissionCreateRequest.getPermissionName());
        permission.setStatus(Status.ACTIVE);

        List<ModulePermissionEntity> modulePermissions = permissionCreateRequest.getPermissions().stream()
                .map(mp -> {
                    ModulePermissionEntity modulePermission = new ModulePermissionEntity();
                    modulePermission.setModuleUrl(mp.getModuleUrl());
                    modulePermission.setPermission(permission);
                    modulePermission.setActions(mp.getActions());
                    return modulePermission;
                }).toList();

        permission.setModulePermissions(modulePermissions);

        Permission saved = permissionRepository.save(permission);

        return PermissionResponse.builder()
                .id(saved.getId())
                .permissionName(saved.getPermissionName())
                .status(saved.getStatus())
                .build();
    }

    public Permission findByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + permissionName));
    }

    public Permission findById(Long id){
        return permissionRepository.findWithModulePermissions(id)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
    }

    @Transactional
    public List<PermissionResponse> read() {
        return permissionMapper.toDtos(permissionRepository.findAllWithModulePermissions());
    }

    @Transactional
    public InfoPermissionResponse info(Long id) {
        Permission permission = findById(id);
        return toDto(permission);
    }

    public InfoPermissionResponse toDto(Permission permission) {
        return InfoPermissionResponse.builder()
                .permissionName(permission.getPermissionName())
                .status(permission.getStatus())
                .modulePermissions(mapModulePermissions(permission.getModulePermissions()))
                .build();
    }

    private List<ModulePermission> mapModulePermissions(List<ModulePermissionEntity> entities) {
        return entities.stream()
                .map(e -> new ModulePermission(e.getModuleUrl(), e.getActions()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    @Transactional
    public List<PermissionResponse> search(PermissionSearchRequest permissionSearchRequest) {
        List<Permission> permissions = permissionRepository.findAll(PermissionSpecification.filterBy(permissionSearchRequest));
        return permissionMapper.toDtos(permissions);
    }

    public void delete(Long id) {
        Permission permission = findById(id);
        permissionRepository.delete(permission);
    }

    @Transactional
    public PermissionResponse statusUpdated(PermissionStatusUpdatedRequest permissionStatusUpdatedRequest) {
        Permission permission = findById(permissionStatusUpdatedRequest.getId());
        permission.setStatus(permissionStatusUpdatedRequest.getStatus());
        permissionRepository.save(permission);
        return permissionMapper.toResponse(permission);
    }

    public PermissionCreateResponse update(PermissionUpdateRequest request) {
        Permission permission = findById(request.getId());

        utilService.updateFieldIfPresent(request.getPermissionName(), permission::setPermissionName);

        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {

            Map<Long, ModulePermissionEntity> existingModulesMap = permission.getModulePermissions().stream()
                    .filter(mp -> mp.getId() != null)
                    .collect(Collectors.toMap(
                            ModulePermissionEntity::getId,
                            Function.identity(),
                            (existing, duplicate) -> {
                                // Əgər istəyirsənsə, burada loq yaz
                                return existing; // eyni id varsa, mövcudu saxla
                            }
                    ));

            List<ModulePermissionEntity> updatedModules = new ArrayList<>();

            for (ModulePermissionUpdateRequest dto : request.getPermissions()) {
                ModulePermissionEntity modulePermission;

                if (dto.getModulePermissionId() != null && existingModulesMap.containsKey(dto.getModulePermissionId())) {
                    modulePermission = existingModulesMap.get(dto.getModulePermissionId());
                    modulePermission.setModuleUrl(dto.getModuleUrl());
                    modulePermission.setActions(dto.getActions());
                } else {
                    modulePermission = ModulePermissionEntity.builder()
                            .moduleUrl(dto.getModuleUrl())
                            .actions(dto.getActions())
                            .permission(permission)
                            .build();
                }

                updatedModules.add(modulePermission);
            }

            permission.getModulePermissions().clear();
            permission.getModulePermissions().addAll(updatedModules);
        }

        Permission updated = permissionRepository.save(permission);

        List<ModulePermissionUpdateRequest> modulePermissionDtos = updated.getModulePermissions().stream()
                .map(mp -> ModulePermissionUpdateRequest.builder()
                        .modulePermissionId(mp.getId())
                        .moduleUrl(mp.getModuleUrl())
                        .actions(mp.getActions())
                        .build())
                .toList();

        return PermissionCreateResponse.builder()
                .permissionName(updated.getPermissionName())
                .modulePermissions(modulePermissionDtos)
                .build();
    }



}
