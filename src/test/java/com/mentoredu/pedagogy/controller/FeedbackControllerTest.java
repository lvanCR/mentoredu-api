package com.mentoredu.pedagogy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.pedagogy.dto.CreateFeedbackRequest;
import com.mentoredu.pedagogy.dto.FeedbackResponse;
import com.mentoredu.pedagogy.exception.FeedbackAlreadyExistsException;
import com.mentoredu.pedagogy.exception.FeedbackNotFoundException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.service.IFeedbackService;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para FeedbackController.
 * Cubre: US19 — Dar feedback correctivo, US20 — Obtener feedback de una resolución.
 * Reglas: RN-10 (solo autor del ejercicio da feedback → 403),
 *         RN-11 (feedback inmutable → 409 si ya existe),
 *         RN-22 (score entre 0.0 y 10.0 → 400 si fuera de rango).
 * Validado en producción (CONFIRMACION_TESTS.md HU-19):
 *   19.1 feedback con score → 201; 19.2 sin score (null válido) → 201;
 *   19.3 feedback duplicado → 409; 19.4 student da feedback → 403;
 *   19.5 score > 10 → 400; 19.6 sin auth → 401.
 */
@WebMvcTest(
    controllers = FeedbackController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class FeedbackControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IFeedbackService feedbackService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /solutions/{solutionId}/feedback (US19) ──────────────────────────

    /** US19 Escenario 19.1 — TEACHER da feedback con score → 201 Created */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_withScoreAndBody_returns201() throws Exception {
        var solutionId = UUID.randomUUID();
        var response = Mockito.mock(FeedbackResponse.class);
        when(feedbackService.create(eq(solutionId), any(), eq("teacher@example.com"))).thenReturn(response);

        var req = new CreateFeedbackRequest(BigDecimal.valueOf(8.5), "Muy buena resolución, corrige la parte 3");
        mockMvc.perform(post("/api/v1/solutions/" + solutionId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US19 Escenario 19.2 — feedback sin score (score=null es válido) → 201 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_withNullScore_returns201() throws Exception {
        var solutionId = UUID.randomUUID();
        var response = Mockito.mock(FeedbackResponse.class);
        when(feedbackService.create(eq(solutionId), any(), any())).thenReturn(response);

        var req = new CreateFeedbackRequest(null, "Buen trabajo, revisa las derivadas");
        mockMvc.perform(post("/api/v1/solutions/" + solutionId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US19 / RN-11 Escenario 19.3 — feedback duplicado (ya existe para esta solución) → 409 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_duplicateFeedback_returns409() throws Exception {
        when(feedbackService.create(any(), any(), any()))
            .thenThrow(new FeedbackAlreadyExistsException("Ya existe feedback para esta resolución (RN-11)"));

        var req = new CreateFeedbackRequest(BigDecimal.valueOf(7.0), "Segundo intento de feedback");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US19 / RN-10 Escenario 19.4 — STUDENT intenta dar feedback → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void create_asStudent_returns403() throws Exception {
        when(feedbackService.create(any(), any(), any()))
            .thenThrow(new SolutionAccessDeniedException("Solo el autor del ejercicio puede dar feedback (RN-10)"));

        var req = new CreateFeedbackRequest(BigDecimal.valueOf(5.0), "Mi propio feedback");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /** US19 / RN-22 Escenario 19.5 — score fuera de rango (> 10.0) → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_scoreTooHigh_returns400() throws Exception {
        var req = new CreateFeedbackRequest(BigDecimal.valueOf(11.0), "Score mayor a 10");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.score").exists());
    }

    /** US19 / RN-22 — score negativo (< 0.0) → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_scoreNegative_returns400() throws Exception {
        var req = new CreateFeedbackRequest(BigDecimal.valueOf(-1.0), "Score negativo");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.score").exists());
    }

    /** US19 — body en blanco → 400 (validación @NotBlank) */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_blankBody_returns400() throws Exception {
        var req = new CreateFeedbackRequest(BigDecimal.valueOf(8.0), "");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.body").exists());
    }

    /** US19 — solución inexistente → 404 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void create_solutionNotFound_returns404() throws Exception {
        when(feedbackService.create(any(), any(), any()))
            .thenThrow(new SolutionNotFoundException("Resolución no encontrada"));

        var req = new CreateFeedbackRequest(null, "Feedback para solución inexistente");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    /** US19 Escenario 19.6 — sin autenticación → 401 */
    @Test
    void create_unauthenticated_returns401() throws Exception {
        var req = new CreateFeedbackRequest(BigDecimal.valueOf(7.0), "Feedback");
        mockMvc.perform(post("/api/v1/solutions/" + UUID.randomUUID() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /solutions/{solutionId}/feedback (US19 / US20) ────────────────────

    /** US19 / US20 — obtener feedback de una resolución → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void get_existingFeedback_returns200() throws Exception {
        var solutionId = UUID.randomUUID();
        var response = Mockito.mock(FeedbackResponse.class);
        when(feedbackService.getBySolution(solutionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/solutions/" + solutionId + "/feedback"))
            .andExpect(status().isOk());
    }

    /** US19 / US20 — sin feedback para la solución → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void get_noFeedback_returns404() throws Exception {
        when(feedbackService.getBySolution(any()))
            .thenThrow(new FeedbackNotFoundException("No hay feedback para esta resolución"));

        mockMvc.perform(get("/api/v1/solutions/" + UUID.randomUUID() + "/feedback"))
            .andExpect(status().isNotFound());
    }

    /** GET /solutions/{id}/feedback — sin autenticación → 401 */
    @Test
    void get_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/solutions/" + UUID.randomUUID() + "/feedback"))
            .andExpect(status().isUnauthorized());
    }
}
