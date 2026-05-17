package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ThreadController.class)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IThreadService threadService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US16 — Create forum thread
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: texto válido → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withValidFields_returns201() throws Exception {
        UUID subjectId = UUID.randomUUID();
        var request = validRequest(subjectId);
        var response = buildResponse(subjectId, "¿Cómo resolver integrales dobles?", false);

        when(threadService.create(any(), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("¿Cómo resolver integrales dobles?"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.subjectId").value(subjectId.toString()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo exitoso: duda concreta → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createThread_withAnonymousFlag_returns201WithAnonimoDisplay() throws Exception {
        UUID subjectId = UUID.randomUUID();
        var request = validRequest(subjectId);
        request.setAnonymous(true);
        var response = buildAnonymousResponse(subjectId, "¿Diferencia entre serie y sucesión?");

        when(threadService.create(any(), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonymous").value(true))
                .andExpect(jsonPath("$.authorDisplay").value("Anónimo"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: body vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withEmptyBody_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setBody("");

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: no autenticado → 401
    // -------------------------------------------------------------------------

    @Test
    void createThread_withoutAuth_returns401() throws Exception {
        var request = validRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Validación: title vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withEmptyTitle_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setTitle("");

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    // -------------------------------------------------------------------------
    // Validación: title excede 160 caracteres → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withTitleTooLong_returns400() throws Exception {
        var request = validRequest(UUID.randomUUID());
        request.setTitle("A".repeat(161));

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    // -------------------------------------------------------------------------
    // RN-16: subjectId ausente → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void createThread_withoutSubjectId_returns400() throws Exception {
        String body = """
                {
                  "title": "¿Cómo resolver integrales dobles?",
                  "body": "Necesito ayuda con integrales dobles de funciones trigonométricas."
                }
                """;

        mockMvc.perform(post("/api/v1/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.subjectId").exists());
    }

    // =========================================================================
    // US16 — List recent threads
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void listThreads_authenticated_returns200() throws Exception {
        UUID subjectId = UUID.randomUUID();
        when(threadService.listRecent(0, 10))
                .thenReturn(List.of(buildResponse(subjectId, "Hilo de prueba", false)));

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
        UUID subjectId = UUID.randomUUID();

        when(threadService.get(eq(id)))
                .thenReturn(buildResponse(id, subjectId, "¿Cómo resolver integrales dobles?", false));

        mockMvc.perform(get("/api/v1/threads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("¿Cómo resolver integrales dobles?"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.subjectId").value(subjectId.toString()));
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
    // Helpers
    // =========================================================================

    private CreateThreadRequest validRequest(UUID subjectId) {
        var r = new CreateThreadRequest();
        r.setTitle("¿Cómo resolver integrales dobles?");
        r.setBody("Necesito ayuda con integrales dobles de funciones trigonométricas.");
        r.setSubjectId(subjectId);
        return r;
    }

    private ThreadResponse buildResponse(UUID subjectId, String title, boolean anonymous) {
        return ThreadResponse.builder()
                .id(UUID.randomUUID())
                .title(title)
                .body("Necesito ayuda con integrales dobles de funciones trigonométricas.")
                .anonymous(anonymous)
                .authorDisplay(anonymous ? "Anónimo" : "Juan Pérez")
                .subjectId(subjectId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ThreadResponse buildResponse(UUID id, UUID subjectId, String title, boolean anonymous) {
        return ThreadResponse.builder()
                .id(id)
                .title(title)
                .body("Necesito ayuda con integrales dobles de funciones trigonométricas.")
                .anonymous(anonymous)
                .authorDisplay(anonymous ? "Anónimo" : "Juan Pérez")
                .subjectId(subjectId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ThreadResponse buildAnonymousResponse(UUID subjectId, String title) {
        return ThreadResponse.builder()
                .id(UUID.randomUUID())
                .title(title)
                .body("Tengo dudas sobre este concepto matemático.")
                .anonymous(true)
                .authorDisplay("Anónimo")
                .subjectId(subjectId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
