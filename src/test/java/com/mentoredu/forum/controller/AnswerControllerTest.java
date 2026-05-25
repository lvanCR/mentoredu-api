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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnswerController.class)
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IAnswerService answerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US13 — POST /threads/{threadId}/answers
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void createAnswer_withValidBody_returns201() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = new CreateAnswerRequest("La derivada de x^2 es 2x por la regla de la potencia.");
        var response = buildAnswerResponse(threadId, request.body(), "Ana García");

        when(answerService.create(eq(threadId), any(), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.threadId").value(threadId.toString()))
                .andExpect(jsonPath("$.body").value("La derivada de x^2 es 2x por la regla de la potencia."))
                .andExpect(jsonPath("$.accepted").value(false))
                .andExpect(jsonPath("$.authorDisplay").value("Ana García"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createAnswer_byTeacher_returns201() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = new CreateAnswerRequest("Para integrar por partes, usa la fórmula udv = uv - vdu.");
        var response = buildAnswerResponse(threadId, request.body(), "Prof. Ramírez");

        when(answerService.create(eq(threadId), any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorDisplay").value("Prof. Ramírez"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_withEmptyBody_returns400() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = new CreateAnswerRequest("");

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_withNullBody_returns400() throws Exception {
        UUID threadId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_onClosedThread_returns422() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.create(eq(threadId), any(), any()))
                .thenThrow(new ThreadClosedException("Thread is closed and does not accept new replies: " + threadId));

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Thread is closed and does not accept new replies: " + threadId));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_onNonExistentThread_returns404() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.create(eq(threadId), any(), any()))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + threadId));
    }

    @Test
    void createAnswer_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US13 — GET /threads/{threadId}/answers
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void listAnswers_whenThreadExists_returns200() throws Exception {
        UUID threadId = UUID.randomUUID();
        var items = List.of(
                buildAnswerResponse(threadId, "Primera respuesta", "Juan"),
                buildAnswerResponse(threadId, "Segunda respuesta", "María"));

        when(answerService.listByThread(eq(threadId), anyInt(), anyInt()))
                .thenReturn(pageOf(items));

        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].threadId").value(threadId.toString()))
                .andExpect(jsonPath("$.content[0].body").value("Primera respuesta"))
                .andExpect(jsonPath("$.content[1].body").value("Segunda respuesta"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listAnswers_whenThreadEmpty_returns200WithEmptyArray() throws Exception {
        UUID threadId = UUID.randomUUID();
        when(answerService.listByThread(eq(threadId), anyInt(), anyInt())).thenReturn(pageOf(List.of()));

        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listAnswers_whenThreadNotFound_returns404() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.listByThread(eq(threadId), anyInt(), anyInt()))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", threadId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + threadId));
    }

    @Test
    void listAnswers_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateAnswerRequest validRequest() {
        return new CreateAnswerRequest("La respuesta es correcta por el teorema fundamental del cálculo.");
    }

    private AnswerResponse buildAnswerResponse(UUID threadId, String body, String authorDisplay) {
        return AnswerResponse.builder()
                .id(UUID.randomUUID())
                .threadId(threadId)
                .body(body)
                .accepted(false)
                .authorDisplay(authorDisplay)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private <T> PagedResponse<T> pageOf(List<T> items) {
        return new PagedResponse<>(items, 0, 20, items.size(), items.isEmpty() ? 0 : 1, true);
    }
}
