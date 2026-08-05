package com.rustam.modern_dentistry.service;

import com.rustam.modern_dentistry.exception.custom.FileException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    public void writeFile(MultipartFile file, String uploadDir, String newFileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath); // <--- Directory'ni yarat
            }
            Path targetLocation = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileException("File yüklənə bilmədi");
        }
    }

    public void deleteFile(String fullPath) {
        try {
            Path path = Paths.get(fullPath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileException("Fayl silinə bilmədi: " + fullPath);
        }
    }

    public void updateFile(MultipartFile file, String path, String oldFileName, String newFileName) {
        if (file != null && !file.isEmpty()) {
            if (oldFileName != null) {
                deleteFile(path + "/" + oldFileName);
            }
            writeFile(file, path, newFileName);
        }
    }

    public String getNewFileName(MultipartFile file, String fileNameStart) {
        var originalFileName = file != null ? file.getOriginalFilename() : null;
        if (originalFileName == null || !originalFileName.contains(".")) {
            return fileNameStart + UUID.randomUUID() + ".jpg";
        }
        var dotIndex = StringUtils.cleanPath(originalFileName).lastIndexOf('.');
        if (dotIndex < 0) {
            return fileNameStart + UUID.randomUUID() + ".jpg";
        }
        var fileExtension = originalFileName.substring(dotIndex);
        return fileNameStart + UUID.randomUUID() + fileExtension;
    }

    public void checkFileIfExist(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileException("Fayl boşdur və ya null-dır.");
        }
    }

    public void checkFileIfExist(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new FileException("Fayl boşdur və ya null-dır.");
        }
    }

    public void checkVideoFile(MultipartFile file) {
        // Fayl tipini yoxlamaq
        if (Objects.requireNonNull(file.getContentType()).startsWith("video/")) {
            throw new FileException("Yalnız video faylları qəbul olunur!");
        }

        // Fayl ölçüsünü yoxlamaq (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileException("Fayl çox böyükdür (max 10MB)!");
        }
    }
}
