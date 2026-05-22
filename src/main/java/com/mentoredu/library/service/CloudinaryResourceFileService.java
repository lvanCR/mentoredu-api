package com.mentoredu.library.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudinaryResourceFileService implements IResourceFileService {

    private static final String ALLOWED_MIME_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC         = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final Cloudinary cloudinary;

    @Value("${app.file.max-size-mb:20}")
    private int maxSizeMb;

    @Override
    public ResourceFileResponse upload(MultipartFile file) {
        validateNotEmpty(file);
        validateMimeType(file);
        validateSize(file);
        validateMagicBytes(file);

        String fileUrl = uploadToCloudinary(file);
        return new ResourceFileResponse(fileUrl, file.getOriginalFilename(), ALLOWED_MIME_TYPE, file.getSize());
    }

    @SuppressWarnings("unchecked")
    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "folder",        "mentoredu/resources",
                "public_id",     UUID.randomUUID().toString()
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");
        } catch (IOException ex) {
            throw new IllegalStateException("Error al subir el archivo a Cloudinary: " + ex.getMessage());
        }
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("No file was provided or the file is empty.");
        }
    }

    private void validateMimeType(MultipartFile file) {
        if (!ALLOWED_MIME_TYPE.equals(file.getContentType())) {
            throw new InvalidFileTypeException(
                    "Only PDF files are accepted. Received content type: " + file.getContentType());
        }
    }

    private void validateSize(MultipartFile file) {
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileSizeLimitExceededException(
                    "File size exceeds the maximum allowed limit of " + maxSizeMb + " MB.");
        }
    }

    private void validateMagicBytes(MultipartFile file) {
        try {
            byte[] firstBytes = Arrays.copyOf(file.getBytes(), PDF_MAGIC.length);
            if (!Arrays.equals(firstBytes, PDF_MAGIC)) {
                throw new InvalidFileTypeException("The file appears to be corrupted or is not a valid PDF.");
            }
        } catch (IOException ex) {
            throw new InvalidFileTypeException("Failed to read the uploaded file: " + ex.getMessage());
        }
    }
}
