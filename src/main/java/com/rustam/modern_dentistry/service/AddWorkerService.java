package com.rustam.modern_dentistry.service;

import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.repository.BaseUserRepository;
import com.rustam.modern_dentistry.dto.request.create.AddWorkerCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.AddWorkerSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.AddWorkerUpdateRequest;
import com.rustam.modern_dentistry.dto.response.create.AddWorkerCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.AddWorkerReadResponse;
import com.rustam.modern_dentistry.dto.response.read.AddWorkerReadStatusResponse;
import com.rustam.modern_dentistry.dto.response.update.AddWorkerUpdateResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.exception.custom.UserNotFountException;
import com.rustam.modern_dentistry.mapper.AddWorkerMapper;
import com.rustam.modern_dentistry.service.settings.PermissionService;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.specification.UserSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AddWorkerService {

    UtilService utilService;
    BaseUserRepository baseUserRepository;
    AddWorkerMapper addWorkerMapper;
    PermissionService permissionService;
    PermissionVisibilityService permissionVisibilityService;
    PasswordEncoder passwordEncoder;

    public AddWorkerCreateResponse create(AddWorkerCreateRequest dto) {
        System.out.println("📥 Backend'ə gələn DTO:");
        System.out.println("  colorCode: " + dto.getColorCode());
        System.out.println("  experience: " + dto.getExperience());
        System.out.println("  address: " + dto.getAddress());
        System.out.println("  degree: " + dto.getDegree());
        
        Set<Permission> newPermissions = dto.getPermissions().stream()
                .map(permissionService::findByName)
                .collect(Collectors.toSet());

        utilService.findByUsernameAndEmailAndColorCode(
                dto.getUsername(),
                dto.getEmail(),
                dto.getColorCode()
        ).ifPresent(user -> {
            throw new ExistsException("User with these credentials already exists in the system");
        });
        BaseUser baseUser = addWorkerMapper.dtoToEntity(new BaseUser(), dto);
        baseUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        baseUser.setPermissions(newPermissions);
        baseUser.setEnabled(true);
        baseUserRepository.save(baseUser);
        return addWorkerMapper.entityToDto(baseUser);
    }

    @Transactional(readOnly = true)
    public List<AddWorkerReadResponse> read() {
        BaseUser currentUser = utilService.findByBaseUserId(utilService.getCurrentUserId());
        Set<String> userPermissions = extractPermissions(currentUser);

        List<BaseUser> visibleUsers = userPermissions.stream()
                .anyMatch(permissionVisibilityService::isAdminPermission)
                ? baseUserRepository.findAll()
                : getFilteredUsers(userPermissions);

        return visibleUsers.stream()
                .map(addWorkerMapper::toResponse)
                .toList();
    }

    private Set<String> extractPermissions(BaseUser user) {
        return user.getPermissions().stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
    }


    private List<BaseUser> getFilteredUsers(Set<String> permissions) {
        Set<String> visiblePermissions = permissions.stream()
                .map(permissionVisibilityService::getVisiblePermissions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return baseUserRepository.findAllByPermissionsPermissionNameIn(visiblePermissions);
    }

    @Transactional
    public AddWorkerUpdateResponse update(AddWorkerUpdateRequest dto) {
        BaseUser baseUser = baseUserRepository.findByIdWithPermissions(dto.getId())
                .orElseThrow(() -> new UserNotFountException("No such user found."));

        Set<Permission> newPermissions = dto.getPermissions().stream()
                .map(permissionService::findByName)
                .collect(Collectors.toSet());

        utilService.findByUsernameAndEmailAndColorCode(
                dto.getUsername(),
                dto.getEmail(),
                dto.getColorCode()
        ).ifPresent(user -> {
            throw new ExistsException("User with these credentials already exists in the system");
        });
        BaseUser entityUpdate = addWorkerMapper.dtoToEntityUpdate(baseUser, dto);
        baseUser.setPermissions(newPermissions);
        baseUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        baseUserRepository.save(entityUpdate);
        return addWorkerMapper.updateDtoToResponse(entityUpdate);
    }

    @Transactional
    public void delete(UUID id) {
        BaseUser baseUser = baseUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFountException("No such user found."));
        baseUserRepository.delete(baseUser);
    }

    @Transactional(readOnly = true)
    public AddWorkerReadResponse info(UUID id) {
        BaseUser baseUser = baseUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFountException("No such user found."));
        return addWorkerMapper.toResponse(baseUser);
    }

    @Transactional(readOnly = true)
    public List<AddWorkerReadResponse> search(AddWorkerSearchRequest addWorkerSearchRequest) {
        List<BaseUser> users = baseUserRepository.findAll(UserSpecification.filterByWorker(addWorkerSearchRequest));
        return users.stream()
                .map(addWorkerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AddWorkerReadResponse> readPermission(String permission) {
        List<BaseUser> users = baseUserRepository.findAll(UserSpecification.filterByPermission(permission));
        return users.stream()
                .map(addWorkerMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<AddWorkerReadStatusResponse> readStatus() {
        return permissionService.read().stream()
                .map(p -> new AddWorkerReadStatusResponse(p.getPermissionName()))
                .collect(Collectors.toList());
    }

    public List<BaseUser> readAll() {
        return baseUserRepository.findAllWithPermissionDoctor();
    }
}
