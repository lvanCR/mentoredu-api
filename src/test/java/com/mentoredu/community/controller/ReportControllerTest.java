package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;
import com.mentoredu.community.exception.*;
import com.mentoredu.community.service.IReportService;
import com.mentoredu.config.PagedResponse;
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
 * Tests de capa web para ReportController.
 * Cubre: US25 (reportar contenido), US26 (resolver reporte).
 * Reglas: RN-19 (audit log en BD), doble check @PreAuthorize + SecurityUtils.requireAnyRole.
 * Validado en producción (CONFIRMACION_TESTS.md HU-25/HU-26):
 *   25.1 reporte THREAD → 201; 25.2 ANSWER → 201; 25.3 RESOURCE → 201;
 *   25.4 targetType inválido → 400; 25.5 sin auth → 401.
 *   26.1/2 MODERATOR resuelve → 200; 26.3 reporte ya resuelto → 409;
 *   26.4 sin rol MODERATOR/ADMIN → 403; 26.5 sin auth → 401.
 * Nota: Request de resolve solo tiene resolutionNote (no action). Confirmado en HU-26.
 */
@WebMvcTest(
    controllers = ReportController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IReportService reportService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /moderation/reports (US25) ───────────────────────────────────────

    /** US25 Escenario 25.1 — reporte de hilo exitoso → 201 con createdAt poblado */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_reportThread_returns201() throws Exception {
        var targetId = UUID.randomUUID();
        var response = ReportResponse.builder()
            .id(UUID.randomUUID()).reporterId(UUID.randomUUID())
            .targetType("THREAD").targetId(targetId)
            .reason("Contenido inapropiado").status("OPEN")
            .createdAt(LocalDateTime.now()).build(); // BUG #5 fix: createdAt no null via saveAndFlush
        when(reportService.create(any(), eq("user@example.com"))).thenReturn(response);

        var req = new ReportRequest("THREAD", targetId, "Contenido inapropiado");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.targetType").value("THREAD"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    /** US25 Escenario 25.2 — reporte de respuesta (ANSWER) → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_reportAnswer_returns201() throws Exception {
        var response = ReportResponse.builder()
            .id(UUID.randomUUID()).targetType("ANSWER").status("OPEN")
            .createdAt(LocalDateTime.now()).build();
        when(reportService.create(any(), any())).thenReturn(response);

        var req = new ReportRequest("ANSWER", UUID.randomUUID(), "Respuesta ofensiva");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.targetType").value("ANSWER"));
    }

    /** US25 Escenario 25.3 — reporte de recurso (RESOURCE) → 201 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_reportResource_returns201() throws Exception {
        var response = ReportResponse.builder()
            .id(UUID.randomUUID()).targetType("RESOURCE").status("OPEN")
            .createdAt(LocalDateTime.now()).build();
        when(reportService.create(any(), any())).thenReturn(response);

        var req = new ReportRequest("RESOURCE", UUID.randomUUID(), "Contenido plagiado");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US25 Escenario 25.4 — targetType inválido → 400 con mensaje de validación */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_invalidTargetType_returns400() throws Exception {
        var req = new ReportRequest("INVALID_TYPE", UUID.randomUUID(), "Razón");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.targetType").exists());
    }

    /** US25 — reporte duplicado (mismo target, misma razón) → 409 Conflict */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_duplicateReport_returns409() throws Exception {
        when(reportService.create(any(), any()))
            .thenThrow(new DuplicateReportException("Ya reportaste este contenido"));

        var req = new ReportRequest("THREAD", UUID.randomUUID(), "Razón");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    /** US25 — target inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_targetNotFound_returns404() throws Exception {
        when(reportService.create(any(), any()))
            .thenThrow(new ReportTargetNotFoundException("El contenido reportado no existe"));

        var req = new ReportRequest("THREAD", UUID.randomUUID(), "Razón");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    /** US25 — reason en blanco → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void create_blankReason_returns400() throws Exception {
        var req = new ReportRequest("THREAD", UUID.randomUUID(), "");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US25 Escenario 25.5 — sin autenticación → 401 */
    @Test
    void create_unauthenticated_returns401() throws Exception {
        var req = new ReportRequest("THREAD", UUID.randomUUID(), "Razón");
        mockMvc.perform(post("/api/v1/moderation/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /moderation/reports (US26) ─────────────────────────────────────────

    /** US26 — MODERATOR lista reportes abiertos → 200 */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void listOpen_asModerator_returns200() throws Exception {
        var paged = new PagedResponse<ReportResponse>(List.of(), 0, 20, 0L, 0, true);
        when(reportService.listOpen(eq(0), eq(20))).thenReturn(paged);

        mockMvc.perform(get("/api/v1/moderation/reports"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /** US26 Escenario 26.4 / RN-19 — STUDENT intenta listar reportes → 403 */
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void listOpen_asStudent_returns403() throws Exception {
        // SecurityUtils.requireAnyRole("MODERATOR","ADMIN") lanza AccessDeniedException → 403
        mockMvc.perform(get("/api/v1/moderation/reports"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /** GET /moderation/reports — sin autenticación → 401 */
    @Test
    void listOpen_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/reports"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /moderation/reports/{id}/resolve (US26) ─────────────────────────

    /** US26 Escenario 26.1 — MODERATOR resuelve reporte → 200 con resolutionNote */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void resolve_asModerator_returns200() throws Exception {
        var reportId = UUID.randomUUID();
        var response = ReportResponse.builder()
            .id(reportId).status("RESOLVED")
            .resolutionNote("Se envió advertencia al usuario")
            .resolvedAt(LocalDateTime.now()).build();
        when(reportService.resolve(eq(reportId), any(), eq("moderator@mentoredu.com"))).thenReturn(response);

        var req = new ResolveReportRequest("Se envió advertencia al usuario");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + reportId + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.resolutionNote").value("Se envió advertencia al usuario"));
    }

    /** US26 Escenario 26.3 — reporte ya resuelto → 409 Conflict */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void resolve_alreadyResolved_returns409() throws Exception {
        when(reportService.resolve(any(), any(), any()))
            .thenThrow(new ReportAlreadyResolvedException("El reporte ya fue resuelto"));

        var req = new ResolveReportRequest("Nota de resolución");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + UUID.randomUUID() + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US26 — reporte inexistente → 404 */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void resolve_reportNotFound_returns404() throws Exception {
        when(reportService.resolve(any(), any(), any()))
            .thenThrow(new ReportNotFoundException("Report not found"));

        var req = new ResolveReportRequest("Nota");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + UUID.randomUUID() + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    /** US26 — resolutionNote en blanco → 400 (obligatorio según HU-26) */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void resolve_blankResolutionNote_returns400() throws Exception {
        var req = new ResolveReportRequest("");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + UUID.randomUUID() + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US26 Escenario 26.4 — STUDENT intenta resolver reporte → 403 */
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void resolve_asStudent_returns403() throws Exception {
        // SecurityUtils.requireAnyRole("MODERATOR","ADMIN") lanza AccessDeniedException → 403
        var req = new ResolveReportRequest("Nota");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + UUID.randomUUID() + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** US26 Escenario 26.5 — sin autenticación → 401 */
    @Test
    void resolve_unauthenticated_returns401() throws Exception {
        var req = new ResolveReportRequest("Nota");
        mockMvc.perform(patch("/api/v1/moderation/reports/" + UUID.randomUUID() + "/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
