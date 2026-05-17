package com.mentoredu.library.service;

import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import com.mentoredu.library.model.ResourceFile;
import com.mentoredu.library.repository.ResourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class ResourceFileService implements IResourceFileService {

    private static final String ALLOWED_MIME_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC         = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final ResourceFileRepository resourceFileRepository;

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

        ResourceFile entity = ResourceFile.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .mimeType(ALLOWED_MIME_TYPE)
                .sizeBytes(file.getSize())
                .build();

        return new ResourceFileResponse(resourceFileRepository.save(entity));
    }

    // -------------------------------------------------------------------------
    // Validations
    // -------------------------------------------------------------------------

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("No file was provided or the file is empty.");
        }
    }

    private void validateMimeType(MultipartFile file) {
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPE.equals(mimeType)) {
            throw new InvalidFileTypeException(
                    "Only PDF files are accepted. Received content type: " + mimeType);
        }
    }

    private void validateSize(MultipartFile file) {
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileSizeLimitExceededException(
                    "File size exceeds the maximum allowed limit of " + maxSizeMb + " MB. "
                    + "Received: " + (file.getSize() / (1024 * 1024)) + " MB.");
        }
    }

    private void validateMagicBytes(MultipartFile file) {
        try {
            byte[] firstBytes = Arrays.copyOf(file.getBytes(), PDF_MAGIC.length);
            if (!Arrays.equals(firstBytes, PDF_MAGIC)) {
                throw new InvalidFileTypeException(
                        "The file appears to be corrupted or is not a valid PDF.");
            }
        } catch (IOException ex) {
            throw new InvalidFileTypeException("Failed to read the uploaded file: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Storage
    // -------------------------------------------------------------------------

    private String storeFile(MultipartFile file, String storedName) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(storedName);
            Files.write(dest, file.getBytes());
            return uploadDir + "/" + storedName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store the uploaded file: " + ex.getMessage());
        }
    }
}
