package com.mentoredu.verification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;
import com.mentoredu.verification.exception.DuplicateVerificationRequestException;
import com.mentoredu.verification.exception.UnauthorizedVerificationException;
import com.mentoredu.verification.model.VerificationDocument;
import com.mentoredu.verification.model.VerificationRequest;
import com.mentoredu.verification.model.enums.EntityType;
import com.mentoredu.verification.model.enums.VerificationStatus;
import com.mentoredu.verification.service.IVerificationService;
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

@WebMvcTest(controllers = VerificationController.class)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IVerificationService verificationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US21 — Teacher verification: POST /api/v1/verification/requests
    // =========================================================================

    // Gherkin: Exitoso — credencial válida → 201 Created, estado PENDING
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_teacherWithValidDocument_returns201() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.TEACHER, "DNI", "https://docs.example.com/dni.pdf");
        VerificationRequestResponse response = buildResponse(EntityType.TEACHER, "https://docs.example.com/dni.pdf");

        when(verificationService.submit(eq("teacher@example.com"), any(SubmitVerificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.entityType").value("TEACHER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.documentType").value("DNI"))
                .andExpect(jsonPath("$.fileUrl").value("https://docs.example.com/dni.pdf"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty());
    }

    // Gherkin: Error — archivo no cumple formato → 400 Bad Request (campos vacíos)
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Gherkin: Alternativo exitoso — documento profesional adjunto → 201 Created, estado PENDING
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_teacherWithProfessionalDocument_returns201WithPending() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.TEACHER, "TITULO_UNIVERSITARIO",
                "https://docs.example.com/titulo.pdf");
        VerificationRequestResponse response = buildResponse(EntityType.TEACHER, "https://docs.example.com/titulo.pdf");

        when(verificationService.submit(eq("teacher@example.com"), any(SubmitVerificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // Gherkin: Alternativo error — solicitud ya existe (RN-24) → 409 Conflict
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_duplicateRequest_returns409() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.TEACHER, "DNI", "https://docs.example.com/dni.pdf");

        when(verificationService.submit(eq("teacher@example.com"), any(SubmitVerificationRequest.class)))
                .thenThrow(new DuplicateVerificationRequestException(
                        "Ya tienes una solicitud de verificación pendiente para TEACHER"));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya tienes una solicitud de verificación pendiente para TEACHER"));
    }

    // Extra: rol incorrecto → 403 Forbidden (RN-25)
    @Test
    @WithMockUser(username = "student@example.com")
    void submit_wrongRole_returns403() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.TEACHER, "DNI", "https://docs.example.com/dni.pdf");

        when(verificationService.submit(eq("student@example.com"), any(SubmitVerificationRequest.class)))
                .thenThrow(new UnauthorizedVerificationException(
                        "Solo docentes y academias pueden solicitar verificación"));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Solo docentes y academias pueden solicitar verificación"));
    }

    // Extra: entityType no coincide con rol → 403 Forbidden (RN-25)
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_entityTypeMismatch_returns403() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.ACADEMY, "RUC", "https://docs.example.com/ruc.pdf");

        when(verificationService.submit(eq("teacher@example.com"), any(SubmitVerificationRequest.class)))
                .thenThrow(new UnauthorizedVerificationException(
                        "El tipo de entidad no corresponde a tu rol. Tu rol permite: TEACHER"));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void submit_withoutAuth_returns401() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.TEACHER, "DNI", "https://docs.example.com/dni.pdf");

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US22 — Organization verification: POST /api/v1/verification/requests
    // =========================================================================

    // Gherkin: Exitoso — documentación válida → 201 Created, estado PENDING
    @Test
    @WithMockUser(username = "academy@example.com")
    void submit_academyWithValidDocument_returns201() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.ACADEMY, "RUC", "https://docs.example.com/ruc.pdf");
        VerificationRequestResponse response = buildAcademyResponse("https://docs.example.com/ruc.pdf");

        when(verificationService.submit(eq("academy@example.com"), any(SubmitVerificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityType").value("ACADEMY"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.documentType").value("RUC"));
    }

    // Gherkin: Error — documentación inválida/incompleta → 400 Bad Request
    @Test
    @WithMockUser(username = "academy@example.com")
    void submit_academy_missingFileUrl_returns400() throws Exception {
        SubmitVerificationRequest req = new SubmitVerificationRequest();
        req.setEntityType(EntityType.ACADEMY);
        req.setDocumentType("RUC");
        // fileUrl missing

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Gherkin: Alternativo exitoso — documentos completos → 201 Created
    @Test
    @WithMockUser(username = "academy@example.com")
    void submit_academyWithCompleteDocuments_returns201() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.ACADEMY, "LICENCIA_FUNCIONAMIENTO",
                "https://docs.example.com/licencia.pdf");
        VerificationRequestResponse response = buildAcademyResponse("https://docs.example.com/licencia.pdf");

        when(verificationService.submit(eq("academy@example.com"), any(SubmitVerificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // Gherkin: Alternativo error — solicitud ya activa → 409 Conflict (RN-24)
    @Test
    @WithMockUser(username = "academy@example.com")
    void submit_academy_duplicateRequest_returns409() throws Exception {
        SubmitVerificationRequest req = buildRequest(EntityType.ACADEMY, "RUC", "https://docs.example.com/ruc.pdf");

        when(verificationService.submit(eq("academy@example.com"), any(SubmitVerificationRequest.class)))
                .thenThrow(new DuplicateVerificationRequestException(
                        "Ya tienes una solicitud de verificación pendiente para ACADEMY"));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya tienes una solicitud de verificación pendiente para ACADEMY"));
    }

    // =========================================================================
    // US21 / US22 — GET /api/v1/verification/requests/me
    // =========================================================================

    // Exitoso — tiene solicitudes → lista no vacía
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getMyRequests_withRequests_returnsList() throws Exception {
        VerificationRequestResponse response = buildResponse(EntityType.TEACHER, "https://docs.example.com/dni.pdf");

        when(verificationService.getMyRequests(eq("teacher@example.com")))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/verification/requests/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("TEACHER"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // Alternativo exitoso — sin solicitudes → lista vacía sin error
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getMyRequests_noRequests_returnsEmptyList() throws Exception {
        when(verificationService.getMyRequests(eq("teacher@example.com")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/verification/requests/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Error: sin autenticación → 401 Unauthorized
    @Test
    void getMyRequests_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/verification/requests/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private SubmitVerificationRequest buildRequest(EntityType entityType, String documentType, String fileUrl) {
        SubmitVerificationRequest req = new SubmitVerificationRequest();
        req.setEntityType(entityType);
        req.setDocumentType(documentType);
        req.setFileUrl(fileUrl);
        return req;
    }

    private VerificationRequestResponse buildResponse(EntityType entityType, String fileUrl) {
        VerificationRequest req = buildVerificationRequest(entityType);
        VerificationDocument doc = buildDocument(req, "DNI", fileUrl);
        return new VerificationRequestResponse(req, doc);
    }

    private VerificationRequestResponse buildAcademyResponse(String fileUrl) {
        VerificationRequest req = buildVerificationRequest(EntityType.ACADEMY);
        VerificationDocument doc = buildDocument(req, "RUC", fileUrl);
        return new VerificationRequestResponse(req, doc);
    }

    private VerificationRequest buildVerificationRequest(EntityType entityType) {
        com.mentoredu.auth.entity.User user = com.mentoredu.auth.entity.User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .build();

        return VerificationRequest.builder()
                .id(UUID.randomUUID())
                .user(user)
                .entityType(entityType)
                .status(VerificationStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    private VerificationDocument buildDocument(VerificationRequest request, String documentType, String fileUrl) {
        return VerificationDocument.builder()
                .id(UUID.randomUUID())
                .request(request)
                .documentType(documentType)
                .fileUrl(fileUrl)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
