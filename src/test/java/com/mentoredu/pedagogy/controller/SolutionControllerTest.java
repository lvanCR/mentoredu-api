package com.mentoredu.pedagogy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.exception.DuplicateSolutionException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.service.ISolutionService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para SolutionController.
 * Cubre: US17 (ver resoluciones), US18 (enviar resolución), US20 (ver mi resolución).
 * Reglas: RN-09 (1 resolución por ejercicio por estudiante → 409),
 *         RN-10 (solo autor o TEACHER asociado a ACADEMY → 403).
 * Validado en producción (CONFIRMACION_TESTS.md HU-17/HU-18/HU-20):
 *   18.1 envío exitoso → 201 con Location; 18.2 duplicado → 409;
 *   18.3 sin aceptaResoluciones → 403; 18.5 TEACHER no puede enviar → 403 (BUG #4 fix);
 *   18.6 sin auth → 401; 17.1 autor ve resoluciones → 200; 17.3 student ajeno → 403;
 *   20.1 ver mi resolución → 200; 20.3 sin resolución → 404.
 * Nota: SolutionResponse, FeedbackResponse y MySolutionWithFeedbackResponse son records con @Builder.
 */
@WebMvcTest(
    controllers = SolutionController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class SolutionControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private ISolutionService solutionService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /resources/{resourceId}/solutions (US18) ─────────────────────────

    /** US18 Escenario 18.1 — STUDENT envía resolución exitosamente → 201 con Location */
    @Test
    @WithMockUser(username = "student@example.com")
    void submit_validContent_returns201WithLocation() throws Exception {
        var resourceId = UUID.randomUUID();
        var response = SolutionResponse.builder()
            .id(UUID.randomUUID()).resourceId(resourceId).studentId(UUID.randomUUID())
            .content("Mi resolución paso a paso...").status("SUBMITTED")
            .submittedAt(LocalDateTime.now()).build();
        when(solutionService.submit(eq(resourceId), any(), eq("student@example.com"))).thenReturn(response);

        var req = new SubmitSolutionRequest(null, "Mi resolución paso a paso...");
        mockMvc.perform(post("/api/v1/resources/" + resourceId + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SUBMITTED"))
            .andExpect(header().exists("Location"));
    }

    /** US18 — resolución con fileUrl → 201 */
    @Test
    @WithMockUser(username = "student@example.com")
    void submit_withFileUrl_returns201() throws Exception {
        var resourceId = UUID.randomUUID();
        var response = SolutionResponse.builder()
            .id(UUID.randomUUID()).resourceId(resourceId)
            .fileUrl("uploads/resources/sol.pdf").status("SUBMITTED")
            .submittedAt(LocalDateTime.now()).build();
        when(solutionService.submit(eq(resourceId), any(), any())).thenReturn(response);

        var req = new SubmitSolutionRequest("uploads/resources/sol.pdf", null);
        mockMvc.perform(post("/api/v1/resources/" + resourceId + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US18 / RN-09 Escenario 18.2 — segunda resolución → 409 Conflict */
    @Test
    @WithMockUser(username = "student@example.com")
    void submit_duplicateSolution_returns409() throws Exception {
        when(solutionService.submit(any(), any(), any()))
            .thenThrow(new DuplicateSolutionException("Ya enviaste una resolución para este ejercicio"));

        var req = new SubmitSolutionRequest(null, "Segundo intento");
        mockMvc.perform(post("/api/v1/resources/" + UUID.randomUUID() + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US18 Escenario 18.3 — recurso sin aceptaResoluciones=true → 403 Forbidden */
    @Test
    @WithMockUser(username = "student@example.com")
    void submit_resourceNotAcceptingSolutions_returns403() throws Exception {
        when(solutionService.submit(any(), any(), any()))
            .thenThrow(new SolutionAccessDeniedException("Este recurso no acepta resoluciones"));

        var req = new SubmitSolutionRequest(null, "Resolución");
        mockMvc.perform(post("/api/v1/resources/" + UUID.randomUUID() + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** US18 Escenario 18.5 — BUG #4 fix: TEACHER intenta enviar resolución → 403.
     * SolutionService.submit() valida que el usuario tenga rol STUDENT. */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_asTeacher_returns403() throws Exception {
        when(solutionService.submit(any(), any(), any()))
            .thenThrow(new SolutionAccessDeniedException("Solo los estudiantes pueden enviar resoluciones"));

        var req = new SubmitSolutionRequest(null, "Intento de teacher");
        mockMvc.perform(post("/api/v1/resources/" + UUID.randomUUID() + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** US18 Escenario 18.6 — sin autenticación → 401 */
    @Test
    void submit_unauthenticated_returns401() throws Exception {
        var req = new SubmitSolutionRequest(null, "Resolución");
        mockMvc.perform(post("/api/v1/resources/" + UUID.randomUUID() + "/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources/{resourceId}/solutions/mine (US20) ─────────────────────

    /** US20 Escenario 20.1 — ver mi resolución con feedback (o sin él) → 200 */
    @Test
    @WithMockUser(username = "student@example.com")
    void getMine_existingSolution_returns200() throws Exception {
        var resourceId = UUID.randomUUID();
        var solution = SolutionResponse.builder()
            .id(UUID.randomUUID()).resourceId(resourceId)
            .status("SUBMITTED").submittedAt(LocalDateTime.now()).build();
        var response = new MySolutionWithFeedbackResponse(solution, null);
        when(solutionService.getMyWithFeedback(eq(resourceId), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/" + resourceId + "/solutions/mine"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.solution.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.feedback").doesNotExist());
    }

    /** US20 Escenario 20.3 — sin resolución enviada → 404 */
    @Test
    @WithMockUser(username = "student@example.com")
    void getMine_noSolution_returns404() throws Exception {
        when(solutionService.getMyWithFeedback(any(), any()))
            .thenThrow(new SolutionNotFoundException("No has enviado resolución para este ejercicio"));

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions/mine"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** GET /solutions/mine — sin autenticación → 401 */
    @Test
    void getMine_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions/mine"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources/{resourceId}/solutions (US17) ──────────────────────────

    /** US17 Escenario 17.1/17.2 — autor ve lista de resoluciones → 200 con array */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void list_asAuthor_returns200WithList() throws Exception {
        var resourceId = UUID.randomUUID();
        var solution = SolutionResponse.builder()
            .id(UUID.randomUUID()).resourceId(resourceId)
            .status("SUBMITTED").submittedAt(LocalDateTime.now()).build();
        when(solutionService.listByResource(eq(resourceId), eq("teacher@example.com")))
            .thenReturn(List.of(solution));

        mockMvc.perform(get("/api/v1/resources/" + resourceId + "/solutions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    /** US17 Escenario 17.2 — ejercicio sin resoluciones → 200 con lista vacía */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void list_noSolutions_returns200WithEmpty() throws Exception {
        when(solutionService.listByResource(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /** US17 / RN-10 Escenario 17.3 — student ajeno no puede ver resoluciones → 403 */
    @Test
    @WithMockUser(username = "other.student@example.com")
    void list_asNonAuthor_returns403() throws Exception {
        when(solutionService.listByResource(any(), any()))
            .thenThrow(new SolutionAccessDeniedException("Solo el autor del ejercicio puede ver las resoluciones (RN-10)"));

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions"))
            .andExpect(status().isForbidden());
    }

    /** US17 Escenario 17.4 — sin autenticación → 401 */
    @Test
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /resources/{resourceId}/solutions/{solutionId} (US17) ────────────

    /** US17 — obtener resolución específica → 200 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getById_validSolution_returns200() throws Exception {
        var resourceId = UUID.randomUUID();
        var solutionId = UUID.randomUUID();
        var response = SolutionResponse.builder()
            .id(solutionId).resourceId(resourceId)
            .status("SUBMITTED").submittedAt(LocalDateTime.now()).build();
        when(solutionService.getById(eq(resourceId), eq(solutionId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/" + resourceId + "/solutions/" + solutionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solutionId.toString()));
    }

    /** US17 — resolución inexistente → 404 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getById_notFound_returns404() throws Exception {
        when(solutionService.getById(any(), any(), any()))
            .thenThrow(new SolutionNotFoundException("Resolución no encontrada"));

        mockMvc.perform(get("/api/v1/resources/" + UUID.randomUUID() + "/solutions/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
