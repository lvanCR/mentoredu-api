package com.mentoredu.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.exception.DuplicateResourceException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.model.ResourceType;
import com.mentoredu.library.service.IResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    // US07+US08 — Publish resource
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withAllRequiredFields_returns201() throws Exception {
        var request = validRequest();
        when(resourceService.publish(any(), eq("user@example.com")))
                .thenReturn(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Examen UNI 2024"))
                .andExpect(jsonPath("$.resourceType").value("EXAMEN_SECCION"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void publishResource_withOptionalDescription_returns201() throws Exception {
        var request = validRequest();
        request.setDescription("Examen oficial de admisión UNI ciclo 2024-I");

        when(resourceService.publish(any(), eq("teacher@example.com")))
                .thenReturn(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withMissingTitle_returns400() throws Exception {
        var request = validRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withMissingUniversityId_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "areaId": "%s",
                  "courseId": "%s",
                  "resourceType": "EXAMEN_SECCION"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.universityId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withMissingAreaId_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "universityId": "%s",
                  "courseId": "%s",
                  "resourceType": "EXAMEN_SECCION"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.areaId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withMissingResourceType_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "universityId": "%s",
                  "areaId": "%s",
                  "courseId": "%s"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.resourceType").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_withInvalidResourceType_returns400() throws Exception {
        String body = """
                {
                  "title": "Examen UNI 2024",
                  "universityId": "%s",
                  "areaId": "%s",
                  "courseId": "%s",
                  "resourceType": "INVALID_TYPE"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void publishResource_whenDuplicate_returns409() throws Exception {
        var request = validRequest();

        when(resourceService.publish(any(), eq("user@example.com")))
                .thenThrow(new DuplicateResourceException("Resource with this title already published."));

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void publishResource_withoutAuth_returns401() throws Exception {
        var request = validRequest();

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US09 — Search resources
    // =========================================================================

    @Test
    void searchResources_withNoFilters_returns200WithResults() throws Exception {
        when(resourceService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(pageOf(List.of(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION))));

        mockMvc.perform(get("/api/v1/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Examen UNI 2024"))
                .andExpect(jsonPath("$.content[0].resourceType").value("EXAMEN_SECCION"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchResources_withAllFilters_returns200() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId       = UUID.randomUUID();
        UUID courseId     = UUID.randomUUID();

        when(resourceService.search(eq("UNI"), eq("EXAMEN_SECCION"), eq(universityId), eq(areaId), isNull(), eq(courseId), anyInt(), anyInt()))
                .thenReturn(pageOf(List.of(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION))));

        mockMvc.perform(get("/api/v1/resources")
                        .param("q", "UNI")
                        .param("type", "EXAMEN_SECCION")
                        .param("universityId", universityId.toString())
                        .param("areaId", areaId.toString())
                        .param("courseId", courseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Examen UNI 2024"));
    }

    @Test
    void searchResources_withNoMatches_returns200EmptyList() throws Exception {
        when(resourceService.search(eq("XYZ_NO_EXISTE"), isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(pageOf(List.of()));

        mockMvc.perform(get("/api/v1/resources")
                        .param("q", "XYZ_NO_EXISTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void searchResources_withoutAuth_returns200() throws Exception {
        when(resourceService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(pageOf(List.of()));

        mockMvc.perform(get("/api/v1/resources"))
                .andExpect(status().isOk());
    }

    @Test
    void searchResources_withInvalidUuidUniversityId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/resources")
                        .param("universityId", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void searchResources_withInvalidUuidAreaId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/resources")
                        .param("areaId", "123-abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    // =========================================================================
    // GET /resources/{id}
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getResourceById_whenExists_returns200() throws Exception {
        UUID id = UUID.randomUUID();

        when(resourceService.getById(eq(id)))
                .thenReturn(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION));

        mockMvc.perform(get("/api/v1/resources/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Examen UNI 2024"))
                .andExpect(jsonPath("$.resourceType").value("EXAMEN_SECCION"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getResourceById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(resourceService.getById(eq(id)))
                .thenThrow(new ResourceNotFoundException("Resource not found: " + id));

        mockMvc.perform(get("/api/v1/resources/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found: " + id));
    }

    @Test
    void getResourceById_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // PATCH /resources/{id}/settings (US16 Escenario 2)
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void updateSettings_activateAceptaResoluciones_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        String body = "{\"aceptaResoluciones\": true}";

        when(resourceService.updateSettings(eq(id), any(), eq("user@example.com")))
                .thenReturn(buildResponse("Práctica UNI", ResourceType.PRACTICA));

        mockMvc.perform(patch("/api/v1/resources/{id}/settings", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateSettings_resourceNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        String body = "{\"aceptaResoluciones\": true}";

        when(resourceService.updateSettings(eq(id), any(), eq("user@example.com")))
                .thenThrow(new ResourceNotFoundException("Resource not found: " + id));

        mockMvc.perform(patch("/api/v1/resources/{id}/settings", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void updateSettings_studentForbidden_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        String body = "{\"aceptaResoluciones\": true}";

        when(resourceService.updateSettings(eq(id), any(), eq("student@example.com")))
                .thenThrow(new com.mentoredu.library.exception.ResourceAccessDeniedException(
                        "Solo docentes, academias y administradores pueden activar aceptaResoluciones (RN-05)"));

        mockMvc.perform(patch("/api/v1/resources/{id}/settings", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateSettings_nonPracticaType_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        String body = "{\"aceptaResoluciones\": true}";

        when(resourceService.updateSettings(eq(id), any(), eq("teacher@example.com")))
                .thenThrow(new IllegalArgumentException("Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)"));

        mockMvc.perform(patch("/api/v1/resources/{id}/settings", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateSettings_missingField_returns400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/resources/{id}/settings", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.aceptaResoluciones").exists());
    }

    @Test
    void updateSettings_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/resources/{id}/settings", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptaResoluciones\": true}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // GET /resources/me (US11)
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyResources_whenAuthenticated_returns200() throws Exception {
        when(resourceService.getByAuthor(eq("user@example.com"), anyInt(), anyInt()))
                .thenReturn(pageOf(List.of(buildResponse("Examen UNI 2024", ResourceType.EXAMEN_SECCION))));

        mockMvc.perform(get("/api/v1/resources/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Examen UNI 2024"));
    }

    @Test
    void getMyResources_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // GET /resources/{id}/download (US10)
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void downloadResource_whenExists_returns200WithFileUrl() throws Exception {
        UUID id = UUID.randomUUID();
        var response = DownloadResponse.builder()
                .resourceId(id)
                .title("Examen UNI 2024")
                .fileUrl("uploads/resources/examen.pdf")
                .fileName("examen.pdf")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .build();
        when(resourceService.download(eq(id), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(id.toString()))
                .andExpect(jsonPath("$.fileUrl").value("uploads/resources/examen.pdf"))
                .andExpect(jsonPath("$.fileName").value("examen.pdf"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void downloadResource_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(resourceService.download(eq(id), eq("user@example.com")))
                .thenThrow(new ResourceNotFoundException("Resource not found: " + id));

        mockMvc.perform(get("/api/v1/resources/{id}/download", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void downloadResource_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/{id}/download", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private PublishResourceRequest validRequest() {
        var r = new PublishResourceRequest();
        r.setTitle("Examen UNI 2024");
        r.setUniversityId(UUID.randomUUID());
        r.setAreaId(UUID.randomUUID());
        r.setCourseId(UUID.randomUUID());
        r.setResourceType(ResourceType.EXAMEN_SECCION);
        r.setFileUrl("uploads/resources/test.pdf");
        r.setFileName("examen.pdf");
        r.setMimeType("application/pdf");
        r.setSizeBytes(1024L);
        return r;
    }

    private ResourceResponse buildResponse(String title, ResourceType resourceType) {
        User author = User.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("user@example.com")
                .build();

        Resource resource = Resource.builder()
                .id(UUID.randomUUID())
                .title(title)
                .resourceType(resourceType)
                .universityId(UUID.randomUUID())
                .areaId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .fileUrl("uploads/resources/test.pdf")
                .fileName("examen.pdf")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .aceptaResoluciones(false)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();

        return new ResourceResponse(resource);
    }

    private <T> PagedResponse<T> pageOf(List<T> items) {
        return new PagedResponse<>(items, 0, 20, items.size(), items.isEmpty() ? 0 : 1, true);
    }
}
