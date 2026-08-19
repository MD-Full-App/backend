no 7.2                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DentalOrderMapper.java
package com.rustam.modern_dentistry.mapper.laboratory;

import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkStatus;
import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import com.rustam.modern_dentistry.dto.request.DentalOrderCreateReq;
import com.rustam.modern_dentistry.dto.request.update.UpdateTechnicianOrderReq;
import com.rustam.modern_dentistry.dao.entity.settings.teeth.Teeth;
import com.rustam.modern_dentistry.dto.response.read.DentalOrderTeethListResponse;
import com.rustam.modern_dentistry.dto.response.read.DentalOrderToothDetailResponse;
import com.rustam.modern_dentistry.dto.response.read.TechnicianOrderResponse;
import com.rustam.modern_dentistry.service.TechnicianService;
import com.rustam.modern_dentistry.service.settings.teeth.TeethService;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.constants.Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
                .isBridge(request.getIsBridge())
                .startTooth(request.getStartTooth())
                .endTooth(request.getEndTooth())
                .build();
    }

    public TechnicianOrderResponse toResponse(DentalOrder o) {
        Long metalId = null;
        String metalName = null;
        Long ceramicId = null;
        String ceramicName = null;
        Long colorId = null;
        String colorName = null;
        String doctorName = o.getBaseUser() != null ? safeName(o.getBaseUser().getName(), o.getBaseUser().getSurname()) : null;
        String technicianName = o.getTechnician() != null ? safeName(o.getTechnician().getName(), o.getTechnician().getSurname()) : null;
        String patientName = o.getPatient() != null ? safeName(o.getPatient().getName(), o.getPatient().getSurname()) : null;
        List<Teeth> teethList = o.getTeethList() != null ? o.getTeethList() : List.of();
        List<String> imagePaths = o.getImagePaths() != null ? o.getImagePaths() : List.of();

        if (o.getToothDetails() != null) {
            for (var detail : o.getToothDetails()) {
                if (detail.getMetal() != null && metalId == null) {
                    metalId = detail.getMetal().getId();
                    metalName = detail.getMetal().getName();
                }
                if (detail.getCeramic() != null && ceramicId == null) {
                    ceramicId = detail.getCeramic().getId();
                    ceramicName = detail.getCeramic().getName();
                }
                if (detail.getColor() != null && colorId == null) {
                    colorId = detail.getColor().getId();
                    colorName = detail.getColor().getName();
                }
            }
        }

        if (colorId == null && o.getOrderDentureInfo() != null && o.getOrderDentureInfo().getColor() != null) {
            try {
                colorId = Long.parseLong(o.getOrderDentureInfo().getColor());
            } catch (NumberFormatException ignored) {}
        }

        String description = o.getDescription();
        String metalWork = "";
        String ceramicWork = "";
        String report = description;

        if (description != null && description.contains(" | ")) {
            String[] parts = description.split(" \\| ");
            String parsedMetal = "";
            String parsedCeramic = "";
            String parsedReport = "";
            for (String part : parts) {
                if (part.startsWith("Metal işi: ")) {
                    parsedMetal = part.substring("Metal işi: ".length());
                } else if (part.startsWith("Keramikanın işi: ")) {
                    parsedCeramic = part.substring("Keramikanın işi: ".length());
                } else if (part.startsWith("Hesabat: ")) {
                    parsedReport = part.substring("Hesabat: ".length());
                }
            }
            if (!parsedMetal.isEmpty() || !parsedCeramic.isEmpty() || !parsedReport.isEmpty()) {
                metalWork = parsedMetal;
                ceramicWork = parsedCeramic;
                report = parsedReport;
            }
        }

        return TechnicianOrderResponse.builder()
                .id(o.getId())
                .checkDate(o.getCheckDate())
                .deliveryDate(o.getDeliveryDate())
                .description(o.getDescription())
                .orderDentureInfo(o.getOrderDentureInfo() != null ? o.getOrderDentureInfo() : null)
                .dentalWorkType(o.getDentalWorkType())
                .dentalWorkStatus(o.getDentalWorkStatus())
                .price(o.getPrice())
                .isBridge(o.getIsBridge())
                .startTooth(o.getStartTooth())
                .endTooth(o.getEndTooth())
                .toothDetails(o.getToothDetails() != null ? o.getToothDetails().stream().map(
                    toothDetail -> DentalOrderToothDetailResponse.builder()
                        .colorId(toothDetail.getColor() != null ? toothDetail.getColor().getId() : null)
                        .colorName(toothDetail.getColor() != null ? toothDetail.getColor().getName() : null)
                        .metalId(toothDetail.getMetal() != null ? toothDetail.getMetal().getId() : null)
                        .metalName(toothDetail.getMetal() != null ? toothDetail.getMetal().getName() : null)
                        .ceramicId(toothDetail.getCeramic() != null ? toothDetail.getCeramic().getId() : null)
                        .ceramicName(toothDetail.getCeramic() != null ? toothDetail.getCeramic().getName() : null)
                        .toothSection(toothDetail.getToothSection())
                        .build()
                ).toList() : List.of())
                .teethList(teethList.stream().map(
                        tooth -> DentalOrderTeethListResponse.builder()
                                .id(tooth.getId())
                                .toothNo(tooth.getToothNo())
                                .toothType(tooth.getToothType() != null ? tooth.getToothType().toString() : null)
                                .toothLocation(tooth.getToothLocation() != null ? tooth.getToothLocation().toString() : null)
                                .build()
                ).toList())
                .doctor(doctorName)
                .patient(patientName)
                .technician(technicianName)
                .urls(imagePaths.stream().filter(java.util.Objects::nonNull).map(
                                fileName -> Directory.getUrl(pathDentalOrder, fileName))
                        .collect(Collectors.toList()))
                .metalId(metalId)
                .metalName(metalName)
                .ceramicId(ceramicId)
                .ceramicName(ceramicName)
                .colorId(colorId)
                .colorName(colorName)
                .metalWork(metalWork)
                .ceramicWork(ceramicWork)
                .report(report)
                .build();
    }

    private String safeName(String firstName, String lastName) {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
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

        if (req.getIsBridge() != null) {
            entity.setIsBridge(req.getIsBridge());
        }
        if (req.getStartTooth() != null) {
            entity.setStartTooth(req.getStartTooth());
        }
        if (req.getEndTooth() != null) {
            entity.setEndTooth(req.getEndTooth());
        }

        entity.setDentalWorkStatus(DentalWorkStatus.PENDING);
    }
}
