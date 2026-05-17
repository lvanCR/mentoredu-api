package com.mentoredu.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.exception.DuplicateResourceException;
import com.mentoredu.library.exception.ResourceFileNotFoundException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.service.IResourceService;
import com.mentoredu.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResourceController.class)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IResourceService resourceService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US13 — Register resource metadata
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: todos los campos obligatorios → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withAllRequiredFields_returns201() throws Exception {
        UUID fileId = UUID.randomUUID();
        var request = validRequest(fileId);
        when(resourceService.publish(any(), eq("user@example.com")))
                .thenReturn(buildResponse(fileId, "Examen UNI 2024", "EXAM", "PUBLIC"));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Examen UNI 2024"))
                .andExpect(jsonPath("$.type").value("EXAM"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo exitoso: recurso listo para búsqueda → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void registerResource_withOptionalFields_returns201() throws Exception {
        UUID fileId = UUID.randomUUID();
        var request = validRequest(fileId);
        request.setDescription("Examen oficial de admisión UNI ciclo 2024-I");
        request.setExamCycle("2024-I");
        request.setVisibility("PREMIUM");

        when(resourceService.publish(any(), eq("teacher@example.com")))
                .thenReturn(buildResponse(fileId, "Examen UNI 2024", "EXAM", "PREMIUM"));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("PREMIUM"))
                .andExpect(jsonPath("$.fileId").value(fileId.toString()));
    }

    // -------------------------------------------------------------------------
    // Escenario error: falta campo obligatorio (title) → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingTitle_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setTitle("");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingFileId_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "institutionId": "%s",
                  "subjectId": "%s",
                  "year": 2024,
                  "type": "EXAM"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.fileId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingInstitutionId_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "fileId": "%s",
                  "subjectId": "%s",
                  "year": 2024,
                  "type": "EXAM"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.institutionId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingSubjectId_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "fileId": "%s",
                  "institutionId": "%s",
                  "year": 2024,
                  "type": "EXAM"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.subjectId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingYear_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "fileId": "%s",
                  "institutionId": "%s",
                  "subjectId": "%s",
                  "type": "EXAM"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.year").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withMissingType_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "fileId": "%s",
                  "institutionId": "%s",
                  "subjectId": "%s",
                  "year": 2024
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.type").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: año fuera de rango → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withYearBelowMin_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setYear(1800);

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.year").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withYearAboveMax_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setYear(2100);

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.year").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withInvalidType_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setType("INVALID_TYPE");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.type").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_withInvalidVisibility_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setVisibility("GRATIS");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.visibility").exists());
    }

    // -------------------------------------------------------------------------
    // fileId no existe en resource_files → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_whenFileIdNotFound_returns404() throws Exception {
        UUID missingFileId = UUID.randomUUID();
        var request = validRequest(missingFileId);

        when(resourceService.publish(any(), eq("user@example.com")))
                .thenThrow(new ResourceFileNotFoundException(
                        "Resource file not found: " + missingFileId));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource file not found: " + missingFileId));
    }

    // -------------------------------------------------------------------------
    // RN-14: recurso duplicado (mismo fileId) → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void registerResource_whenFileAlreadyUsed_returns409() throws Exception {
        UUID duplicateFileId = UUID.randomUUID();
        var request = validRequest(duplicateFileId);

        when(resourceService.publish(any(), eq("user@example.com")))
                .thenThrow(new DuplicateResourceException(
                        "A resource already exists for file: " + duplicateFileId));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "A resource already exists for file: " + duplicateFileId));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void registerResource_withoutAuth_returns401() throws Exception {
        var request = validRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private PublishResourceRequest validRequest(UUID fileId) {
        var r = new PublishResourceRequest();
        r.setTitle("Examen UNI 2024");
        r.setFileId(fileId);
        r.setInstitutionId(UUID.randomUUID());
        r.setSubjectId(UUID.randomUUID());
        r.setYear(2024);
        r.setType("EXAM");
        return r;
    }

    private ResourceResponse buildResponse(UUID fileId, String title, String type, String visibility) {
        User author = User.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("user@example.com")
                .build();

        AcademicResource resource = AcademicResource.builder()
                .id(UUID.randomUUID())
                .title(title)
                .type(type)
                .visibility(visibility)
                .fileId(fileId)
                .institutionId(UUID.randomUUID())
                .subjectId(UUID.randomUUID())
                .year(2024)
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return new ResourceResponse(resource);
    }
}
