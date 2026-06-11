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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        String publicId = null;
        try {
            publicId = extractPublicId(fileUrl);
            String cloudName = cloudinary.config.cloudName;
            String apiKey    = cloudinary.config.apiKey;
            String apiSecret = cloudinary.config.apiSecret;
            long   timestamp = System.currentTimeMillis() / 1000L;

            // Cloudinary signing: params alphabetically sorted, raw (non-URL-encoded) values
            // "public_id" < "timestamp" alphabetically — order is correct
            String paramsToSign = "public_id=" + publicId + "&timestamp=" + timestamp;
            String signature    = sha1Hex(paramsToSign + apiSecret);
            String encodedId    = URLEncoder.encode(publicId, StandardCharsets.UTF_8);

            String downloadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/raw/download"
                               + "?public_id=" + encodedId
                               + "&timestamp=" + timestamp
                               + "&api_key="   + apiKey
                               + "&signature=" + signature;

            log.info("Fetching from Cloudinary API: publicId={}", publicId);

            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
            HttpResponse<byte[]> resp = client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray()
            );

            if (resp.statusCode() != 200) {
                String body = new String(resp.body(), StandardCharsets.UTF_8);
                log.error("Cloudinary download failed: HTTP {} for publicId={} — {}", resp.statusCode(), publicId, body);
                throw new IllegalStateException(
                    "Cloudinary HTTP " + resp.statusCode() + " for: " + publicId + " | " + body);
            }

            log.info("Fetched {} bytes for publicId={}", resp.body().length, publicId);
            return resp.body();

        } catch (IllegalStateException ex) {
            throw ex;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            log.error("IO error fetching from Cloudinary, publicId={}: {}", publicId, ex.getMessage());
            throw new IllegalStateException("IO error al obtener de Cloudinary: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error in fetch, publicId={}: {}", publicId, ex.getMessage(), ex);
            throw new IllegalStateException("Error inesperado en fetch: " + ex.getMessage());
        }
    }

    private String sha1Hex(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(40);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    private String extractPublicId(String cloudinaryUrl) {
        // URL format: https://res.cloudinary.com/{cloud}/raw/upload/v{version}/{public_id}
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length < 2) {
            log.warn("Cannot extract publicId from URL: {}", cloudinaryUrl);
            return cloudinaryUrl;
        }
        return parts[1].replaceFirst("^v\\d+/", "");
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
