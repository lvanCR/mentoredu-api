package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.service.IReactionService;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para ReactionController.
 * Cubre: US14 — Reaccionar a hilos, respuestas y comentarios (toggle LIKE/DISLIKE).
 * Regla RN-15: reacciones únicas por usuario por contenido — funciona como toggle.
 * Validado en producción (CONFIRMACION_TESTS.md HU-14):
 *   14.1 nueva LIKE → 201; 14.2 misma reacción → toggle off 204 (body vacío);
 *   14.3 distinta reacción → reemplaza 201; 14.4 createdAt no null al crear;
 *   14.5 reaccionar a answer → 201; 14.6 sin auth → 401.
 */
@WebMvcTest(
    controllers = ReactionController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ReactionControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IReactionService reactionService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /threads/{id}/reactions (US14) ───────────────────────────────────

    /** US14 Escenario 14.1 — nueva reacción LIKE en hilo → 201 con ReactionResponse */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_newLike_returns201() throws Exception {
        var threadId = UUID.randomUUID();
        var response = ReactionResponse.builder()
            .id(UUID.randomUUID()).reactionType("LIKE")
            .createdAt(LocalDateTime.now()).build();
        when(reactionService.reactToThread(eq(threadId), any(), eq("user@example.com")))
            .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/threads/" + threadId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reactionType").value("LIKE"))
            .andExpect(jsonPath("$.createdAt").exists()); // RN-15 / BUG #2: createdAt no debe ser null
    }

    /** US14 Escenario 14.2 — misma reacción (toggle off) → 204 con body vacío */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_sameReactionToggleOff_returns204() throws Exception {
        var threadId = UUID.randomUUID();
        when(reactionService.reactToThread(eq(threadId), any(), any()))
            .thenReturn(Optional.empty()); // toggle elimina la reacción

        mockMvc.perform(post("/api/v1/threads/" + threadId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isNoContent()); // 204 body vacío
    }

    /** US14 Escenario 14.3 — distinta reacción reemplaza la anterior → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_differentReactionReplaces_returns201() throws Exception {
        var threadId = UUID.randomUUID();
        var response = ReactionResponse.builder()
            .id(UUID.randomUUID()).reactionType("DISLIKE")
            .createdAt(LocalDateTime.now()).build();
        when(reactionService.reactToThread(eq(threadId), any(), any()))
            .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/threads/" + threadId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("DISLIKE"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reactionType").value("DISLIKE"));
    }

    /** US14 — hilo inexistente al reaccionar → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_threadNotFound_returns404() throws Exception {
        var threadId = UUID.randomUUID();
        when(reactionService.reactToThread(eq(threadId), any(), any()))
            .thenThrow(new ThreadNotFoundException("Thread not found"));

        mockMvc.perform(post("/api/v1/threads/" + threadId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isNotFound());
    }

    /** US14 — reactionType en blanco → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_blankReactionType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/threads/" + UUID.randomUUID() + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest(""))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.reactionType").exists());
    }

    /** US14 Escenario 14.6 — sin autenticación → 401 */
    @Test
    void reactToThread_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/threads/" + UUID.randomUUID() + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /answers/{id}/reactions (US14) ───────────────────────────────────

    /** US14 Escenario 14.5 — reaccionar a answer → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToAnswer_newLike_returns201() throws Exception {
        var answerId = UUID.randomUUID();
        var response = ReactionResponse.builder()
            .id(UUID.randomUUID()).reactionType("LIKE")
            .createdAt(LocalDateTime.now()).build();
        when(reactionService.reactToAnswer(eq(answerId), any(), eq("user@example.com")))
            .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/answers/" + answerId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isCreated());
    }

    /** US14 — answer inexistente al reaccionar → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToAnswer_answerNotFound_returns404() throws Exception {
        var answerId = UUID.randomUUID();
        when(reactionService.reactToAnswer(eq(answerId), any(), any()))
            .thenThrow(new AnswerNotFoundException("Answer not found"));

        mockMvc.perform(post("/api/v1/answers/" + answerId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isNotFound());
    }

    /** POST /answers/{id}/reactions — sin autenticación → 401 */
    @Test
    void reactToAnswer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/answers/" + UUID.randomUUID() + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /comments/{id}/reactions (US14) ──────────────────────────────────

    /** US14 — reaccionar a comentario → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToComment_newLike_returns201() throws Exception {
        var commentId = UUID.randomUUID();
        var response = ReactionResponse.builder()
            .id(UUID.randomUUID()).reactionType("LIKE")
            .createdAt(LocalDateTime.now()).build();
        when(reactionService.reactToComment(eq(commentId), any(), eq("user@example.com")))
            .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/comments/" + commentId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isCreated());
    }

    /** US14 — toggle off en comentario → 204 */
    @Test
    @WithMockUser(username = "user@example.com")
    void reactToComment_sameReactionToggleOff_returns204() throws Exception {
        var commentId = UUID.randomUUID();
        when(reactionService.reactToComment(eq(commentId), any(), any()))
            .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/comments/" + commentId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isNoContent());
    }

    /** POST /comments/{id}/reactions — sin autenticación → 401 */
    @Test
    void reactToComment_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/comments/" + UUID.randomUUID() + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateReactionRequest("LIKE"))))
            .andExpect(status().isUnauthorized());
    }
}
