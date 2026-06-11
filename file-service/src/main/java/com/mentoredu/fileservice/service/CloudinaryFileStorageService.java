package com.mentoredu.fileservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mentoredu.fileservice.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
            String publicId = extractPublicId(fileUrl);
            // Signed URL valid 15 minutes — bypasses account-level access restrictions
            String signedUrl = cloudinary.url()
                .resourceType("raw")
                .type("upload")
                .signed(true)
                .generate(publicId);

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(signedUrl))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                throw new IllegalStateException(
                    "Cloudinary respondió con HTTP " + resp.statusCode() + " al descargar: " + publicId);
            }
            return resp.body();
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Error al obtener el archivo de Cloudinary: " + ex.getMessage());
        }
    }

    private String extractPublicId(String cloudinaryUrl) {
        // https://res.cloudinary.com/{cloud}/raw/upload/v{ver}/{folder}/{uuid.ext}
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length < 2) return cloudinaryUrl;
        return parts[1].replaceFirst("^v\\d+/", "");
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
