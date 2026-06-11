package com.mentoredu.library.service;

import com.mentoredu.library.dto.ResourceFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ResourceFileService implements IResourceFileService {

    private final RestClient restClient;

    @Value("${app.file-service.base-url}")
    private String fileServiceBaseUrl;

    public ResourceFileService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public ResourceFileResponse upload(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            byte[] bytes = file.getBytes();
            body.add("file", new ByteArrayResource(bytes) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            });

            Map<?, ?> response = restClient.post()
                .uri(fileServiceBaseUrl + "/api/files/pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

            return new ResourceFileResponse(
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
