package com.mentoredu.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.catalog.dto.*;
import com.mentoredu.catalog.exception.AreaNotFoundException;
import com.mentoredu.catalog.exception.CourseNotFoundException;
import com.mentoredu.catalog.exception.DuplicateUniversityException;
import com.mentoredu.catalog.exception.UniversityNotFoundException;
import com.mentoredu.catalog.service.ICatalogService;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para CatalogController.
 * Cubre: US28 — Administrar catálogo del sistema.
 * Regla RN-20: solo ADMIN puede crear/modificar entidades del catálogo.
 * Validado en producción (CONFIRMACION_TESTS.md HU-28):
 *   28.1 ADMIN crea universidad → 201; 28.4 asocia curso a área → 204;
 *   28.5 no-ADMIN → 403 (vía @PreAuthorize); 28.6/7/8 GETs → 200.
 *
 * NOTA sobre tests de autenticación:
 * CatalogController NO llama SecurityUtils directamente — usa @PreAuthorize.
 * Con SecurityAutoConfiguration excluida, @PreAuthorize no se aplica.
 * Por ello, los tests de 401 (no autenticado) y 403 (rol incorrecto) solo son
 * confiables para endpoints que llaman SecurityUtils explícitamente. Los demás
 * se testean con roles apropiados para verificar el flujo correcto.
 */
