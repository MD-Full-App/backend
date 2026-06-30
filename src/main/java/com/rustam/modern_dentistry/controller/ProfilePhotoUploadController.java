package com.rustam.modern_dentistry.controller;

import com.rustam.modern_dentistry.service.file.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling profile photo file uploads
 * Accepts multipart/form-data requests with image files
 */
@RestController
@RequestMapping(path = "/api/v1/profile/photo")
@RequiredArgsConstructor
public class ProfilePhotoUploadController {

    private final ProfilePhotoService profilePhotoService;

    /**
     * Upload a profile photo for a user
     * @param file The image file to upload (JPG, PNG, WebP, max 5MB)
     * @param userId The user ID to associate with the photo
     * @return Response with the saved file path
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        
        try {
            System.out.println("\n========== 📸 PHOTO UPLOAD START ==========");
            System.out.println("📸 User ID: " + userId);
            System.out.println("📸 File name: " + file.getOriginalFilename());
            System.out.println("📸 File size: " + file.getSize() + " bytes");
            System.out.println("📸 Content-Type: " + file.getContentType());
            System.out.println("📸 Is empty: " + file.isEmpty());

            // Validate userId format
            if (userId == null || userId.trim().isEmpty()) {
                System.out.println("❌ User ID is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "User ID cannot be empty"
                        ));
            }

            UUID userUUID;
            try {
                userUUID = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid UUID format: " + userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid user ID format: " + userId
                        ));
            }

            String photoPath = profilePhotoService.uploadProfilePhoto(file, userUUID);

            if (photoPath == null) {
                System.out.println("❌ Photo path is null after upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Failed to save photo - path is null"
                        ));
            }

            System.out.println("✅ Upload successful!");
            System.out.println("✅ Photo path: " + photoPath);
            System.out.println("========== 📸 PHOTO UPLOAD END ==========\n");
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "Photo uploaded successfully",
                            "photoPath", photoPath,
                            "photoUrl", "/" + photoPath
                    ));

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Validation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "Upload validation failed: " + e.getMessage()
                    ));
        } catch (Exception e) {
            System.out.println("❌ Upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error uploading photo: " + e.getMessage()
                    ));
        }
    }
}
