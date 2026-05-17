package com.mentoredu.moderation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.exception.DuplicateReportException;
import com.mentoredu.moderation.exception.ReportedContentNotFoundException;
import com.mentoredu.moderation.exception.SelfReportException;
import com.mentoredu.moderation.model.enums.TargetType;
import com.mentoredu.moderation.service.IReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
        com.mentoredu.moderation.model.Report report = com.mentoredu.moderation.model.Report.builder()
                .id(UUID.randomUUID())
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .status(com.mentoredu.moderation.model.enums.ReportStatus.OPEN)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        return new ReportResponse(report);
    }
}
