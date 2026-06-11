package com.mentoredu.fileservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mentoredu.fileservice.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
                "public_id",     publicId,
                "access_mode",   "public"
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            String fileUrl   = (String) result.get("secure_url");
            return new FileUploadResponse(fileUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Error al subir el archivo a Cloudinary: " + ex.getMessage());
        }
    }

    @Override
    public byte[] fetch(String fileUrl) {
        try {
            log.info("Fetching file: {}", fileUrl);
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
            HttpResponse<byte[]> resp = client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray()
            );
            if (resp.statusCode() != 200) {
                String body = new String(resp.body(), StandardCharsets.UTF_8);
                log.error("Fetch failed: HTTP {} for {} — {}", resp.statusCode(), fileUrl, body);
                throw new IllegalStateException("HTTP " + resp.statusCode() + " fetching: " + fileUrl);
            }
            log.info("Fetched {} bytes from {}", resp.body().length, fileUrl);
            return resp.body();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("IO error fetching {}: {}", fileUrl, ex.getMessage());
            throw new IllegalStateException("IO error al obtener archivo: " + ex.getMessage());
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
