package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.service.ICommentService;
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
 * Tests de capa web para CommentController.
 * Cubre: US15 — Comentar en una respuesta del foro.
 * Validado en producción (CONFIRMACION_TESTS.md HU-15):
 *   15.1 comentario exitoso → 201; 15.2 GET lista plana (no paginada) → 200;
 *   15.3 answer inexistente → 404; 15.4 body vacío → 400; 15.5 sin auth → 401.
 */
@WebMvcTest(
    controllers = CommentController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class CommentControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private ICommentService commentService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /answers/{answerId}/comments (US15) ───────────────────────────────

    /** US15 Escenario 15.1 — comentario exitoso → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_validComment_returns201() throws Exception {
        var answerId = UUID.randomUUID();
        var response = CommentResponse.builder()
            .id(UUID.randomUUID()).answerId(answerId)
            .body("Gran respuesta, gracias").authorDisplay("Juan")
            .createdAt(LocalDateTime.now()).build();
        when(commentService.create(eq(answerId), any(), eq("user@example.com"))).thenReturn(response);

        var req = new CreateCommentRequest("Gran respuesta, gracias");
        mockMvc.perform(post("/api/v1/answers/" + answerId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Gran respuesta, gracias"));
    }

    /** US15 Escenario 15.3 — answer inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_answerNotFound_returns404() throws Exception {
        var answerId = UUID.randomUUID();
        when(commentService.create(eq(answerId), any(), any()))
            .thenThrow(new AnswerNotFoundException("Answer not found: " + answerId));

        var req = new CreateCommentRequest("Comentario");
        mockMvc.perform(post("/api/v1/answers/" + answerId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** US15 Escenario 15.4 — body vacío → 400 con detalle del campo body */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withBlankBody_returns400() throws Exception {
        var req = new CreateCommentRequest("");
        mockMvc.perform(post("/api/v1/answers/" + UUID.randomUUID() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.body").exists());
    }

    /** US15 Escenario 15.5 — sin autenticación → 401 */
    @Test
    void create_unauthenticated_returns401() throws Exception {
        var req = new CreateCommentRequest("Comentario");
        mockMvc.perform(post("/api/v1/answers/" + UUID.randomUUID() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /answers/{answerId}/comments (US15) ────────────────────────────────
    // Confirmado en HU-15.2: devuelve lista plana (no paginada), no List

    /** US15 Escenario 15.2 — listar comentarios → 200 con lista plana */
    @Test
    @WithMockUser(username = "user@example.com")
    void listByAnswer_existingAnswer_returns200WithFlatList() throws Exception {
        var answerId = UUID.randomUUID();
        var comment1 = CommentResponse.builder()
            .id(UUID.randomUUID()).answerId(answerId).body("Comentario 1")
            .authorDisplay("Ana").createdAt(LocalDateTime.now()).build();
        var comment2 = CommentResponse.builder()
            .id(UUID.randomUUID()).answerId(answerId).body("Comentario 2")
            .authorDisplay("Luis").createdAt(LocalDateTime.now()).build();
        when(commentService.listByAnswer(answerId)).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/api/v1/answers/" + answerId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray()) // lista plana, no PagedResponse
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].body").value("Comentario 1"))
            .andExpect(jsonPath("$[1].body").value("Comentario 2"));
    }

    /** US15 — listar comentarios de answer inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listByAnswer_answerNotFound_returns404() throws Exception {
        var answerId = UUID.randomUUID();
        when(commentService.listByAnswer(answerId))
            .thenThrow(new AnswerNotFoundException("Answer not found"));

        mockMvc.perform(get("/api/v1/answers/" + answerId + "/comments"))
            .andExpect(status().isNotFound());
    }

    /** GET /answers/{id}/comments — sin autenticación → 401 */
    @Test
    void listByAnswer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/answers/" + UUID.randomUUID() + "/comments"))
            .andExpect(status().isUnauthorized());
    }
}
