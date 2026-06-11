package com.mentoredu.fileservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mentoredu.fileservice.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements IFileStorageService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public FileUploadResponse store(MultipartFile file, String folder) {
        try {
            String ext      = extractExtension(file.getOriginalFilename());
            String publicId = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "folder",        "mentoredu/" + folder,
                "public_id",     publicId
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            String fileUrl   = (String) result.get("secure_url");
            return new FileUploadResponse(fileUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Error al subir el archivo a Cloudinary: " + ex.getMessage());
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
