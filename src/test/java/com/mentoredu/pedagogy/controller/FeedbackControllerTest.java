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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IFeedbackService feedbackService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US19 — Dar feedback correctivo
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_withValidData_returns201() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("8.5"), "Buena resolución, pero falta justificar el paso 3.");
        var response = buildFeedbackResponse(solutionId, new BigDecimal("8.5"), "Buena resolución, pero falta justificar el paso 3.");

        when(feedbackService.create(eq(solutionId), any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.solutionId").value(solutionId.toString()))
                .andExpect(jsonPath("$.score").value(8.5))
                .andExpect(jsonPath("$.body").value("Buena resolución, pero falta justificar el paso 3."))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_withPerfectScore_returns201() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("10.0"), "Excelente trabajo.");
        var response = buildFeedbackResponse(solutionId, new BigDecimal("10.0"), "Excelente trabajo.");

        when(feedbackService.create(eq(solutionId), any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(10.0));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_withEmptyBody_returns400() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("7.0"), "");

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_withScoreAboveMax_returns400() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("11.0"), "Feedback válido.");

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.score").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_withNegativeScore_returns400() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("-1.0"), "Feedback válido.");

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.score").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_alreadyExists_returns409() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("6.0"), "Ya revisé esto.");

        when(feedbackService.create(eq(solutionId), any(), eq("teacher@example.com")))
                .thenThrow(new FeedbackAlreadyExistsException("Esta resolución ya tiene feedback"));

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Esta resolución ya tiene feedback"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createFeedback_solutionNotFound_returns404() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("5.0"), "Feedback.");

        when(feedbackService.create(eq(solutionId), any(), eq("teacher@example.com")))
                .thenThrow(new SolutionNotFoundException("Resolución no encontrada: " + solutionId));

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createFeedback_notAuthorOfExercise_returns403() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var request = new CreateFeedbackRequest(new BigDecimal("7.0"), "Intento de feedback.");

        when(feedbackService.create(eq(solutionId), any(), eq("student@example.com")))
                .thenThrow(new SolutionAccessDeniedException("Solo el autor del ejercicio puede dar feedback"));

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", solutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Solo el autor del ejercicio puede dar feedback"));
    }

    @Test
    void createFeedback_withoutAuth_returns401() throws Exception {
        var request = new CreateFeedbackRequest(new BigDecimal("8.0"), "Feedback.");

        mockMvc.perform(post("/api/v1/solutions/{solutionId}/feedback", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US20 — Ver feedback recibido
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void getFeedback_whenExists_returns200() throws Exception {
        UUID solutionId = UUID.randomUUID();
        var response = buildFeedbackResponse(solutionId, new BigDecimal("9.0"), "Muy buena resolución.");

        when(feedbackService.getBySolution(eq(solutionId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/solutions/{solutionId}/feedback", solutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solutionId").value(solutionId.toString()))
                .andExpect(jsonPath("$.score").value(9.0))
                .andExpect(jsonPath("$.body").value("Muy buena resolución."))
                .andExpect(jsonPath("$.authorId").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void getFeedback_whenNotFound_returns404() throws Exception {
        UUID solutionId = UUID.randomUUID();

        when(feedbackService.getBySolution(eq(solutionId)))
                .thenThrow(new FeedbackNotFoundException("No hay feedback para esta resolución: " + solutionId));

        mockMvc.perform(get("/api/v1/solutions/{solutionId}/feedback", solutionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No hay feedback para esta resolución: " + solutionId));
    }

    @Test
    void getFeedback_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/solutions/{solutionId}/feedback", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private FeedbackResponse buildFeedbackResponse(UUID solutionId, BigDecimal score, String body) {
        return FeedbackResponse.builder()
                .id(UUID.randomUUID())
                .solutionId(solutionId)
                .authorId(UUID.randomUUID())
                .score(score)
                .body(body)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
