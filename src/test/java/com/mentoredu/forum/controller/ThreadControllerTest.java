package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.service.IThreadService;
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
 * Tests de capa web para ThreadController.
 * Cubre: US12 — Crear, listar, ver y cerrar hilos de foro.
 * Reglas: RN-12 (al menos uno de university/course/career), RN-13 (anonymous=true → authorDisplay="Anónimo"),
 *         RN-14 (solo autor o MODERATOR puede cerrar).
 * Validado en producción (CONFIRMACION_TESTS.md HU-12):
 *   12.1 con universityId → 201; 12.2 anónimo → Anónimo; 12.3 sin contexto → 400;
 *   12.4 area sin university → 400; 12.5 careerId+courseId → 400;
 *   12.6 area de otra universidad → 400; 12.7 close por no-autor → 403; 12.8 sin auth → 401.
 */
@WebMvcTest(
    controllers = ThreadController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ThreadControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IThreadService threadService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /threads (US12) ───────────────────────────────────────────────────

    /** US12 Escenario 12.1 — hilo con universityId → 201 con Location header */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withUniversityId_returns201() throws Exception {
        var threadId = UUID.randomUUID();
        var response = ThreadResponse.builder()
            .id(threadId).title("¿Cómo estudiar Cálculo?").body("Busco consejos")
            .status("OPEN").authorDisplay("Juan Pérez").anonymous(false)
            .universityId(UUID.randomUUID())
            .likeCount(0).dislikeCount(0)
            .createdAt(LocalDateTime.now()).build();
        when(threadService.create(any(), eq("user@example.com"))).thenReturn(response);

        var req = validRequest();
        req.setUniversityId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(header().exists("Location"));
    }

    /** US12 Escenario 12.2 — hilo anónimo → authorDisplay="Anónimo" (RN-13) */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_anonymousThread_returns201WithAnonDisplay() throws Exception {
        var response = ThreadResponse.builder()
            .id(UUID.randomUUID()).title("Pregunta anónima").body("...")
            .status("OPEN").authorDisplay("Anónimo").anonymous(true)
            .universityId(UUID.randomUUID())
            .createdAt(LocalDateTime.now()).build();
        when(threadService.create(any(), any())).thenReturn(response);

        var req = validRequest();
        req.setUniversityId(UUID.randomUUID());
        req.setAnonymous(true);

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.authorDisplay").value("Anónimo"))
            .andExpect(jsonPath("$.anonymous").value(true));
    }

    /** US12 / RN-12 Escenario 12.3 — sin ningún campo de contexto → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withoutContextFields_returns400() throws Exception {
        when(threadService.create(any(), any()))
            .thenThrow(new IllegalArgumentException(
                "El hilo requiere al menos una categoría (universityId, courseId o careerId)"));

        var req = validRequest(); // sin universityId, courseId, careerId

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US12 / RN-12 Escenario 12.4 — areaId sin universityId → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_areaIdWithoutUniversityId_returns400() throws Exception {
        when(threadService.create(any(), any()))
            .thenThrow(new IllegalArgumentException("El área requiere una universidad seleccionada"));

        var req = validRequest();
        req.setAreaId(UUID.randomUUID()); // area sin university → inválido RN-12

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /** US12 / RN-12 Escenario 12.5 — careerId + courseId simultáneos → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_careerIdAndCourseIdTogether_returns400() throws Exception {
        when(threadService.create(any(), any()))
            .thenThrow(new IllegalArgumentException("No puedes combinar carrera y curso en el mismo hilo"));

        var req = validRequest();
        req.setCareerId(UUID.randomUUID());
        req.setCourseId(UUID.randomUUID()); // RN-12: careerId + courseId prohibido

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US12 / RN-12 Escenario 12.6 — área de otra universidad → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_areaNotBelongingToUniversity_returns400() throws Exception {
        when(threadService.create(any(), any()))
            .thenThrow(new IllegalArgumentException("El área no pertenece a la universidad seleccionada"));

        var req = validRequest();
        req.setUniversityId(UUID.randomUUID());
        req.setAreaId(UUID.randomUUID()); // área de otra universidad

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US12 — título en blanco → 400 (validación @NotBlank) */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withBlankTitle_returns400() throws Exception {
        var req = validRequest();
        req.setTitle("");

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.title").exists());
    }

    /** US12 — body en blanco → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_withBlankBody_returns400() throws Exception {
        var req = validRequest();
        req.setBody("");

        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.body").exists());
    }

    /** US12 Escenario 12.8 — sin autenticación → 401 */
    @Test
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRequest())))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /threads (US12) ────────────────────────────────────────────────────

    /** US12 — listar hilos recientes → 200 con contenido paginado */
    @Test
    @WithMockUser(username = "user@example.com")
    void listRecent_returns200WithPagedContent() throws Exception {
        var thread = ThreadResponse.builder()
            .id(UUID.randomUUID()).title("Test").body("Body")
            .status("OPEN").authorDisplay("Juan").createdAt(LocalDateTime.now()).build();
        var paged = new PagedResponse<>(List.of(thread), 0, 10, 1L, 1, true);
        when(threadService.listRecent(eq(0), eq(10), any(), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/threads"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].title").value("Test"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    /** GET /threads — sin autenticación → 401 */
    @Test
    void listRecent_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/threads"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /threads/{id} (US12) ───────────────────────────────────────────────

    /** US12 — obtener hilo existente por ID → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getById_existingThread_returns200() throws Exception {
        var threadId = UUID.randomUUID();
        var response = ThreadResponse.builder()
            .id(threadId).title("Test").body("Body").status("OPEN")
            .createdAt(LocalDateTime.now()).build();
        when(threadService.get(eq(threadId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/threads/" + threadId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(threadId.toString()));
    }

    /** US12 — hilo no encontrado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getById_notFound_returns404() throws Exception {
        var threadId = UUID.randomUUID();
        when(threadService.get(eq(threadId), any()))
            .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        mockMvc.perform(get("/api/v1/threads/" + threadId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ── PATCH /threads/{id}/close (US12) ──────────────────────────────────────

    /** US12 Escenario 12.7 — autor cierra su hilo → 200 con status=CLOSED */
    @Test
    @WithMockUser(username = "author@example.com")
    void close_byAuthor_returns200WithClosedStatus() throws Exception {
        var threadId = UUID.randomUUID();
        var response = ThreadResponse.builder()
            .id(threadId).title("Test").body("Body").status("CLOSED")
            .createdAt(LocalDateTime.now()).build();
        when(threadService.close(eq(threadId), eq("author@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/threads/" + threadId + "/close"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    /** US12 / RN-14 Escenario 12.7 — no-autor intenta cerrar hilo → 403 */
    @Test
    @WithMockUser(username = "other@example.com")
    void close_byNonAuthor_returns403() throws Exception {
        var threadId = UUID.randomUUID();
        when(threadService.close(eq(threadId), any()))
            .thenThrow(new ThreadNotOwnedException("Solo el autor puede cerrar este hilo"));

        mockMvc.perform(patch("/api/v1/threads/" + threadId + "/close"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /** US12 — cerrar hilo inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void close_notFound_returns404() throws Exception {
        var threadId = UUID.randomUUID();
        when(threadService.close(eq(threadId), any()))
            .thenThrow(new ThreadNotFoundException("Thread not found"));

        mockMvc.perform(patch("/api/v1/threads/" + threadId + "/close"))
            .andExpect(status().isNotFound());
    }

    /** PATCH /threads/{id}/close — sin autenticación → 401 */
    @Test
    void close_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/threads/" + UUID.randomUUID() + "/close"))
            .andExpect(status().isUnauthorized());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Request base: título y body válidos, sin campos de contexto (para pruebas de validación) */
    private CreateThreadRequest validRequest() {
        var req = new CreateThreadRequest();
        req.setTitle("¿Cómo estudiar Cálculo?");
        req.setBody("Busco consejos para preparar el parcial de Cálculo I");
        return req;
    }
}
