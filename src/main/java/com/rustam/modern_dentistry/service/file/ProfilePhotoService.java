package com.rustam.modern_dentistry.service.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ProfilePhotoService {

    @Value("${app.upload.dir:./uploads/profiles}")
    String uploadDir;

    /**
     * Upload profile photo - saves file organized by userId
     * @param file MultipartFile to upload
     * @param userId User ID for file organization
     * @return Path relative to uploads folder (e.g., "uploads/profiles/uuid.jpg")
     * @throws IOException if file operations fail
     */
    public String uploadProfilePhoto(MultipartFile file, UUID userId) throws IOException {
        if (file == null || file.isEmpty()) {
            System.out.println("⚠️ No file provided");
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        System.out.println("📸 File content-type: " + contentType);
        
        if (contentType == null || (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/png") && 
            !contentType.equals("image/webp") &&
            !contentType.equals("image/jpg"))) {
            System.out.println("❌ File type not allowed: " + contentType);
            throw new IllegalArgumentException("Only JPG, PNG, and WebP images are allowed. Got: " + contentType);
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            System.out.println("❌ File too large: " + file.getSize() + " bytes");
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        try {
            // Extract file extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            System.out.println("📝 File extension: " + extension);

            // Create upload directory if doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            System.out.println("📁 Upload directory: " + uploadPath.toAbsolutePath());

            // Save file with userId as filename (overwrites previous photo)
            String filename = userId + extension;
            Path filePath = uploadPath.resolve(filename);
            System.out.println("💾 Saving to: " + filePath.toAbsolutePath());

            // Transfer file (REPLACE_EXISTING will overwrite old photo)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ File saved successfully: " + filePath.toAbsolutePath());

            // Return relative path for database
            String relativePath = "uploads/profiles/" + filename;
            System.out.println("📤 Returning path: " + relativePath);
            log.info("Profile photo uploaded successfully: {} ({})", relativePath, file.getSize());
            return relativePath;

        } catch (IOException e) {
            System.out.println("❌ IO Error: " + e.getMessage());
            e.printStackTrace();
            log.error("Error saving profile photo for user {}: {}", userId, e.getMessage());
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
    }

    /**
     * Save profile photo from base64 (backward compatibility)
     * @param file MultipartFile to upload
     * @return Path relative to uploads folder
     */
    public String saveProfilePhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        return "uploads/profiles/" + filename;
    }

    public byte[] getProfilePhoto(String photoPath) throws IOException {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }
        
        // Handle path that may include "uploads/profiles/" prefix
        String cleanPath = photoPath.replace("uploads/profiles/", "");
        Path filePath = Paths.get(uploadDir).resolve(cleanPath);
        
        System.out.println("📖 Reading photo from: " + filePath.toAbsolutePath());
        if (!Files.exists(filePath)) {
            System.out.println("❌ File not found: " + filePath.toAbsolutePath());
            throw new IOException("File not found: " + photoPath);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteProfilePhoto(String photoPath) throws IOException {
        if (photoPath == null || photoPath.isEmpty()) {
            return;
        }
        
        // Handle path that may include "uploads/profiles/" prefix
        String cleanPath = photoPath.replace("uploads/profiles/", "");
        Path filePath = Paths.get(uploadDir).resolve(cleanPath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("✅ File deleted: " + filePath.toAbsolutePath());
            log.info("Profile photo deleted: {}", photoPath);
        }
    }

    public Path getPhotoPath(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }
        String cleanPath = photoPath.replace("uploads/profiles/", "");
        return Paths.get(uploadDir).resolve(cleanPath);
    }

    public boolean photoExists(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return false;
        }
        Path filePath = getPhotoPath(photoPath);
        return filePath != null && Files.exists(filePath);
    }
}