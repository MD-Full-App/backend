package com.rustam.modern_dentistry.service.settings;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.settings.AppointmentType;
import com.rustam.modern_dentistry.dao.repository.settings.AppointmentTypeRepository;
import com.rustam.modern_dentistry.dto.request.create.AppointmentTypeCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.AppointmentTypeSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.AppointmentTypeUpdateRequest;
import com.rustam.modern_dentistry.dto.request.update.ProductCategoryStatusUpdatedRequest;
import com.rustam.modern_dentistry.dto.response.read.AppointmentTypeResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.settings.AppointmentTypeMapper;
import com.rustam.modern_dentistry.util.specification.settings.AppointmentTypeSpecification;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AppointmentTypeService {

    AppointmentTypeRepository appointmentTypeRepository;
    AppointmentTypeMapper appointmentTypeMapper;

    public AppointmentTypeResponse create(AppointmentTypeCreateRequest appointmentTypeCreateRequest) {
        boolean existsByAppointmentTypeName = appointmentTypeRepository.existsByAppointmentTypeName(appointmentTypeCreateRequest.getAppointmentTypeName());
        if (existsByAppointmentTypeName){
            throw new ExistsException("bu randevu tip adi movcuddur");
        }
        AppointmentType appointmentType = AppointmentType.builder()
                .appointmentTypeName(appointmentTypeCreateRequest.getAppointmentTypeName())
                .time(appointmentTypeCreateRequest.getTime())
                .status(Status.ACTIVE)
                .build();

        appointmentTypeRepository.save(appointmentType);
        return appointmentTypeMapper.toDto(appointmentType);
    }

    public List<AppointmentTypeResponse> read() {
        List<AppointmentType> appointmentTypes = appointmentTypeRepository.findAll();
        return appointmentTypeMapper.toDtos(appointmentTypes);
    }

    public AppointmentType findById(Long id){
        return appointmentTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("bele bir randevu tipi tapilmadi"));
    }

    public AppointmentTypeResponse update(AppointmentTypeUpdateRequest appointmentTypeUpdateRequest) {
        AppointmentType appointmentType = findById(appointmentTypeUpdateRequest.getId());
        updateFieldIfPresent(appointmentTypeUpdateRequest.getAppointmentTypeName(),appointmentType::setAppointmentTypeName);
        updateFieldIfPresent(appointmentTypeUpdateRequest.getTime(),appointmentType::setTime);
        appointmentTypeRepository.save(appointmentType);
        return appointmentTypeMapper.toDto(appointmentType);
    }

    private <T> void updateFieldIfPresent(T newValue, Consumer<T> setter) {
        if (newValue != null && !(newValue instanceof String str && str.isBlank())) {
            setter.accept(newValue);
        }
    }

    @jakarta.transaction.Transactional
    public void delete(Long id) {
        AppointmentType appointmentType = findById(id);
        if (appointmentType.getCalendars() != null) {
            for (com.rustam.modern_dentistry.dao.entity.GeneralCalendar calendar : appointmentType.getCalendars()) {
                if (calendar.getAppointmentTypes() != null) {
                    calendar.getAppointmentTypes().remove(appointmentType);
                }
            }
            appointmentType.getCalendars().clear();
        }
        appointmentTypeRepository.delete(appointmentType);
    }

    public List<AppointmentTypeResponse> search(AppointmentTypeSearchRequest appointmentTypeSearchRequest) {
        List<AppointmentType> appointmentTypes = appointmentTypeRepository.findAll(AppointmentTypeSpecification.filterBy(appointmentTypeSearchRequest));
        return appointmentTypeMapper.toDtos(appointmentTypes);
    }

    public AppointmentTypeResponse statusUpdated(ProductCategoryStatusUpdatedRequest productCategoryStatusUpdatedRequest) {
        AppointmentType appointmentType = findById(productCategoryStatusUpdatedRequest.getId());
        updateFieldIfPresent(productCategoryStatusUpdatedRequest.getStatus(),appointmentType::setStatus);
        appointmentTypeRepository.save(appointmentType);
        return appointmentTypeMapper.toDto(appointmentType);
    }
}
