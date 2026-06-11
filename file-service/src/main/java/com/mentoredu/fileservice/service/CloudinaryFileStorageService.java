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
            Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "folder",        "mentoredu/" + folder,
                "public_id",     UUID.randomUUID().toString()
            );
            Map<?, ?> result  = cloudinary.uploader().upload(file.getBytes(), options);
            String fileUrl    = (String) result.get("secure_url");
            return new FileUploadResponse(fileUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Error al subir el archivo a Cloudinary: " + ex.getMessage());
        }
    }
}
