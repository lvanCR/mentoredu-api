package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.service.IAnswerService;
import com.mentoredu.forum.service.IThreadService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ThreadController.class, AnswerController.class})
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IThreadService threadService;

    @MockitoBean
    private IAnswerService answerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US16 — Create forum thread
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withValidFields_returns201() throws Exception {
        UUID courseId = UUID.randomUUID();
        var request = validThreadRequest(courseId);
        var response = buildThreadResponse(courseId, "¿Cómo resolver integrales dobles?", false);

        when(threadService.create(any(), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("¿Cómo resolver integrales dobles?"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createThread_withAnonymousFlag_returns201WithAnonimoDisplay() throws Exception {
        UUID courseId = UUID.randomUUID();
        var request = validThreadRequest(courseId);
        request.setAnonymous(true);
        var response = buildAnonymousThreadResponse(courseId, "¿Diferencia entre serie y sucesión?");

        when(threadService.create(any(), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonymous").value(true))
                .andExpect(jsonPath("$.authorDisplay").value("Anónimo"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withEmptyBody_returns400() throws Exception {
        var request = validThreadRequest(UUID.randomUUID());
        request.setBody("");

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    void createThread_withoutAuth_returns401() throws Exception {
        var request = validThreadRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withEmptyTitle_returns400() throws Exception {
        var request = validThreadRequest(UUID.randomUUID());
        request.setTitle("");

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withTitleTooLong_returns400() throws Exception {
        var request = validThreadRequest(UUID.randomUUID());
        request.setTitle("A".repeat(161));

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withoutClassification_returns400() throws Exception {
        String body = """
                {
                  "title": "¿Cómo resolver integrales dobles?",
                  "body": "Necesito ayuda con integrales dobles de funciones trigonométricas."
                }
                """;

        when(threadService.create(any(), eq("user@example.com")))
                .thenThrow(new IllegalArgumentException(
                        "El hilo requiere al menos una categoría (universityId, courseId o careerId)"));

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // =========================================================================
    // US16 — List recent threads
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void listThreads_authenticated_returns200() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(threadService.listRecent(0, 10))
                .thenReturn(List.of(buildThreadResponse(courseId, "Hilo de prueba", false)));

        mockMvc.perform(get("/api/v1/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Hilo de prueba"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listThreads_withPaginationParams_returns200() throws Exception {
        when(threadService.listRecent(1, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/threads")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listThreads_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US16 — Get thread by ID
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getThread_whenExists_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        when(threadService.get(eq(id)))
                .thenReturn(buildThreadResponse(id, courseId, "¿Cómo resolver integrales dobles?", false));

        mockMvc.perform(get("/api/v1/threads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("¿Cómo resolver integrales dobles?"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getThread_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(threadService.get(eq(id)))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + id));

        mockMvc.perform(get("/api/v1/threads/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + id));
    }

    @Test
    void getThread_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US17 — Reply to forum thread
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_withValidBody_returns201() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = validAnswerRequest();
        var response = buildAnswerResponse(threadId);

        when(answerService.create(eq(threadId), any(), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.threadId").value(threadId.toString()))
                .andExpect(jsonPath("$.body").value("Los límites de integración deben seguir el orden correcto."))
                .andExpect(jsonPath("$.accepted").value(false))
                .andExpect(jsonPath("$.authorDisplay").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createAnswer_withDetailedExplanation_returns201() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = new CreateAnswerRequest();
        request.setBody("Para resolver integrales dobles, primero define el dominio de integración y luego aplica Fubini.");
        var response = buildAnswerResponse(threadId,
                "Para resolver integrales dobles, primero define el dominio de integración y luego aplica Fubini.",
                "María López");

        when(answerService.create(eq(threadId), any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Para resolver integrales dobles, primero define el dominio de integración y luego aplica Fubini."))
                .andExpect(jsonPath("$.authorDisplay").value("María López"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createAnswer_withEmptyBody_returns400() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = new CreateAnswerRequest();
        request.setBody("");

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
    void createAnswer_onClosedThread_returns409() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.create(eq(threadId), any(), any()))
                .thenThrow(new ThreadClosedException("Thread is closed and does not accept new replies: " + threadId));

        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAnswerRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
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
                        .content(objectMapper.writeValueAsString(validAnswerRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + threadId));
    }

    @Test
    void createAnswer_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/threads/{threadId}/answers", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAnswerRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listAnswers_whenThreadExists_returns200() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.listByThread(eq(threadId)))
                .thenReturn(List.of(buildAnswerResponse(threadId)));

        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].threadId").value(threadId.toString()))
                .andExpect(jsonPath("$[0].body").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listAnswers_whenThreadNotFound_returns404() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(answerService.listByThread(eq(threadId)))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", threadId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void listAnswers_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads/{threadId}/answers", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US18 — Close forum thread
    // =========================================================================

    @Test
    @WithMockUser(username = "author@example.com")
    void closeThread_asAuthor_returns200WithStatusClosed() throws Exception {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        var closed = ThreadResponse.builder()
                .id(id)
                .title("¿Cómo resolver integrales dobles?")
                .body("Necesito ayuda con integrales dobles de funciones trigonométricas.")
                .anonymous(false)
                .authorDisplay("Juan Pérez")
                .courseId(courseId)
                .status("CLOSED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(threadService.close(eq(id), eq("author@example.com"))).thenReturn(closed);

        mockMvc.perform(patch("/api/v1/threads/{id}/close", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void closeThread_asNonAuthor_returns403() throws Exception {
        UUID id = UUID.randomUUID();

        when(threadService.close(eq(id), eq("other@example.com")))
                .thenThrow(new ThreadNotOwnedException("Only the author can close this thread: " + id));

        mockMvc.perform(patch("/api/v1/threads/{id}/close", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Only the author can close this thread: " + id));
    }

    @Test
    @WithMockUser(username = "author@example.com")
    void closeThread_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(threadService.close(eq(id), eq("author@example.com")))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + id));

        mockMvc.perform(patch("/api/v1/threads/{id}/close", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + id));
    }

    @Test
    @WithMockUser(username = "author@example.com")
    void closeThread_alreadyClosed_returns409() throws Exception {
        UUID id = UUID.randomUUID();

        when(threadService.close(eq(id), eq("author@example.com")))
                .thenThrow(new ThreadClosedException("Thread is already closed: " + id));

        mockMvc.perform(patch("/api/v1/threads/{id}/close", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Thread is already closed: " + id));
    }

    @Test
    void closeThread_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/threads/{id}/close", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateThreadRequest validThreadRequest(UUID courseId) {
        var r = new CreateThreadRequest();
        r.setTitle("¿Cómo resolver integrales dobles?");
        r.setBody("Necesito ayuda con integrales dobles de funciones trigonométricas.");
        r.setCourseId(courseId);
        return r;
    }

    private ThreadResponse buildThreadResponse(UUID courseId, String title, boolean anonymous) {
        return ThreadResponse.builder()
                .id(UUID.randomUUID())
                .title(title)
                .body("Necesito ayuda con integrales dobles de funciones trigonométricas.")
                .anonymous(anonymous)
                .authorDisplay(anonymous ? "Anónimo" : "Juan Pérez")
                .courseId(courseId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ThreadResponse buildThreadResponse(UUID id, UUID courseId, String title, boolean anonymous) {
        return ThreadResponse.builder()
                .id(id)
                .title(title)
                .body("Necesito ayuda con integrales dobles de funciones trigonométricas.")
                .anonymous(anonymous)
                .authorDisplay(anonymous ? "Anónimo" : "Juan Pérez")
                .courseId(courseId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ThreadResponse buildAnonymousThreadResponse(UUID courseId, String title) {
        return ThreadResponse.builder()
                .id(UUID.randomUUID())
                .title(title)
                .body("Tengo dudas sobre este concepto matemático.")
                .anonymous(true)
                .authorDisplay("Anónimo")
                .courseId(courseId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateAnswerRequest validAnswerRequest() {
        var r = new CreateAnswerRequest();
        r.setBody("Los límites de integración deben seguir el orden correcto.");
        return r;
    }

    private AnswerResponse buildAnswerResponse(UUID threadId) {
        return buildAnswerResponse(threadId,
                "Los límites de integración deben seguir el orden correcto.", "Juan Pérez");
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
}