@WebMvcTest(
    controllers = CatalogController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class CatalogControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private ICatalogService catalogService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── GET /catalog/universities (US28 Escenario 28.6) ──────────────────────

    /** US28 Escenario 28.6 — listar universidades → 200 con lista de 8 universidades */
    @Test
    @WithMockUser(username = "user@example.com")
    void listUniversities_authenticated_returns200() throws Exception {
        var uni = UniversityResponse.builder().id(UUID.randomUUID()).name("UNMSM").city("Lima").build();
        when(catalogService.listUniversities()).thenReturn(List.of(uni));

        mockMvc.perform(get("/api/v1/catalog/universities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("UNMSM"));
    }

    /** US28 — lista vacía cuando no hay universidades → 200 con array vacío */
    @Test
    @WithMockUser(username = "user@example.com")
    void listUniversities_empty_returns200WithEmptyArray() throws Exception {
        when(catalogService.listUniversities()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/catalog/universities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ── POST /catalog/universities (US28 Escenario 28.1, RN-20) ──────────────

    /** US28 Escenario 28.1 — ADMIN crea universidad → 201 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createUniversity_byAdmin_returns201() throws Exception {
        var response = UniversityResponse.builder()
            .id(UUID.randomUUID()).name("Nueva Universidad").city("Arequipa").build();
        when(catalogService.createUniversity(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateUniversityRequest("Nueva Universidad", "Arequipa"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Nueva Universidad"));
    }

    /** US28 — universidad con nombre duplicado → 409 Conflict */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createUniversity_duplicateName_returns409() throws Exception {
        when(catalogService.createUniversity(any()))
            .thenThrow(new DuplicateUniversityException("University already exists: UNMSM"));

        mockMvc.perform(post("/api/v1/catalog/universities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateUniversityRequest("UNMSM", "Lima"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US28 — name en blanco → 400 (validación @NotBlank) */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createUniversity_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/universities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateUniversityRequest("", null))))
            .andExpect(status().isBadRequest());
    }

    // ── GET /catalog/universities/{universityId}/areas (US28 Escenario 28.7) ──

    /** US28 Escenario 28.7 — listar áreas por universidad → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listAreas_byUniversity_returns200() throws Exception {
        var universityId = UUID.randomUUID();
        var area = AreaResponse.builder()
            .id(UUID.randomUUID()).universityId(universityId)
            .name("Área B — Ingenierías").build();
        when(catalogService.listAreasByUniversity(universityId)).thenReturn(List.of(area));

        mockMvc.perform(get("/api/v1/catalog/universities/" + universityId + "/areas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Área B — Ingenierías"));
    }

    /** US28 — universidad inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listAreas_universityNotFound_returns404() throws Exception {
        when(catalogService.listAreasByUniversity(any()))
            .thenThrow(new UniversityNotFoundException("University not found"));

        mockMvc.perform(get("/api/v1/catalog/universities/" + UUID.randomUUID() + "/areas"))
            .andExpect(status().isNotFound());
    }

    // ── POST /catalog/universities/{universityId}/areas (US28 Escenario 28.2) ─

    /** US28 Escenario 28.2 — ADMIN crea área en universidad → 201 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createArea_byAdmin_returns201() throws Exception {
        var universityId = UUID.randomUUID();
        var response = AreaResponse.builder()
            .id(UUID.randomUUID()).universityId(universityId).name("Ingenierías").build();
        when(catalogService.createArea(eq(universityId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities/" + universityId + "/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateAreaRequest("Ingenierías", null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Ingenierías"));
    }

    /** US28 — área con name en blanco → 400 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createArea_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/universities/" + UUID.randomUUID() + "/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateAreaRequest("", null))))
            .andExpect(status().isBadRequest());
    }

    // ── GET /catalog/courses (US28) ────────────────────────────────────────────

    /** US28 — listar todos los cursos del catálogo → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listCourses_returns200() throws Exception {
        var course = CourseResponse.builder().id(UUID.randomUUID()).name("Matemática").build();
        when(catalogService.listCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/api/v1/catalog/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Matemática"));
    }

    // ── GET /catalog/areas/{areaId}/courses (US28 Escenario 28.8) ────────────

    /** US28 Escenario 28.8 — listar cursos por área → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listCoursesByArea_returns200() throws Exception {
        var areaId = UUID.randomUUID();
        var course = CourseResponse.builder().id(UUID.randomUUID()).name("Cálculo").build();
        when(catalogService.listCoursesByArea(areaId)).thenReturn(List.of(course));

        mockMvc.perform(get("/api/v1/catalog/areas/" + areaId + "/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Cálculo"));
    }

    /** US28 — área inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listCoursesByArea_areaNotFound_returns404() throws Exception {
        when(catalogService.listCoursesByArea(any()))
            .thenThrow(new AreaNotFoundException("Area not found"));

        mockMvc.perform(get("/api/v1/catalog/areas/" + UUID.randomUUID() + "/courses"))
            .andExpect(status().isNotFound());
    }

    // ── POST /catalog/courses (US28 Escenario 28.3) ───────────────────────────

    /** US28 Escenario 28.3 — ADMIN crea curso → 201 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createCourse_byAdmin_returns201() throws Exception {
        var response = CourseResponse.builder().id(UUID.randomUUID()).name("Química General").build();
        when(catalogService.createCourse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateCourseRequest("Química General", null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Química General"));
    }

    // ── PUT /catalog/areas/{areaId}/courses/{courseId} (US28 Escenario 28.4) ──

    /** US28 Escenario 28.4 — ADMIN asocia curso a área → 204 No Content */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void linkCourseToArea_byAdmin_returns204() throws Exception {
        var areaId   = UUID.randomUUID();
        var courseId = UUID.randomUUID();
        doNothing().when(catalogService).linkCourseToArea(areaId, courseId);

        mockMvc.perform(put("/api/v1/catalog/areas/" + areaId + "/courses/" + courseId))
            .andExpect(status().isNoContent());
    }

    /** US28 — curso inexistente al asociar → 404 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void linkCourseToArea_courseNotFound_returns404() throws Exception {
        doThrow(new CourseNotFoundException("Course not found"))
            .when(catalogService).linkCourseToArea(any(), any());

        mockMvc.perform(put("/api/v1/catalog/areas/" + UUID.randomUUID() + "/courses/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    // ── GET /catalog/universities/{universityId}/careers ──────────────────────

    /** US28 — listar carreras por universidad → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listCareers_byUniversity_returns200() throws Exception {
        var universityId = UUID.randomUUID();
        var career = CareerResponse.builder()
            .id(UUID.randomUUID()).universityId(universityId)
            .areaId(UUID.randomUUID()).name("Ingeniería Civil").build();
        when(catalogService.listCareersByUniversity(universityId)).thenReturn(List.of(career));

        mockMvc.perform(get("/api/v1/catalog/universities/" + universityId + "/careers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Ingeniería Civil"));
    }

    // ── POST /catalog/universities/{universityId}/careers ─────────────────────

    /** US28 — ADMIN crea carrera → 201 */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void createCareer_byAdmin_returns201() throws Exception {
        var universityId = UUID.randomUUID();
        var areaId       = UUID.randomUUID();
        var response = CareerResponse.builder()
            .id(UUID.randomUUID()).universityId(universityId).areaId(areaId)
            .name("Medicina Humana").build();
        when(catalogService.createCareer(eq(universityId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities/" + universityId + "/careers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateCareerRequest(areaId, "Medicina Humana", null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Medicina Humana"));
    }

    // ── PUT /catalog/careers/{careerId}/courses/{courseId} ────────────────────

    /** US28 — ADMIN asocia curso a carrera → 204 No Content */
    @Test
    @WithMockUser(username = "admin@mentoredu.com", roles = "ADMIN")
    void linkCourseToCareer_byAdmin_returns204() throws Exception {
        doNothing().when(catalogService).linkCourseToCareer(any(), any());

        mockMvc.perform(put("/api/v1/catalog/careers/" + UUID.randomUUID() + "/courses/" + UUID.randomUUID()))
            .andExpect(status().isNoContent());
    }
}
