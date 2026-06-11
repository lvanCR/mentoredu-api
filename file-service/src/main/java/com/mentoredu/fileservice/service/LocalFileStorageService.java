package com.mentoredu.fileservice.service;

import com.mentoredu.fileservice.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("!prod")
public class LocalFileStorageService implements IFileStorageService {

    @Value("${app.file.upload-dir:uploads/files}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Override
    public FileUploadResponse store(MultipartFile file, String folder) {
        try {
            String ext        = extractExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path dir          = Paths.get(uploadDir, folder);
            Files.createDirectories(dir);
            Files.write(dir.resolve(storedName), file.getBytes());
            String fileUrl = baseUrl + "/" + uploadDir + "/" + folder + "/" + storedName;
            return new FileUploadResponse(fileUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo almacenar el archivo: " + ex.getMessage());
        }
    }

    @Override
    public byte[] fetch(String fileUrl) {
        try {
            String path = new URL(fileUrl).getPath(); // /uploads/files/resources/uuid.pdf
            if (path.startsWith("/")) path = path.substring(1);
            return Files.readAllBytes(Paths.get(path));
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL de archivo inválida: " + fileUrl);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo local: " + ex.getMessage());
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) return "";
        return originalName.substring(originalName.lastIndexOf('.') + 1);
    }
}
