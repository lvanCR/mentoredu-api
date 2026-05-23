package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;
import com.mentoredu.community.exception.DuplicateReportException;
import com.mentoredu.community.exception.ReportAlreadyResolvedException;
import com.mentoredu.community.exception.ReportNotFoundException;
import com.mentoredu.community.service.IReportService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IReportService reportService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US25 — Reportar contenido
    // =========================================================================

    @Test
    @WithMockUser(username = "reporter@example.com")
    void createReport_withValidData_returns201() throws Exception {
        UUID targetId = UUID.randomUUID();
        var request = new ReportRequest("THREAD", targetId, "Contenido ofensivo");
        var response = new ReportResponse(UUID.randomUUID(), UUID.randomUUID(), "THREAD", targetId, "Contenido ofensivo", "OPEN", LocalDateTime.now());

        when(reportService.create(eq(request), eq("reporter@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "reporter@example.com")
    void createReport_withMissingTargetType_returns400() throws Exception {
        String body = """
                { "targetId": "%s", "reason": "Contenido ofensivo" }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.targetType").exists());
    }

    @Test
    @WithMockUser(username = "reporter@example.com")
    void createReport_withMissingReason_returns400() throws Exception {
        String body = """
                { "targetType": "THREAD", "targetId": "%s" }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.reason").exists());
    }

    @Test
    @WithMockUser(username = "reporter@example.com")
    void createReport_duplicate_returns409() throws Exception {
        UUID targetId = UUID.randomUUID();
        var request = new ReportRequest("ANSWER", targetId, "Spam");

        when(reportService.create(eq(request), eq("reporter@example.com")))
                .thenThrow(new DuplicateReportException("Ya reportaste este contenido"));

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya reportaste este contenido"));
    }

    @Test
    void createReport_withoutAuth_returns401() throws Exception {
        var request = new ReportRequest("THREAD", UUID.randomUUID(), "Contenido ofensivo");

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US26 — Listar reportes abiertos (MODERATOR/ADMIN)
    // =========================================================================

    @Test
    @WithMockUser(username = "mod@example.com", roles = "MODERATOR")
    void listOpenReports_asModerator_returns200() throws Exception {
        var response = new ReportResponse(UUID.randomUUID(), UUID.randomUUID(), "THREAD", UUID.randomUUID(), "Spam", "OPEN", LocalDateTime.now());

        when(reportService.listOpen(anyInt(), anyInt())).thenReturn(pageOf(List.of(response)));

        mockMvc.perform(get("/api/v1/moderation/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void listOpenReports_asAdmin_returns200() throws Exception {
        when(reportService.listOpen(anyInt(), anyInt())).thenReturn(pageOf(List.of()));

        mockMvc.perform(get("/api/v1/moderation/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void listOpenReports_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listOpenReports_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/moderation/reports"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US26 — Resolver un reporte (MODERATOR/ADMIN)
    // =========================================================================

    @Test
    @WithMockUser(username = "mod@example.com", roles = "MODERATOR")
    void resolveReport_asModerator_returns200() throws Exception {
        UUID reportId = UUID.randomUUID();
        var request = new ResolveReportRequest("Contenido eliminado");
        var response = new ReportResponse(reportId, UUID.randomUUID(), "THREAD", UUID.randomUUID(), "Spam", "RESOLVED", LocalDateTime.now());

        when(reportService.resolve(eq(reportId), eq(request), eq("mod@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = "MODERATOR")
    void resolveReport_withoutNote_returns400() throws Exception {
        UUID reportId = UUID.randomUUID();
        var request = new ResolveReportRequest("");

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.resolutionNote").exists());
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = "MODERATOR")
    void resolveReport_notFound_returns404() throws Exception {
        UUID reportId = UUID.randomUUID();
        var request = new ResolveReportRequest("Acción tomada");

        when(reportService.resolve(eq(reportId), any(), any()))
                .thenThrow(new ReportNotFoundException("Reporte no encontrado: " + reportId));

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reporte no encontrado: " + reportId));
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = "MODERATOR")
    void resolveReport_alreadyResolved_returns409() throws Exception {
        UUID reportId = UUID.randomUUID();
        var request = new ResolveReportRequest("Ya fue resuelto");

        when(reportService.resolve(eq(reportId), any(), any()))
                .thenThrow(new ReportAlreadyResolvedException("El reporte ya fue resuelto"));

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El reporte ya fue resuelto"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void resolveReport_asStudent_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResolveReportRequest("nota"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void resolveReport_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResolveReportRequest("nota"))))
                .andExpect(status().isUnauthorized());
    }

    private <T> PagedResponse<T> pageOf(List<T> items) {
        return new PagedResponse<>(items, 0, 20, items.size(), items.isEmpty() ? 0 : 1, true);
    }
}
