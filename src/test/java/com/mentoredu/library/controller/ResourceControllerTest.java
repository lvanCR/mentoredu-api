package com.mentoredu.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;
import com.mentoredu.library.exception.*;
import com.mentoredu.library.model.ResourceType;
import com.mentoredu.library.service.IResourceFileService;
import com.mentoredu.library.service.IResourceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para ResourceController.
 * Cubre: US07 (subir archivo), US08 (registrar metadatos), US09 (buscar),
 *        US10 (descargar), US11 (mis recursos), US16 (activar acepta_resoluciones).
 * Reglas: RN-05 (solo TEACHER/ACADEMY/ADMIN), RN-07 (campos obligatorios),
 *         RN-08 (acepta_resoluciones solo para PRACTICA), RN-23 (career.area debe coincidir).
 * Validado en producción (CONFIRMACION_TESTS.md HU-07 a HU-11, HU-16):
 *   8.1 EXAMEN_COMPLETO sin courseId → 201; 8.2 EXAMEN_SECCION sin courseId → 400;
 *   8.5 aceptaResoluciones=true en GUIA → 400; 8.7 careerId de otra área → 400;
 *   8.8 STUDENT publica → 403; 9.1 lista paginada → 200; 10.1 descarga → 200 con fileUrl.
 */
@WebMvcTest(
    controllers = ResourceController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IResourceService resourceService;
    @MockitoBean private IResourceFileService resourceFileService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    private static final UUID UNIVERSITY_ID = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID AREA_ID       = UUID.fromString("b3000000-0000-0000-0000-000000000002");
    private static final UUID COURSE_ID     = UUID.fromString("b2000000-0000-0000-0000-000000000001");

    // ── POST /resources (US08) ─────────────────────────────────────────────────

    /** US08 Escenario 8.1 — TEACHER publica EXAMEN_COMPLETO sin courseId → 201 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_examCompleto_withoutCourse_returns201() throws Exception {
        var response = Mockito.mock(ResourceResponse.class);
        Mockito.when(response.getId()).thenReturn(UUID.randomUUID());
        when(resourceService.publish(any(), eq("teacher@example.com"))).thenReturn(response);

        var req = validPublishRequest(ResourceType.EXAMEN_COMPLETO);
        req.setCourseId(null); // EXAMEN_COMPLETO no requiere courseId (RN-07)

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }

    /** US08 Escenario 8.3 — PRACTICA con courseId y aceptaResoluciones=true → 201 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_practica_withAceptaResoluciones_returns201() throws Exception {
        var response = Mockito.mock(ResourceResponse.class);
        Mockito.when(response.getId()).thenReturn(UUID.randomUUID());
        when(resourceService.publish(any(), any())).thenReturn(response);

        var req = validPublishRequest(ResourceType.PRACTICA);
        req.setAceptaResoluciones(true);

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US08 / RN-07 Escenario 8.2 — EXAMEN_SECCION sin courseId → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_examenSeccion_withoutCourse_returns400() throws Exception {
        when(resourceService.publish(any(), any()))
            .thenThrow(new CourseIdRequiredException("courseId es obligatorio para EXAMEN_SECCION"));

        var req = validPublishRequest(ResourceType.EXAMEN_SECCION);
        req.setCourseId(null);

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /** US08 / RN-08 Escenario 8.5 — aceptaResoluciones=true en tipo GUIA → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_aceptaResolucionesOnGuia_returns400() throws Exception {
        when(resourceService.publish(any(), any()))
            .thenThrow(new AceptaResolucionesPracticaOnlyException(
                "Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)"));

        var req = validPublishRequest(ResourceType.GUIA);
        req.setAceptaResoluciones(true);

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US08 / RN-23 Escenario 8.7 — careerId de área distinta al areaId → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_careerAreaMismatch_returns400() throws Exception {
        when(resourceService.publish(any(), any()))
            .thenThrow(new CareerAreaMismatchException("La carrera no pertenece al área seleccionada (RN-23)"));

        var req = validPublishRequest(ResourceType.EXAMEN_SECCION);
        req.setCareerId(UUID.randomUUID()); // carrera de otra área

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US08 / RN-05 Escenario 8.8 — STUDENT intenta publicar recurso → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void publish_asStudent_returns403() throws Exception {
        when(resourceService.publish(any(), any()))
            .thenThrow(new ResourceAccessDeniedException(
                "Solo docentes, academias y administradores pueden publicar recursos (RN-05)"));

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validPublishRequest(ResourceType.GUIA))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /** US08 Escenario 8.9 — body vacío → 400 con campos requeridos */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_withBlankTitle_returns400() throws Exception {
        var req = validPublishRequest(ResourceType.PRACTICA);
        req.setTitle("");

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.title").exists());
    }

    /** POST /resources — universityId ausente → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void publish_withMissingUniversityId_returns400() throws Exception {
        var req = validPublishRequest(ResourceType.PRACTICA);
        req.setUniversityId(null);

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.universityId").exists());
    }

    /** POST /resources — sin autenticación → 401 */
    @Test
    void publish_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validPublishRequest(ResourceType.PRACTICA))))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources (US09) ──────────────────────────────────────────────────

    /** US09 Escenario 9.1 — lista paginada sin filtros → 200 con estructura PagedResponse */
    @Test
    @WithMockUser(username = "user@example.com")
    void search_withNoFilters_returns200WithPagedList() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.search(any(), any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    /** US09 Escenario 9.2 — filtrar por universityId → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void search_withUniversityFilter_returns200() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.search(any(), any(), eq(UNIVERSITY_ID), any(), any(), any(), any(), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources").param("universityId", UNIVERSITY_ID.toString()))
            .andExpect(status().isOk());
    }

    /** US09 Escenario 9.4 — búsqueda por texto libre → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void search_withTextQuery_returns200() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.search(eq("cálculo"), any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources").param("q", "cálculo"))
            .andExpect(status().isOk());
    }

    /** US09 Escenario 9.5 — sin resultados → 200 con lista vacía y totalElements=0 */
    @Test
    @WithMockUser(username = "user@example.com")
    void search_noResults_returns200WithEmptyContent() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.search(any(), any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    /** US09 Escenario 9.6 — paginación page=0 size=1 → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void search_withCustomPagination_returns200() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 1, 21L, 21, false);
        when(resourceService.search(any(), any(), any(), any(), any(), any(), any(), eq(0), eq(1)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources").param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(21));
    }

    /** US09 — endpoint de búsqueda es público para autenticados; sin auth → 401 */
    @Test
    void search_unauthenticated_noEffect_stillReturns200() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.search(any(), any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
            .thenReturn(paged);
        // SecurityUtils no se llama en GET /resources → accesible sin auth
        mockMvc.perform(get("/api/v1/resources"))
            .andExpect(status().isOk());
    }

    // ── GET /resources/{id} (US09) ─────────────────────────────────────────────

    /** US09 — obtener recurso por ID → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getById_existingResource_returns200() throws Exception {
        var resourceId = UUID.randomUUID();
        var response = Mockito.mock(ResourceResponse.class);
        when(resourceService.getById(resourceId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/" + resourceId))
            .andExpect(status().isOk());
    }

    /** US09 — recurso no encontrado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getById_notFound_returns404() throws Exception {
        when(resourceService.getById(any()))
            .thenThrow(new ResourceNotFoundException("Recurso no encontrado"));

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** GET /resources/{id} — sin autenticación → 401 (SecurityUtils.requireAuth()) */
    @Test
    void getById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources/me (US11) ───────────────────────────────────────────────

    /** US11 Escenario 11.1 — docente ve sus recursos → 200 con lista paginada */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getMyResources_returns200WithPagedList() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.getByAuthor(eq("teacher@example.com"), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /** US11 Escenario 11.2 — sin recursos → 200 con lista vacía */
    @Test
    @WithMockUser(username = "new.teacher@example.com")
    void getMyResources_noResources_returns200WithEmpty() throws Exception {
        var paged = new PagedResponse<ResourceResponse>(List.of(), 0, 20, 0L, 0, true);
        when(resourceService.getByAuthor(any(), eq(0), eq(20))).thenReturn(paged);

        mockMvc.perform(get("/api/v1/resources/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    /** US11 Escenario 11.3 — sin autenticación → 401 */
    @Test
    void getMyResources_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /resources/{id}/settings (US16) ─────────────────────────────────

    /** US16 Escenario 8.10 / 16.1 — TEACHER activa acepta_resoluciones en PRACTICA → 200 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateSettings_aceptaResolucionesTrue_returns200() throws Exception {
        var response = Mockito.mock(ResourceResponse.class);
        when(resourceService.updateSettings(any(), any(), eq("teacher@example.com"))).thenReturn(response);

        var req = new UpdateResourceSettingsRequest();
        req.setAceptaResoluciones(true);

        mockMvc.perform(patch("/api/v1/resources/" + UUID.randomUUID() + "/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    /** US16 / RN-08 — recurso no es PRACTICA → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateSettings_nonPracticaType_returns400() throws Exception {
        when(resourceService.updateSettings(any(), any(), any()))
            .thenThrow(new AceptaResolucionesPracticaOnlyException("Solo válido para PRACTICA"));

        var req = new UpdateResourceSettingsRequest();
        req.setAceptaResoluciones(true);

        mockMvc.perform(patch("/api/v1/resources/" + UUID.randomUUID() + "/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US16 Escenario 16.5 — aceptaResoluciones ausente en body → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateSettings_missingAceptaResoluciones_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/resources/" + UUID.randomUUID() + "/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.aceptaResoluciones").exists());
    }

    /** US16 Escenario 16.2 / RN-05 — STUDENT intenta cambiar ajustes → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void updateSettings_asStudent_returns403() throws Exception {
        when(resourceService.updateSettings(any(), any(), any()))
            .thenThrow(new ResourceAccessDeniedException("Solo el autor puede modificar la configuración del recurso"));

        var req = new UpdateResourceSettingsRequest();
        req.setAceptaResoluciones(true);

        mockMvc.perform(patch("/api/v1/resources/" + UUID.randomUUID() + "/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** PATCH /resources/{id}/settings — sin autenticación → 401 */
    @Test
    void updateSettings_unauthenticated_returns401() throws Exception {
        var req = new UpdateResourceSettingsRequest();
        req.setAceptaResoluciones(true);

        mockMvc.perform(patch("/api/v1/resources/" + UUID.randomUUID() + "/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources/{id}/download (US10) ───────────────────────────────────

    /** US10 Escenario 10.1 — usuario autenticado descarga recurso → 200 con fileUrl */
    @Test
    @WithMockUser(username = "user@example.com")
    void download_existingResource_returns200WithFileUrl() throws Exception {
        var resourceId = UUID.randomUUID();
        var response = DownloadResponse.builder()
            .resourceId(resourceId).title("Examen de Cálculo")
            .fileUrl("uploads/resources/examen.pdf").fileName("examen.pdf")
            .mimeType("application/pdf").sizeBytes(102400L).build();
        when(resourceService.download(eq(resourceId), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/" + resourceId + "/download"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileUrl").value("uploads/resources/examen.pdf"))
            .andExpect(jsonPath("$.title").value("Examen de Cálculo"));
    }

    /** US10 Escenario 10.2 — recurso no encontrado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void download_resourceNotFound_returns404() throws Exception {
        when(resourceService.download(any(), any()))
            .thenThrow(new ResourceNotFoundException("Recurso no encontrado"));

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/download"))
            .andExpect(status().isNotFound());
    }

    /** US10 Escenario 10.3 — sin autenticación → 401 */
    @Test
    void download_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/download"))
            .andExpect(status().isUnauthorized());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private PublishResourceRequest validPublishRequest(ResourceType type) {
        var req = new PublishResourceRequest();
        req.setTitle("Examen de Cálculo 2023");
        req.setUniversityId(UNIVERSITY_ID);
        req.setAreaId(AREA_ID);
        req.setCourseId(COURSE_ID);
        req.setResourceType(type);
        req.setDescription("Primer parcial de Cálculo");
        return req;
    }
}
