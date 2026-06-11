package com.mentoredu.files.service;

import com.mentoredu.files.dto.ImageFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Service
public class ImageFileService implements IImageFileService {

    private final RestClient restClient;

    public ImageFileService(@Value("${app.file-service.base-url}") String fileServiceBaseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(90));
        this.restClient = RestClient.builder()
            .baseUrl(fileServiceBaseUrl)
            .requestFactory(factory)
            .build();
    }

    @Override
    public ImageFileResponse upload(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            byte[] bytes = file.getBytes();
            body.add("file", new ByteArrayResource(bytes) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            });

            Map<?, ?> response = restClient.post()
                .uri("/api/files/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

            return new ImageFileResponse(
                (String) response.get("fileUrl"),
                (String) response.get("fileName"),
                (String) response.get("mimeType"),
                ((Number) response.get("sizeBytes")).longValue()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo para enviarlo al file-service: " + ex.getMessage());
        }
    }
}
