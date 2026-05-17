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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    // US14 — Search resources by filters
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: búsqueda sin filtros devuelve resultados → 200
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withNoFilters_returns200WithResults() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(resourceService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(buildResponse(fileId, "Examen UNI 2024", "EXAM", "PUBLIC")));

        mockMvc.perform(get("/api/v1/resources/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Examen UNI 2024"))
                .andExpect(jsonPath("$[0].type").value("EXAM"))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"));
    }

    // -------------------------------------------------------------------------
    // Escenario exitoso: búsqueda con todos los filtros válidos → 200
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withAllFilters_returns200() throws Exception {
        UUID institutionId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(resourceService.search(eq("UNI"), eq("EXAM"), eq("PUBLIC"),
                eq(institutionId), eq(subjectId), eq(2024)))
                .thenReturn(List.of(buildResponse(fileId, "Examen UNI 2024", "EXAM", "PUBLIC")));

        mockMvc.perform(get("/api/v1/resources/search")
                        .param("q", "UNI")
                        .param("type", "EXAM")
                        .param("visibility", "PUBLIC")
                        .param("institutionId", institutionId.toString())
                        .param("subjectId", subjectId.toString())
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Examen UNI 2024"));
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo exitoso: filtros válidos sin coincidencias → 200 lista vacía
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withNoMatches_returns200EmptyList() throws Exception {
        when(resourceService.search(eq("XYZ_NO_EXISTE"), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resources/search")
                        .param("q", "XYZ_NO_EXISTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // Acceso sin autenticación → 200 (endpoint público, US14)
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withoutAuth_returns200() throws Exception {
        when(resourceService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resources/search"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Escenario error: tipo de recurso inválido → 400
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withInvalidType_returns400() throws Exception {
        when(resourceService.search(isNull(), eq("INVALIDO"), isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException(
                        "Invalid resource type: 'INVALIDO'. Allowed values: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER"));

        mockMvc.perform(get("/api/v1/resources/search")
                        .param("type", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: filtros inconsistentes — visibility=PRIVATE → 400
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withPrivateVisibility_returns400() throws Exception {
        when(resourceService.search(isNull(), isNull(), eq("PRIVATE"), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException(
                        "Invalid visibility filter: 'PRIVATE'. Allowed values for search: PUBLIC, PREMIUM"));

        mockMvc.perform(get("/api/v1/resources/search")
                        .param("visibility", "PRIVATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: año fuera de rango → 400
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withYearOutOfRange_returns400() throws Exception {
        when(resourceService.search(isNull(), isNull(), isNull(), isNull(), isNull(), eq(1800)))
                .thenThrow(new IllegalArgumentException(
                        "Year must be between 1900 and 2099. Received: 1800"));

        mockMvc.perform(get("/api/v1/resources/search")
                        .param("year", "1800"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario error: UUID inválido en institutionId → 400
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withInvalidUuidInstitutionId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/resources/search")
                        .param("institutionId", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario error: UUID inválido en subjectId → 400
    // -------------------------------------------------------------------------

    @Test
    void searchResources_withInvalidUuidSubjectId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/resources/search")
                        .param("subjectId", "123-abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
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
