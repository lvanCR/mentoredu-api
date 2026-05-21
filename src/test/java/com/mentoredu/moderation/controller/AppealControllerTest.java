package com.mentoredu.moderation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;
import com.mentoredu.moderation.exception.DuplicateAppealException;
import com.mentoredu.moderation.exception.ReportNotFoundException;
import com.mentoredu.moderation.model.Appeal;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.model.enums.ReportStatus;
import com.mentoredu.moderation.model.enums.TargetType;
import com.mentoredu.moderation.service.IAppealService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppealController.class)
class AppealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAppealService appealService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US38 — submit: POST /api/v1/moderation/appeals
    // =========================================================================

    // Gherkin: Exitoso — reporte existe, sin apelación previa, motivo válido → 201 Created, status OPEN
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_validAppeal_returns201WithOpenStatus() throws Exception {
        UUID reportId = UUID.randomUUID();
        AppealRequest request = buildRequest(reportId, "La decisión fue incorrecta");
        AppealResponse response = buildResponse(reportId, "La decisión fue incorrecta");

        when(appealService.submit(any(AppealRequest.class), eq("user@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.reason").value("La decisión fue incorrecta"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // Gherkin: Error — motivo omitido → 400 Bad Request (validación @NotBlank, RN-44)
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_missingReason_returns400() throws Exception {
        AppealRequest request = buildRequest(UUID.randomUUID(), "");

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Gherkin: Alternativo exitoso — apelación presentada correctamente → estado OPEN visible
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_validAppeal_statusIsOpenAndCreatedAtIsPresent() throws Exception {
        UUID reportId = UUID.randomUUID();
        AppealRequest request = buildRequest(reportId, "Solicito revisión del caso");
        AppealResponse response = buildResponse(reportId, "Solicito revisión del caso");

        when(appealService.submit(any(AppealRequest.class), eq("user@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    // Gherkin: Alternativo error — apelación duplicada para el mismo reporte → 409 Conflict (RN-43)
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_duplicateAppeal_returns409() throws Exception {
        UUID reportId = UUID.randomUUID();
        AppealRequest request = buildRequest(reportId, "Ya apelé antes");

        when(appealService.submit(any(AppealRequest.class), eq("user@example.com")))
                .thenThrow(new DuplicateAppealException("Ya presentaste una apelación para este reporte"));

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya presentaste una apelación para este reporte"));
    }

    // Extra: reporte inexistente → 404 Not Found
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_reportNotFound_returns404() throws Exception {
        UUID reportId = UUID.randomUUID();
        AppealRequest request = buildRequest(reportId, "Quiero apelar");

        when(appealService.submit(any(AppealRequest.class), eq("user@example.com")))
                .thenThrow(new ReportNotFoundException("Reporte no encontrado: " + reportId));

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Reporte no encontrado: " + reportId));
    }

    // Extra: reportId ausente → 400 Bad Request (validación @NotNull)
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_missingReportId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Motivo válido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void submit_withoutAuth_returns401() throws Exception {
        AppealRequest request = buildRequest(UUID.randomUUID(), "Motivo de apelación");

        mockMvc.perform(post("/api/v1/moderation/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private AppealRequest buildRequest(UUID reportId, String reason) {
        AppealRequest req = new AppealRequest();
        req.setReportId(reportId);
        req.setReason(reason);
        return req;
    }

    private AppealResponse buildResponse(UUID reportId, String reason) {
        Report report = Report.builder()
                .id(reportId)
                .targetType(TargetType.THREAD)
                .targetId(UUID.randomUUID())
                .reason("Contenido inapropiado")
                .status(ReportStatus.OPEN)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        Appeal appeal = Appeal.builder()
                .id(UUID.randomUUID())
                .report(report)
                .reason(reason)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();
        return new AppealResponse(appeal);
    }
}
