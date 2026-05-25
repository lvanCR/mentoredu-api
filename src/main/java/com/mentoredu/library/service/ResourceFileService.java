package com.mentoredu.library.service;

import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
@Profile("!prod")
public class ResourceFileService implements IResourceFileService {

    private static final String ALLOWED_MIME_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC         = "%PDF".getBytes(StandardCharsets.US_ASCII);

    @Value("${app.file.upload-dir:uploads/resources}")
    private String uploadDir;

    @Value("${app.file.max-size-mb:20}")
    private int maxSizeMb;

    @Override
    public ResourceFileResponse upload(MultipartFile file) {
        validateNotEmpty(file);
        validateMimeType(file);
        validateSize(file);
        validateMagicBytes(file);

        String storedName = UUID.randomUUID() + ".pdf";
        String fileUrl    = storeFile(file, storedName);

        return new ResourceFileResponse(fileUrl, file.getOriginalFilename(), ALLOWED_MIME_TYPE, file.getSize());
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("No se proporcionó un archivo o está vacío.");
        }
    }

    private void validateMimeType(MultipartFile file) {
        if (!ALLOWED_MIME_TYPE.equals(file.getContentType())) {
            throw new InvalidFileTypeException(
                    "Solo se aceptan archivos PDF. Tipo recibido: " + file.getContentType());
        }
    }

    private void validateSize(MultipartFile file) {
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileSizeLimitExceededException(
                    "El archivo supera el tamaño máximo permitido de " + maxSizeMb + " MB.");
        }
    }

    private void validateMagicBytes(MultipartFile file) {
        try {
            byte[] firstBytes = Arrays.copyOf(file.getBytes(), PDF_MAGIC.length);
            if (!Arrays.equals(firstBytes, PDF_MAGIC)) {
                throw new InvalidFileTypeException("El archivo parece estar corrupto o no es un PDF válido.");
            }
        } catch (IOException ex) {
            throw new InvalidFileTypeException("No se pudo leer el archivo cargado: " + ex.getMessage());
        }
    }

    private String storeFile(MultipartFile file, String storedName) {
        try {
            Path dir  = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(storedName);
            Files.write(dest, file.getBytes());
            return uploadDir + "/" + storedName;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo almacenar el archivo: " + ex.getMessage());
        }
    }
}
