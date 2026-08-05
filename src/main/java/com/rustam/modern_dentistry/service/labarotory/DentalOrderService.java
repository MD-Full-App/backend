package com.rustam.modern_dentistry.service.labarotory;

import com.rustam.modern_dentistry.dao.entity.enums.DentalWorkType;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrderToothDetail;
import com.rustam.modern_dentistry.dao.entity.settings.Ceramic;
import com.rustam.modern_dentistry.dao.entity.settings.Color;
import com.rustam.modern_dentistry.dao.entity.settings.Metal;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dao.repository.laboratory.DentalOrderRepository;
import com.rustam.modern_dentistry.dao.repository.settings.CeramicRepository;
import com.rustam.modern_dentistry.dao.repository.settings.ColorRepository;
import com.rustam.modern_dentistry.dao.repository.settings.MetalRepository;
import com.rustam.modern_dentistry.dto.request.DentalOrderCreateReq;
import com.rustam.modern_dentistry.dto.request.create.DentalOrderToothDetailIds;
import com.rustam.modern_dentistry.dto.request.update.UpdateLabOrderStatus;
import com.rustam.modern_dentistry.dto.request.update.UpdateOrderPrice;
import com.rustam.modern_dentistry.dto.request.update.UpdateTechnicianOrderReq;
import com.rustam.modern_dentistry.dto.response.read.TechnicianOrderResponse;
import com.rustam.modern_dentistry.exception.custom.CustomError;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.laboratory.DentalOrderMapper;
import com.rustam.modern_dentistry.service.FileService;
import com.rustam.modern_dentistry.service.TechnicianService;
import com.rustam.modern_dentistry.service.settings.teeth.TeethService;
import com.rustam.modern_dentistry.util.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rustam.modern_dentistry.util.constants.Directory.pathDentalOrder;

@Service
@RequiredArgsConstructor
public class DentalOrderService {
    private final UtilService utilService;
    private final FileService fileService;
    private final TeethService teethService;
    private final TechnicianService technicianService;
    private final DentalOrderMapper dentalOrderMapper;
    private final DentalOrderRepository dentalOrderRepository;
    private final ColorRepository colorRepository;
    private final MetalRepository metalRepository;
    private final CeramicRepository ceramicRepository;

