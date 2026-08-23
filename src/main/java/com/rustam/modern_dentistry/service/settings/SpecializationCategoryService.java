package com.rustam.modern_dentistry.service.settings;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.settings.SpecializationCategory;
import com.rustam.modern_dentistry.dao.repository.settings.SpecializationCategoryRepository;
import com.rustam.modern_dentistry.dto.request.create.SpecializationCategoryCreateRequest;
import com.rustam.modern_dentistry.dto.request.update.SpecializationCategoryUpdateRequest;
import com.rustam.modern_dentistry.dto.response.read.SpecializationCategoryResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.settings.SpecializationCategoryMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class SpecializationCategoryService {

    SpecializationCategoryRepository specializationCategoryRepository;
    SpecializationCategoryMapper specializationCategoryMapper;

    public SpecializationCategory findByName(String specializationCategoryName) {
        return specializationCategoryRepository.findByName(specializationCategoryName)
                .orElseThrow(
                        () -> new NotFoundException("Specialization category name not found"));
    }

    public SpecializationCategoryResponse create(SpecializationCategoryCreateRequest specializationCategoryCreateRequest) {
        validate(specializationCategoryCreateRequest.getName());
        SpecializationCategory save = specializationCategoryRepository.save(
                SpecializationCategory.builder()
                        .name(specializationCategoryCreateRequest.getName())
                        .status(Status.ACTIVE)
                        .build()
        );
        return specializationCategoryMapper.toDto(save);
    }

    public List<SpecializationCategoryResponse> read() {
        return specializationCategoryMapper.toDtos(specializationCategoryRepository.findAll());
    }

    public Boolean existsByName(String specializationCategoryName) {
        return specializationCategoryRepository.existsByName(specializationCategoryName);
    }

    public SpecializationCategory readById(Long id) {
        return specializationCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id tapilmadi"));
    }

    private void validate(String name){
        if (existsByName(name)) {
            throw new ExistsException("Specialization category name " + name + " already exists");
        }
    }

    public SpecializationCategoryResponse update(SpecializationCategoryUpdateRequest specializationCategoryUpdateRequest) {
        SpecializationCategory specializationCategory = readById(specializationCategoryUpdateRequest.getId());
        if (specializationCategoryUpdateRequest.getName() != null && !specializationCategoryUpdateRequest.getName().trim().isEmpty()) {
            if (!specializationCategory.getName().equalsIgnoreCase(specializationCategoryUpdateRequest.getName().trim())) {
                validate(specializationCategoryUpdateRequest.getName().trim());
            }
            specializationCategory.setName(specializationCategoryUpdateRequest.getName().trim());
        }
        if (specializationCategoryUpdateRequest.getStatus() != null) {
            specializationCategory.setStatus(specializationCategoryUpdateRequest.getStatus());
        }
        return specializationCategoryMapper.toDto(specializationCategoryRepository.save(specializationCategory));
    }

    public void delete(Long id) {
        specializationCategoryRepository.delete(readById(id));
    }
}
