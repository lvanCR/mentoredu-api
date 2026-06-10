package com.mentoredu.files.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mentoredu.files.dto.ImageFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudinaryImageFileService implements IImageFileService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
        "image/png",  "png",
        "image/jpeg", "jpg"
    );
    private static final byte[] PNG_MAGIC  = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final Cloudinary cloudinary;

    @Value("${app.image.max-size-mb:5}")
    private int maxSizeMb;

    @Override
    public ImageFileResponse upload(MultipartFile file) {
        validateNotEmpty(file);
        validateMimeType(file);
        validateSize(file);
        String ext = ALLOWED_TYPES.get(file.getContentType());
        validateMagicBytes(file, ext);

        String fileUrl = uploadToCloudinary(file);
        return new ImageFileResponse(fileUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @SuppressWarnings("unchecked")
    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "image",
                "folder",        "mentoredu/images",
                "public_id",     UUID.randomUUID().toString()
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");
        } catch (IOException ex) {
            throw new IllegalStateException("Error al subir la imagen a Cloudinary: " + ex.getMessage());
        }
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new InvalidFileTypeException("No se proporcionó un archivo o está vacío.");
    }

    private void validateMimeType(MultipartFile file) {
        if (!ALLOWED_TYPES.containsKey(file.getContentType()))
            throw new InvalidFileTypeException(
                "Solo se aceptan imágenes PNG o JPEG. Tipo recibido: " + file.getContentType());
    }

    private void validateSize(MultipartFile file) {
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes)
            throw new FileSizeLimitExceededException(
                "La imagen supera el tamaño máximo de " + maxSizeMb + " MB.");
    }

    private void validateMagicBytes(MultipartFile file, String ext) {
        try {
            byte[] first = Arrays.copyOf(file.getBytes(), 4);
            boolean valid = "png".equals(ext)
                ? Arrays.equals(first, PNG_MAGIC)
                : first[0] == JPEG_MAGIC[0] && first[1] == JPEG_MAGIC[1] && first[2] == JPEG_MAGIC[2];
            if (!valid)
                throw new InvalidFileTypeException("El archivo no es una imagen PNG o JPEG válida.");
        } catch (IOException ex) {
            throw new InvalidFileTypeException("No se pudo leer el archivo: " + ex.getMessage());
        }
    }
}