    @Transactional
    public void create(DentalOrderCreateReq request) {
        List<String> imagePaths = new ArrayList<>();
        List<MultipartFile> processedFiles = new ArrayList<>();
        try {
            var entity = dentalOrderMapper.toEntity(request);
            List<com.rustam.modern_dentistry.dao.entity.settings.teeth.Teeth> teeth = request.getTeethList() != null && !request.getTeethList().isEmpty() ? teethService.findAllById(request.getTeethList()) : Collections.emptyList();
            var doctor = request.getDoctorId() != null && !request.getDoctorId().isBlank() ? utilService.findByBaseUserId(request.getDoctorId()) : null;
            var technician = request.getTechnicianId() != null ? technicianService.getTechnicianById(request.getTechnicianId()) : null;
            var patient = request.getPatientId() != null ? utilService.findByPatientId(request.getPatientId()) : null;
            List<DentalOrderToothDetail> toothDetails = request.getToothDetailIds() != null ? getToothDetails(request.getToothDetailIds(), entity) : Collections.emptyList();
            entity.setTeethList(teeth);
            entity.setBaseUser(doctor);
            entity.setTechnician(technician);
            entity.setPatient(patient);
            entity.setOrderDentureInfo(request.getOrderDentureInfo()); // TODO check elemeyi yaz
            entity.setToothDetails(toothDetails);

            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                for (String base64File : request.getFiles()) {
                    try {
                        // Diaqnostika (istəyə bağlı)
                        diagnoseBase64Encoding(base64File);

                        // Base64-u MultipartFile-a çevirin
                        MultipartFile multipartFile = convertBase64ToMultipartFile(base64File);
                        processedFiles.add(multipartFile);

                        // Fayl adını yaradın
                        var newFileName = getNewFileName(multipartFile, patient);
                        imagePaths.add(newFileName);

                        // Faylı yazın
                        fileService.writeFile(multipartFile, pathDentalOrder, newFileName);
                    } catch (Exception fileConversionEx) {
                        // Hər bir fayl üçün ayrıca xəta idarəetməsi
                        throw new CustomError("Failed to process file: " + fileConversionEx.getMessage());
                    }
                }
            }

            entity.setImagePaths(imagePaths);
            dentalOrderRepository.save(entity);
        } catch (Exception ex) {
            // Yüklənmiş faylları silin
            imagePaths.forEach(path -> {
                var fullPath = pathDentalOrder + "/" + path;
                fileService.deleteFile(fullPath);
            });
            throw new CustomError(ex.getMessage());
        }
    }

    private void diagnoseBase64Encoding(String base64String) {
        try {
            System.out.println("Original Base64 String: " + base64String);

            String cleanBase64 = base64String
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "")
                    .trim();

            System.out.println("Cleaned Base64 String: " + cleanBase64);

            if (cleanBase64.contains("base64,")) {
                String[] parts = cleanBase64.split("base64,");
                System.out.println("MIME Type: " + parts[0]);
                System.out.println("Base64 Data Length: " + parts[1].length());
            }

            Base64.getDecoder().decode(cleanBase64);
        } catch (Exception e) {
            System.err.println("Base64 Diagnosis Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


private MultipartFile convertBase64ToMultipartFile(String base64String) {
    try {
        String cleanBase64 = base64String
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")
            .trim();

        String mimeType = "application/octet-stream";
        String base64Data = cleanBase64;

        if (cleanBase64.contains("base64,")) {
            String[] parts = cleanBase64.split("base64,");
            if (parts.length > 1) {
                mimeType = parts[0].replace("data:", "").replace(";", "");
                base64Data = parts[1];
            }
        }
        base64Data = base64Data.trim();

        base64Data = base64Data.replaceAll("[^A-Za-z0-9+/=]", "");

        byte[] fileContent = Base64.getDecoder().decode(base64Data);

        String fileName = switch (mimeType) {
            case "application/pdf" -> "document.pdf";
            case "image/jpeg" -> "image.jpg";
            case "image/png" -> "image.png";
            default -> "uploaded_file";
        };

        return new MockMultipartFile(
            "file",
            fileName,
            mimeType,
            fileContent
        );
    } catch (IllegalArgumentException e) {
        throw new CustomError(
            "Invalid Base64 encoding: " + 
            "Ensure the file is properly encoded. " + 
            "Original error: " + e.getMessage()
        );
    }
}

    @Transactional(readOnly = true)
    public List<TechnicianOrderResponse> read() {
        // 1. First, load orders without toothDetails and teethDetails
        List<DentalOrder> orders = dentalOrderRepository.findAllWithBasicRelations();

        // 2. Then, load toothDetails (this will enrich the same objects) and teethDetails
        // To avoid the "cannot simultaneously fetch multiple bags" error
        orders.forEach(order -> {
            if (order.getToothDetails() != null) {
                order.getToothDetails().size(); // Lazy yükləmə üçün
            }
            if (order.getTeethList() != null) {
                order.getTeethList().size(); // Lazy yükləmə üçün
            }
        });


        return orders.stream().map(dentalOrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TechnicianOrderResponse readById(Long id) {
        var order = dentalOrderRepository.findByIdWithBasicRelations(id).orElseThrow(
                () -> new NotFoundException("Dental Order Not Found")
        );
        dentalOrderRepository.fetchTeethList(order);
        dentalOrderRepository.fetchToothDetails(order);
        return dentalOrderMapper.toResponse(order);
    }

    public void updateOrderStatus(UpdateLabOrderStatus request) {
        var dentalOrder = getDentalOrder(request.getId());
        dentalOrder.setDentalWorkStatus(request.getDentalWorkStatus());
        dentalOrderRepository.save(dentalOrder);
    }

    @Transactional(readOnly = true)
    public List<TechnicianOrderResponse> getTechnicianOrdersByUUID() {
        // When the technician logs in, to see their own orders.
        var currentUserId = utilService.getCurrentUserId();
        var user = utilService.findByBaseUserId(currentUserId);

        // 1. First, load imagePaths
        List<DentalOrder> orders = dentalOrderRepository.findAllWithTechnicianId(UUID.fromString(user.getId()));

        // 2. Then, load toothDetails (this will enrich the same objects) and teethDetails
        // To avoid the "cannot simultaneously fetch multiple bags" error
        if (!orders.isEmpty()) {
            dentalOrderRepository.fetchToothDetails(orders);
            dentalOrderRepository.fetchTeethList(orders);
        }

        return orders.stream().map(dentalOrderMapper::toResponse).toList();
    }

    @Transactional
    public void update(UpdateTechnicianOrderReq request) {
        var entity = getDentalOrder(request.getId());

        List<String> originalImagePaths = new ArrayList<>(entity.getImagePaths()); // Mövcud şəkillər
        List<String> tempSavedFiles = new ArrayList<>(); // Müvəqqəti saxlanılan fayllar
        var updatedImagePaths = new ArrayList<>(originalImagePaths.stream()
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .toList());

        try {
            // 1. Silinəcək faylları siyahıdan çıxarın
            if (request.getDeleteFiles() != null) {
                var deleteFiles = request.getDeleteFiles().stream()
                        .map(url -> url.substring(url.lastIndexOf("/") + 1))
                        .toList();
                updatedImagePaths.removeAll(deleteFiles);
            }

            // 2. Yeni faylları əlavə edin (Base64 dəstəyi ilə)
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                for (String base64File : request.getFiles()) {
                    // Base64-u MultipartFile-a çevirin
                    MultipartFile multipartFile = convertBase64ToMultipartFile(base64File);

                    String newFileName = getNewFileName(multipartFile, entity.getPatient());
                    fileService.writeFile(multipartFile, pathDentalOrder, newFileName);

                    tempSavedFiles.add(newFileName);
                    updatedImagePaths.add(newFileName);
                }
            }

            // 3. Entity-ni yeniləyin
            dentalOrderMapper.updateEntity(entity, request);
            entity.setImagePaths(updatedImagePaths);

            // Diş detallarını yeniləyin
            if (request.getToothDetailIds() != null) {
                entity.getToothDetails().clear(); // Köhnələri silin
                var newDetails = getToothDetails(request.getToothDetailIds(), entity);
                entity.getToothDetails().addAll(newDetails); // Yeniləri əlavə edin
            }

            dentalOrderRepository.save(entity); // Tranzaksiya uğurlu

            // 4. Silinəcək faylları sistemdən silin
            if (request.getDeleteFiles() != null) {
                var deleteFiles = request.getDeleteFiles().stream()
                        .map(url -> url.substring(url.lastIndexOf("/") + 1))
                        .toList();
                deleteFiles.forEach(fileName -> fileService.deleteFile(pathDentalOrder + "/" + fileName));
            }

        } catch (Exception ex) {
            // Müvəqqəti saxlanılan faylları silin
            tempSavedFiles.forEach(fileName -> fileService.deleteFile(pathDentalOrder + "/" + fileName));
            throw new CustomError(ex.getMessage());
        }
    }


    public void delete(Long id) {
        var dentalOrder = dentalOrderRepository.findByIdWithBasicRelations(id).orElseThrow(
                () -> new NotFoundException("Dental Order Not Found")
        );
        dentalOrderRepository.delete(dentalOrder);
        dentalOrder.getImagePaths().forEach(
                fileName -> fileService.deleteFile(pathDentalOrder + "/" + fileName)
        );
    }

    public void setOrderPrice(Long id, UpdateOrderPrice request) {
        var dentalOrder = getDentalOrder(id);
        dentalOrder.setPrice(request.getPrice());
        dentalOrderRepository.save(dentalOrder);
    }


    private DentalOrder getDentalOrder(Long id) {
        return dentalOrderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Dental Order Not Found")
        );
    }

    private String getNewFileName(MultipartFile file, Patient patient) {
        return fileService.getNewFileName(file,
                patient.getName() + "_"
                + patient.getSurname() + "_");
    }

    private List<DentalOrderToothDetail> getToothDetails(List<DentalOrderToothDetailIds> request, DentalOrder entity) {
        if (request == null) {
            return null;
        }

        List<DentalOrderToothDetail> toothDetails = new ArrayList<>();

        // Collect all non-null IDs
        Set<Long> colorIds = new HashSet<>();
        Set<Long> metalIds = new HashSet<>();
        Set<Long> ceramicIds = new HashSet<>();

        for (var detailReq : request) {
            if (detailReq.getColorId() != null) colorIds.add(detailReq.getColorId());
            if (detailReq.getMetalId() != null) metalIds.add(detailReq.getMetalId());
            if (detailReq.getCeramicId() != null) ceramicIds.add(detailReq.getCeramicId());
        }

        // Fetch from the DB in bulk
        Map<Long, Color> colorMap = colorIds.isEmpty() ? Collections.emptyMap() :
                colorRepository.findAllById(colorIds).stream().collect(Collectors.toMap(Color::getId, Function.identity()));
        Map<Long, Metal> metalMap = metalIds.isEmpty() ? Collections.emptyMap() :
                metalRepository.findAllById(metalIds).stream().collect(Collectors.toMap(Metal::getId, Function.identity()));
        Map<Long, Ceramic> ceramicMap = ceramicIds.isEmpty() ? Collections.emptyMap() :
                ceramicRepository.findAllById(ceramicIds).stream().collect(Collectors.toMap(Ceramic::getId, Function.identity()));

        // For each detail, assign present objects
        for (var detailReq : request) {
            Color color = detailReq.getColorId() != null ? colorMap.get(detailReq.getColorId()) : null;
            Metal metal = detailReq.getMetalId() != null ? metalMap.get(detailReq.getMetalId()) : null;
            Ceramic ceramic = detailReq.getCeramicId() != null ? ceramicMap.get(detailReq.getCeramicId()) : null;

            if (color != null || metal != null || ceramic != null) {
                DentalOrderToothDetail detail = new DentalOrderToothDetail();
                detail.setDentalOrder(entity);
                detail.setColor(color);
                detail.setMetal(metal);
                detail.setCeramic(ceramic);
                toothDetails.add(detail);
            }
        }

        return toothDetails;
    }

    public List<DentalWorkType> readByDentalWorkType() {
        return Arrays.asList(DentalWorkType.values());
    }
}