package com.mentoredu.feedback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.entity.Role;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.feedback.dto.FeedbackRequest;
import com.mentoredu.feedback.dto.FeedbackResponse;
import com.mentoredu.feedback.exception.FeedbackResourceAuthorshipException;
import com.mentoredu.feedback.exception.FeedbackSolutionNotFoundException;
import com.mentoredu.feedback.exception.FeedbackTargetNotFoundException;
import com.mentoredu.feedback.exception.FeedbackUnauthorizedException;
import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.feedback.service.IFeedbackService;
import com.mentoredu.forum.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IFeedbackService feedbackService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US34 — POST /api/v1/feedback
    // =========================================================================

    // Gherkin: Exitoso — docente registrado + estudiante existe + body válido → 201 Created
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_validRequest_returns201() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Buen desempeño en álgebra.", null);
        FeedbackResponse response = buildResponse(targetUserId, "Buen desempeño en álgebra.", null);

        when(feedbackService.provideFeedback(eq("teacher@example.com"), any(FeedbackRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.targetUserId").value(targetUserId.toString()))
                .andExpect(jsonPath("$.body").value("Buen desempeño en álgebra."))
                .andExpect(jsonPath("$.score").isEmpty());
    }

    // Gherkin: Error — body vacío → 400 Bad Request
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_emptyBody_returns400() throws Exception {
        FeedbackRequest request = buildRequest(UUID.randomUUID(), "", null);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.body").isNotEmpty());
    }

    // Gherkin: Error — sin targetUserId → 400 Bad Request
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_missingTargetUserId_returns400() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setBody("Texto válido");

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.targetUserId").isNotEmpty());
    }

    // Gherkin: Alternativo exitoso — incluye puntuación → 201 con score guardado
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_withScore_returns201WithScore() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        BigDecimal score = new BigDecimal("15.50");
        FeedbackRequest request = buildRequest(targetUserId, "Excelente participación.", score);
        FeedbackResponse response = buildResponse(targetUserId, "Excelente participación.", score);

        when(feedbackService.provideFeedback(eq("teacher@example.com"), any(FeedbackRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(15.50));
    }

    // Gherkin: Alternativo error — estudiante objetivo no existe → 404 Not Found
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_targetNotFound_returns404() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Retroalimentación válida.", null);

        when(feedbackService.provideFeedback(eq("teacher@example.com"), any(FeedbackRequest.class)))
                .thenThrow(new FeedbackTargetNotFoundException("Estudiante no encontrado: " + targetUserId));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Estudiante no encontrado: " + targetUserId));
    }

    // Extra: no es TEACHER ni ADMIN → 403 Forbidden (RN-36)
    @Test
    @WithMockUser(username = "student@example.com")
    void provideFeedback_notTeacherRole_returns403() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Intento no autorizado.", null);

        when(feedbackService.provideFeedback(eq("student@example.com"), any(FeedbackRequest.class)))
                .thenThrow(new FeedbackUnauthorizedException(
                        "Solo los docentes y administradores pueden emitir retroalimentación académica (RN-36)"));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // Extra: no autenticado → 401 Unauthorized
    @Test
    void provideFeedback_withoutAuth_returns401() throws Exception {
        FeedbackRequest request = buildRequest(UUID.randomUUID(), "Retroalimentación válida.", null);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US35 — GET /api/v1/feedback/me
    // =========================================================================

    // Gherkin: Exitoso — tiene retroalimentación registrada → 200 con lista ordenada por fecha
    @Test
    @WithMockUser(username = "student@example.com")
    void getReceivedFeedback_withFeedback_returns200WithList() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        List<FeedbackResponse> responses = List.of(
                buildResponse(targetId, authorId, "Excelente trabajo.", new BigDecimal("18.00")),
                buildResponse(targetId, authorId, "Mejorar la presentación.", null)
        );

        when(feedbackService.getReceivedFeedback("student@example.com")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/feedback/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].body").value("Excelente trabajo."))
                .andExpect(jsonPath("$[0].score").value(18.00))
                .andExpect(jsonPath("$[1].body").value("Mejorar la presentación."));
    }

    // Gherkin: Error — no autenticado → 401 Unauthorized
    @Test
    void getReceivedFeedback_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/feedback/me"))
                .andExpect(status().isUnauthorized());
    }

    // Gherkin: Alternativo exitoso — sin retroalimentación → 200 lista vacía sin error
    @Test
    @WithMockUser(username = "student@example.com")
    void getReceivedFeedback_noFeedback_returns200EmptyList() throws Exception {
        when(feedbackService.getReceivedFeedback("student@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/feedback/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Gherkin: Alternativo error — usuario no registrado → 404 Not Found
    @Test
    @WithMockUser(username = "ghost@example.com")
    void getReceivedFeedback_userNotFound_returns404() throws Exception {
        when(feedbackService.getReceivedFeedback("ghost@example.com"))
                .thenThrow(new UserNotFoundException("Usuario no encontrado: ghost@example.com"));

        mockMvc.perform(get("/api/v1/feedback/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado: ghost@example.com"));
    }

    // =========================================================================
    // US40 — POST /api/v1/feedback (with solutionId)
    // =========================================================================

    // US40: exitoso — docente vincula feedback a una resolución → 201
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_withSolutionId_returns201() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Excelente resolución.", new BigDecimal("9.5"));
        request.setSolutionId(solutionId);

        FeedbackResponse response = buildResponse(targetUserId, "Excelente resolución.", new BigDecimal("9.5"));

        when(feedbackService.provideFeedback(eq("teacher@example.com"), any(FeedbackRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Excelente resolución."))
                .andExpect(jsonPath("$.score").value(9.5));
    }

    // US40: solución no existe → 404 (RN-48 prerequisite)
    @Test
    @WithMockUser(username = "teacher@example.com")
    void provideFeedback_withSolutionId_solutionNotFound_returns404() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Feedback válido.", null);
        request.setSolutionId(solutionId);

        when(feedbackService.provideFeedback(eq("teacher@example.com"), any(FeedbackRequest.class)))
                .thenThrow(new FeedbackSolutionNotFoundException("Resolución no encontrada: " + solutionId));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resolución no encontrada: " + solutionId));
    }

    // US40: docente no es autor del recurso → 403 (RN-48)
    @Test
    @WithMockUser(username = "other-teacher@example.com")
    void provideFeedback_withSolutionId_notResourceAuthor_returns403() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();
        FeedbackRequest request = buildRequest(targetUserId, "Feedback no autorizado.", null);
        request.setSolutionId(solutionId);

        when(feedbackService.provideFeedback(eq("other-teacher@example.com"), any(FeedbackRequest.class)))
                .thenThrow(new FeedbackResourceAuthorshipException(
                        "Solo el autor del recurso puede dar feedback a sus resoluciones (RN-48)"));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Solo el autor del recurso puede dar feedback a sus resoluciones (RN-48)"));
    }

    // =========================================================================
    // F1.2 — GET /api/v1/feedback/given
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getGivenFeedback_withFeedback_returns200WithList() throws Exception {
        UUID targetId = UUID.randomUUID();
        List<FeedbackResponse> responses = List.of(
                buildResponse(targetId, "Retroalimentación emitida.", new BigDecimal("14.0"))
        );

        when(feedbackService.getGivenFeedback("teacher@example.com")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/feedback/given"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].body").value("Retroalimentación emitida."));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getGivenFeedback_emptyList_returns200() throws Exception {
        when(feedbackService.getGivenFeedback("teacher@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/feedback/given"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getGivenFeedback_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/feedback/given"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private FeedbackRequest buildRequest(UUID targetUserId, String body, BigDecimal score) {
        FeedbackRequest req = new FeedbackRequest();
        req.setTargetUserId(targetUserId);
        req.setBody(body);
        req.setScore(score);
        return req;
    }

    private FeedbackResponse buildResponse(UUID targetUserId, String body, BigDecimal score) {
        return buildResponse(targetUserId, UUID.randomUUID(), body, score);
    }

    private FeedbackResponse buildResponse(UUID targetUserId, UUID authorId, String body, BigDecimal score) {
        Role teacherRole = new Role();
        teacherRole.setName("TEACHER");

        Role studentRole = new Role();
        studentRole.setName("STUDENT");

        User author = User.builder().id(authorId).email("teacher@example.com").role(teacherRole).build();
        User target = User.builder().id(targetUserId).email("student@example.com").role(studentRole).build();

        FeedbackEntry entry = FeedbackEntry.builder()
                .id(UUID.randomUUID())
                .author(author)
                .target(target)
                .body(body)
                .score(score)
                .createdAt(LocalDateTime.now())
                .build();

        return new FeedbackResponse(entry);
    }
}
