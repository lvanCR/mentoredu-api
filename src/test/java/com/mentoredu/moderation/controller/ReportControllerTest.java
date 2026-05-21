package com.mentoredu.moderation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.dto.ResolveReportRequest;
import com.mentoredu.moderation.dto.ResolveReportResponse;
import com.mentoredu.moderation.exception.DuplicateReportException;
import com.mentoredu.moderation.exception.ReportAlreadyResolvedException;
import com.mentoredu.moderation.exception.ReportNotFoundException;
import com.mentoredu.moderation.exception.ReportedContentNotFoundException;
import com.mentoredu.moderation.exception.SelfReportException;
import com.mentoredu.moderation.exception.UnauthorizedModerationException;
import com.mentoredu.moderation.model.ModerationAction;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.model.enums.ModerationActionType;
import com.mentoredu.moderation.model.enums.ReportStatus;
import com.mentoredu.moderation.model.enums.TargetType;
import com.mentoredu.moderation.service.IReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReportService reportService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US19 — create: POST /api/v1/moderation/reports
    // =========================================================================

    // Gherkin: Exitoso — motivo válido → 201 Created con reporte registrado
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_validReport_returns201WithBody() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.THREAD, targetId, "Contenido inapropiado");
        ReportResponse response = buildResponse(targetId, TargetType.THREAD, "Contenido inapropiado");

        when(reportService.create(any(ReportRequest.class), eq("reporter@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.targetType").value("THREAD"))
                .andExpect(jsonPath("$.targetId").value(targetId.toString()))
                .andExpect(jsonPath("$.reason").value("Contenido inapropiado"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // Gherkin: Error — no indica motivo → 400 Bad Request (validación @NotBlank)
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_missingReason_returns400() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.THREAD, targetId, "");

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Gherkin: Alternativo exitoso — categoría correcta → 201 guardado para revisión
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_resourceTarget_returns201() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.RESOURCE, targetId, "Recurso con contenido plagiado");
        ReportResponse response = buildResponse(targetId, TargetType.RESOURCE, "Recurso con contenido plagiado");

        when(reportService.create(any(ReportRequest.class), eq("reporter@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetType").value("RESOURCE"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // Gherkin: Alternativo error — contenido inexistente → 404 Not Found
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_contentNotFound_returns404() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.THREAD, targetId, "Spam");

        when(reportService.create(any(ReportRequest.class), eq("reporter@example.com")))
                .thenThrow(new ReportedContentNotFoundException("Hilo no encontrado: " + targetId));

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Hilo no encontrado: " + targetId));
    }

    // Extra: intento de reportar contenido propio → 400 Bad Request
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_selfReport_returns400() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.ANSWER, targetId, "Motivo cualquiera");

        when(reportService.create(any(ReportRequest.class), eq("reporter@example.com")))
                .thenThrow(new SelfReportException("No puedes reportar tu propio contenido"));

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("No puedes reportar tu propio contenido"));
    }

    // Extra: reporte duplicado → 409 Conflict
    @Test
    @WithMockUser(username = "reporter@example.com")
    void create_duplicateReport_returns409() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.COMMENT, targetId, "Ya lo había reportado");

        when(reportService.create(any(ReportRequest.class), eq("reporter@example.com")))
                .thenThrow(new DuplicateReportException("Ya has reportado este contenido anteriormente"));

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya has reportado este contenido anteriormente"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void create_withoutAuth_returns401() throws Exception {
        UUID targetId = UUID.randomUUID();
        ReportRequest request = buildRequest(TargetType.THREAD, targetId, "Contenido inapropiado");

        mockMvc.perform(post("/api/v1/moderation/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US20 — resolve: PATCH /api/v1/moderation/reports/{id}/resolve
    // =========================================================================

    // Gherkin: Exitoso — moderador con permisos + resolución válida → 200 OK, estado cambiado
    @Test
    @WithMockUser(username = "moderator@example.com")
    void resolve_asModerator_returns200WithResolvedStatus() throws Exception {
        UUID reportId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        ResolveReportRequest request = buildResolveRequest(ReportStatus.RESOLVED, ModerationActionType.HIDE, "Contenido ocultado por spam");
        ResolveReportResponse response = buildResolveResponse(reportId, targetId, ReportStatus.RESOLVED, ModerationActionType.HIDE, "Contenido ocultado por spam");

        when(reportService.resolve(eq(reportId), any(ResolveReportRequest.class), eq("moderator@example.com")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.actionType").value("HIDE"))
                .andExpect(jsonPath("$.resolvedBy").isNotEmpty())
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
    }

    // Gherkin: Error — sin permisos → 403 Forbidden
    @Test
    @WithMockUser(username = "student@example.com")
    void resolve_withoutModerationRole_returns403() throws Exception {
        UUID reportId = UUID.randomUUID();
        ResolveReportRequest request = buildResolveRequest(ReportStatus.RESOLVED, ModerationActionType.WARN, null);

        when(reportService.resolve(eq(reportId), any(ResolveReportRequest.class), eq("student@example.com")))
                .thenThrow(new UnauthorizedModerationException("No tienes permisos para resolver reportes"));

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No tienes permisos para resolver reportes"));
    }

    // Gherkin: Alternativo exitoso — admin cierra con REJECTED → 200 OK
    @Test
    @WithMockUser(username = "admin@example.com")
    void resolve_asAdminReject_returns200WithRejectedStatus() throws Exception {
        UUID reportId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        ResolveReportRequest request = buildResolveRequest(ReportStatus.REJECTED, ModerationActionType.RESTORE, "Reporte infundado");
        ResolveReportResponse response = buildResolveResponse(reportId, targetId, ReportStatus.REJECTED, ModerationActionType.RESTORE, "Reporte infundado");

        when(reportService.resolve(eq(reportId), any(ResolveReportRequest.class), eq("admin@example.com")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.actionType").value("RESTORE"))
                .andExpect(jsonPath("$.notes").value("Reporte infundado"));
    }

    // Gherkin: Alternativo error — reporte ya resuelto → 409 Conflict
    @Test
    @WithMockUser(username = "moderator@example.com")
    void resolve_alreadyResolved_returns409() throws Exception {
        UUID reportId = UUID.randomUUID();
        ResolveReportRequest request = buildResolveRequest(ReportStatus.RESOLVED, ModerationActionType.DELETE, null);

        when(reportService.resolve(eq(reportId), any(ResolveReportRequest.class), eq("moderator@example.com")))
                .thenThrow(new ReportAlreadyResolvedException("El reporte ya fue resuelto con estado: RESOLVED"));

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El reporte ya fue resuelto con estado: RESOLVED"));
    }

    // Extra: reporte no encontrado → 404 Not Found
    @Test
    @WithMockUser(username = "moderator@example.com")
    void resolve_reportNotFound_returns404() throws Exception {
        UUID reportId = UUID.randomUUID();
        ResolveReportRequest request = buildResolveRequest(ReportStatus.RESOLVED, ModerationActionType.HIDE, null);

        when(reportService.resolve(eq(reportId), any(ResolveReportRequest.class), eq("moderator@example.com")))
                .thenThrow(new ReportNotFoundException("Reporte no encontrado: " + reportId));

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Reporte no encontrado: " + reportId));
    }

    // Extra: campos obligatorios ausentes → 400 Bad Request
    @Test
    @WithMockUser(username = "moderator@example.com")
    void resolve_missingRequiredFields_returns400() throws Exception {
        UUID reportId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void resolve_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/moderation/reports/{id}/resolve", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildResolveRequest(ReportStatus.RESOLVED, ModerationActionType.HIDE, null))))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private ReportRequest buildRequest(TargetType targetType, UUID targetId, String reason) {
        ReportRequest req = new ReportRequest();
        req.setTargetType(targetType);
        req.setTargetId(targetId);
        req.setReason(reason);
        return req;
    }

    private ReportResponse buildResponse(UUID targetId, TargetType targetType, String reason) {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .status(ReportStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        return new ReportResponse(report);
    }

    private ResolveReportRequest buildResolveRequest(ReportStatus resolution, ModerationActionType actionType, String notes) {
        ResolveReportRequest req = new ResolveReportRequest();
        req.setResolution(resolution);
        req.setActionType(actionType);
        req.setNotes(notes);
        return req;
    }

    private ResolveReportResponse buildResolveResponse(UUID reportId, UUID targetId, ReportStatus resolution,
                                                        ModerationActionType actionType, String notes) {
        UUID moderatorId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .targetType(TargetType.THREAD)
                .targetId(targetId)
                .reason("Contenido inapropiado")
                .status(resolution)
                .createdAt(LocalDateTime.now().minusHours(1))
                .resolvedBy(moderatorId)
                .resolvedAt(LocalDateTime.now())
                .build();
        ModerationAction action = ModerationAction.builder()
                .id(UUID.randomUUID())
                .report(report)
                .actionType(actionType.name())
                .notes(notes)
                .build();
        return new ResolveReportResponse(report, action);
    }
}
