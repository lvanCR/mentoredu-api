package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.service.IAnswerService;
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
 * Tests de capa web para AnswerController.
 * Cubre: US13 — Responder a un hilo de foro.
 * Validado en producción (CONFIRMACION_TESTS.md HU-13):
 *   13.1 respuesta exitosa → 201; 13.2 hilo cerrado → 409; 13.3 hilo inexistente → 404;
 *   13.4 body vacío → 400; 13.5 sin auth → 401.
 * Nota: el campo del body es `body`, confirmado en HU-13.
 */
@WebMvcTest(
    controllers = AnswerController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AnswerControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IAnswerService answerService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /threads/{threadId}/answers (US13) ────────────────────────────────

    /** US13 Escenario 13.1 — respuesta exitosa a hilo abierto → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_validAnswer_returns201() throws Exception {
        var threadId = UUID.randomUUID();
        var response = AnswerResponse.builder()
            .id(UUID.randomUUID()).threadId(threadId)
            .body("Esta es mi respuesta").authorDisplay("Maria Gonzalez")
            .likeCount(0).dislikeCount(0)
            .createdAt(LocalDateTime.now()).build();
        when(answerService.create(eq(threadId), any(), eq("user@example.com"))).thenReturn(response);

        var req = new CreateAnswerRequest("Esta es mi respuesta");
        mockMvc.perform(post("/api/v1/threads/" + threadId + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Esta es mi respuesta"))
            .andExpect(jsonPath("$.authorDisplay").value("Maria Gonzalez"));
    }

    /** US13 Escenario 13.2 — responder a hilo cerrado → 409 Conflict */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_threadClosed_returns409() throws Exception {
        var threadId = UUID.randomUUID();
        when(answerService.create(eq(threadId), any(), any()))
            .thenThrow(new ThreadClosedException("El hilo está cerrado y no acepta respuestas"));

        var req = new CreateAnswerRequest("Intento responder hilo cerrado");
        mockMvc.perform(post("/api/v1/threads/" + threadId + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US13 Escenario 13.3 — hilo inexistente → 404 Not Found */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_threadNotFound_returns404() throws Exception {
        var threadId = UUID.randomUUID();
        when(answerService.create(eq(threadId), any(), any()))
            .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        var req = new CreateAnswerRequest("Respuesta");
        mockMvc.perform(post("/api/v1/threads/" + threadId + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** US13 Escenario 13.4 — body vacío → 400 con detalle del campo body */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withBlankBody_returns400WithBodyFieldError() throws Exception {
        var req = new CreateAnswerRequest(""); // body vacío
        mockMvc.perform(post("/api/v1/threads/" + UUID.randomUUID() + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.body").value("Body is required"));
    }

    /** US13 Escenario 13.5 — sin autenticación → 401 */
    @Test
    void create_unauthenticated_returns401() throws Exception {
        var req = new CreateAnswerRequest("Respuesta");
        mockMvc.perform(post("/api/v1/threads/" + UUID.randomUUID() + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /threads/{threadId}/answers (US13) ─────────────────────────────────

    /** US13 — listar respuestas de un hilo → 200 con lista paginada */
    @Test
    @WithMockUser(username = "user@example.com")
    void listByThread_existingThread_returns200WithAnswers() throws Exception {
        var threadId = UUID.randomUUID();
        var answer = AnswerResponse.builder()
            .id(UUID.randomUUID()).threadId(threadId)
            .body("Mi respuesta").authorDisplay("Juan")
            .createdAt(LocalDateTime.now()).build();
        var paged = new PagedResponse<>(List.of(answer), 0, 20, 1L, 1, true);
        when(answerService.listByThread(eq(threadId), eq(0), eq(20), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/threads/" + threadId + "/answers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].body").value("Mi respuesta"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    /** US13 — listar respuestas de hilo inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void listByThread_threadNotFound_returns404() throws Exception {
        var threadId = UUID.randomUUID();
        when(answerService.listByThread(eq(threadId), eq(0), eq(20), any()))
            .thenThrow(new ThreadNotFoundException("Thread not found"));

        mockMvc.perform(get("/api/v1/threads/" + threadId + "/answers"))
            .andExpect(status().isNotFound());
    }

    /** GET /threads/{id}/answers — sin autenticación → 401 */
    @Test
    void listByThread_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads/" + UUID.randomUUID() + "/answers"))
            .andExpect(status().isUnauthorized());
    }
}
