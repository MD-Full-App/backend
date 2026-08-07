package com.rustam.modern_dentistry.mapper.laboratory;

import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus;
import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import com.rustam.modern_dentistry.dto.request.DentalOrderCreateReq;
import com.rustam.modern_dentistry.dto.request.update.UpdateTechnicianOrderReq;
import com.rustam.modern_dentistry.dto.response.read.DentalOrderTeethListResponse;
import com.rustam.modern_dentistry.dto.response.read.DentalOrderToothDetailResponse;
import com.rustam.modern_dentistry.dto.response.read.TechnicianOrderResponse;
import com.rustam.modern_dentistry.service.TechnicianService;
import com.rustam.modern_dentistry.service.settings.teeth.TeethService;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.constants.Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.rustam.modern_dentistry.util.constants.Directory.pathDentalOrder;

@Component
@RequiredArgsConstructor
public class DentalOrderMapper {
    private final UtilService utilService;
    private final TeethService teethService;
    private final TechnicianService technicianService;

    public DentalOrder toEntity(DentalOrderCreateReq request) {
        return DentalOrder.builder()
                .checkDate(request.getCheckDate())
                .deliveryDate(request.getDeliveryDate())
                .description(request.getDescription())
                .orderDentureInfo(request.getOrderDentureInfo())
                .dentalWorkType(DentalWorkType.valueOf(request.getDentalWorkType()))
                .dentalWorkStatus(DentalWorkStatus.PENDING)
                .build();
    }

    public TechnicianOrderResponse toResponse(DentalOrder o) {
        return TechnicianOrderResponse.builder()
                .id(o.getId())
                .checkDate(o.getCheckDate())
                .deliveryDate(o.getDeliveryDate())
                .description(o.getDescription())
                .orderDentureInfo(o.getOrderDentureInfo() != null ? o.getOrderDentureInfo() : null)
                .dentalWorkType(o.getDentalWorkType())
                .dentalWorkStatus(o.getDentalWorkStatus())
                .price(o.getPrice())
                .toothDetails(o.getToothDetails().stream().map(
                        toothDetail -> DentalOrderToothDetailResponse.builder()
                                .colorId(toothDetail.getColor() != null ? toothDetail.getColor().getId() : null)
                                .colorName(toothDetail.getColor() != null ? toothDetail.getColor().getName() : null)
                                .metalId(toothDetail.getMetal() != null ? toothDetail.getMetal().getId() : null)
                                .metalName(toothDetail.getMetal() != null ? toothDetail.getMetal().getName() : null)
                                .ceramicId(toothDetail.getCeramic() != null ? toothDetail.getCeramic().getId() : null)
                                .ceramicName(toothDetail.getCeramic() != null ? toothDetail.getCeramic().getName() : null)
                                .toothSection(toothDetail.getToothSection())
                                .build()
                ).toList())
                .teethList(o.getTeethList().stream().map(
                        tooth -> {
                            return DentalOrderTeethListResponse.builder()
                                    .id(tooth.getId())
                                    .toothNo(tooth.getToothNo())
                                    .toothType(tooth.getToothType().toString())
                                    .toothLocation(tooth.getToothLocation().toString())
                                    .build();
                        }
                ).toList())
                .doctor(o.getBaseUser().getName() + " " + o.getBaseUser().getSurname())
                .patient(o.getPatient().getName() + " " + o.getPatient().getSurname())
                .technician(o.getTechnician().getName() + " " + o.getTechnician().getSurname())
                .urls(o.getImagePaths().stream().map(
                                fileName -> Directory.getUrl(pathDentalOrder, fileName))
                        .collect(Collectors.toList()))
                .build();
    }

    public void updateEntity(DentalOrder entity, UpdateTechnicianOrderReq req) {

        if (req.getCheckDate() != null) {
            entity.setCheckDate(req.getCheckDate());
        }

        if (req.getDeliveryDate() != null) {
            entity.setDeliveryDate(req.getDeliveryDate());
        }

        if (req.getDescription() != null) {
            entity.setDescription(req.getDescription());
        }

        if (req.getOrderType() != null) {
            entity.setDentalWorkType(DentalWorkType.valueOf(req.getOrderType()));
        }

        if (req.getOrderDentureInfo() != null) {
            entity.setOrderDentureInfo(req.getOrderDentureInfo()); // TODO check elemeyi yaz
        }

        if (req.getDoctorId() != null) {
            var doctor = utilService.findByBaseUserId(req.getDoctorId());
            entity.setBaseUser(doctor);
        }

        if (req.getTechnicianId() != null) {
            var technician = technicianService.getTechnicianById(req.getTechnicianId());
            entity.setTechnician(technician);
        }

        if (req.getPatientId() != null) {
            var patient = utilService.findByPatientId(req.getPatientId());
            entity.setPatient(patient);
        }

        if (req.getTeethList() != null) {
            var teeth = teethService.findAllById(req.getTeethList());
            entity.setTeethList(teeth);
        }

        entity.setDentalWorkStatus(DentalWorkStatus.PENDING);
    }
}
