package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.*;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.model.VerificationStatus;
import com.mentoredu.community.service.IVerificationService;
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
 * Tests de capa web para VerificationController.
 * Cubre: US22 (solicitar verificación), US23 (aprobar/rechazar).
 * Reglas: RN-16 (al menos un documento), RN-17 (rechazo requiere notes).
 * Validado en producción (CONFIRMACION_TESTS.md HU-22/HU-23):
 *   22.1 solicitud con doc → 201; 22.2 sin docs → 400; 22.3 duplicada → 409;
 *   22.4 GET mis solicitudes → 200; 22.5 sin auth → 401.
 *   23.1 MODERATOR aprueba → 200; 23.2 rechaza con notes → 200;
 *   23.3 rechazo sin notes → 400; 23.4 ya procesada → 500 (IllegalStateException);
 *   23.5 sin rol → 403; 23.6 sin auth → 401.
 * Nota: Endpoint es PATCH /requests/{id}/review (un solo endpoint, no /approve y /reject).
 * IMPORTANTE: Tests sin @WithMockUser necesitan body válido para llegar a SecurityUtils.
 */
@WebMvcTest(
    controllers = VerificationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class VerificationControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IVerificationService verificationService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /verification/requests (US22) ────────────────────────────────────

    /** US22 Escenario 22.1 — solicitud con documento adjunto → 201 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_withDocument_returns201() throws Exception {
        var response = VerificationResponse.builder()
            .id(UUID.randomUUID()).userId(UUID.randomUUID())
            .entityType("TEACHER").status("PENDING")
            .submittedAt(LocalDateTime.now()).documents(List.of()).build();
        when(verificationService.submit(any(), eq("teacher@example.com"))).thenReturn(response);

        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/docs/dni.pdf");

        var req = new CreateVerificationRequest();
        req.setEntityType("TEACHER");
        req.setDocuments(List.of(doc));

        mockMvc.perform(post("/api/v1/verification/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.entityType").value("TEACHER"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /** US22 / RN-16 Escenario 22.2 — sin documentos adjuntos → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_withoutDocuments_returns400() throws Exception {
        var req = new CreateVerificationRequest();
        req.setEntityType("TEACHER");
        req.setDocuments(List.of()); // @NotEmpty falla

        mockMvc.perform(post("/api/v1/verification/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.documents").exists());
    }

    /** US22 — entityType inválido → 400 */
    @Test
    @WithMockUser(username = "user@example.com")
    void submit_invalidEntityType_returns400() throws Exception {
        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/doc.pdf");

        var req = new CreateVerificationRequest();
        req.setEntityType("USER"); // inválido: solo TEACHER o ACADEMY
        req.setDocuments(List.of(doc));

        mockMvc.perform(post("/api/v1/verification/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.entityType").exists());
    }

    /** US22 Escenario 22.3 — solicitud duplicada pendiente → 409 Conflict */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void submit_duplicatePending_returns409() throws Exception {
        when(verificationService.submit(any(), any()))
            .thenThrow(new DuplicateVerificationException("Ya tienes una solicitud de verificación pendiente"));

        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/doc.pdf");

        var req = new CreateVerificationRequest();
        req.setEntityType("TEACHER");
        req.setDocuments(List.of(doc));

        mockMvc.perform(post("/api/v1/verification/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US22 Escenario 22.5 — sin autenticación + body válido → 401 */
    @Test
    void submit_unauthenticated_returns401() throws Exception {
        // Body válido para que @Valid pase y llegue a SecurityUtils → 401
        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/doc.pdf");

        var req = new CreateVerificationRequest();
        req.setEntityType("TEACHER");
        req.setDocuments(List.of(doc));

        mockMvc.perform(post("/api/v1/verification/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /verification/requests/me (US22 Escenario 22.4) ──────────────────

    /** US22 Escenario 22.4 — ver mis solicitudes → 200 con lista paginada */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void myRequests_authenticated_returns200() throws Exception {
        var paged = new PagedResponse<VerificationResponse>(List.of(), 0, 20, 0L, 0, true);
        when(verificationService.getMyRequests(eq("teacher@example.com"), eq(0), eq(20)))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/verification/requests/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /** GET /verification/requests/me — sin autenticación → 401 */
    @Test
    void myRequests_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/verification/requests/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /verification/requests (US23) ─────────────────────────────────────

    /** US23 — MODERATOR lista todas las solicitudes → 200 */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void allRequests_asModerator_returns200() throws Exception {
        var paged = new PagedResponse<VerificationResponse>(List.of(), 0, 20, 0L, 0, true);
        when(verificationService.getAllRequests(any(), eq(0), eq(20))).thenReturn(paged);

        mockMvc.perform(get("/api/v1/verification/requests"))
            .andExpect(status().isOk());
    }

    /** US23 Escenario 23.5 — STUDENT intenta listar solicitudes → 403 */
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void allRequests_asStudent_returns403() throws Exception {
        // SecurityUtils.requireAnyRole("MODERATOR","ADMIN") lanza AccessDeniedException → 403
        mockMvc.perform(get("/api/v1/verification/requests"))
            .andExpect(status().isForbidden());
    }

    /** GET /verification/requests — sin autenticación → 401 */
    @Test
    void allRequests_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/verification/requests"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /verification/requests/{id}/review (US23) ───────────────────────
    // Confirmado en HU-23: un solo endpoint /review (no /approve y /reject)

    /** US23 Escenario 23.1 — MODERATOR aprueba solicitud → 200 con status=APPROVED */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void review_approve_returns200() throws Exception {
        var reqId = UUID.randomUUID();
        var response = VerificationResponse.builder()
            .id(reqId).status("APPROVED").reviewedAt(LocalDateTime.now()).documents(List.of()).build();
        when(verificationService.review(eq(reqId), any(), eq("moderator@mentoredu.com"))).thenReturn(response);

        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/verification/requests/" + reqId + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    /** US23 Escenario 23.2 — MODERATOR rechaza con notas → 200 con status=REJECTED */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void review_rejectWithNotes_returns200() throws Exception {
        var reqId = UUID.randomUUID();
        var response = VerificationResponse.builder()
            .id(reqId).status("REJECTED").notes("Documentos no legibles")
            .reviewedAt(LocalDateTime.now()).documents(List.of()).build();
        when(verificationService.review(eq(reqId), any(), any())).thenReturn(response);

        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.REJECTED);
        req.setNotes("Documentos no legibles");

        mockMvc.perform(patch("/api/v1/verification/requests/" + reqId + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    /** US23 / RN-17 Escenario 23.3 — rechazo sin notes → 400 (servicio lo valida) */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void review_rejectWithoutNotes_returns400() throws Exception {
        when(verificationService.review(any(), any(), any()))
            .thenThrow(new IllegalArgumentException(
                "El rechazo requiere una razón (notes) obligatoria (RN-17)"));

        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.REJECTED); // sin notes

        mockMvc.perform(patch("/api/v1/verification/requests/" + UUID.randomUUID() + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US23 — action ausente en review → 400 (validación @NotNull) */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void review_missingAction_returns400() throws Exception {
        var req = new ReviewVerificationRequest(); // action = null → @NotNull falla

        mockMvc.perform(patch("/api/v1/verification/requests/" + UUID.randomUUID() + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.action").exists());
    }

    /** US23 — solicitud inexistente → 404 */
    @Test
    @WithMockUser(username = "moderator@mentoredu.com", roles = "MODERATOR")
    void review_notFound_returns404() throws Exception {
        when(verificationService.review(any(), any(), any()))
            .thenThrow(new VerificationNotFoundException("Solicitud no encontrada"));

        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/verification/requests/" + UUID.randomUUID() + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    /** US23 Escenario 23.5 — TEACHER intenta revisar → 403 (RequireAnyRole) */
    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void review_withoutModeratorRole_returns403() throws Exception {
        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/verification/requests/" + UUID.randomUUID() + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** US23 Escenario 23.6 — sin autenticación + body válido → 401 */
    @Test
    void review_unauthenticated_returns401() throws Exception {
        // Body con action válida para que @Valid pase y llegue a SecurityUtils
        var req = new ReviewVerificationRequest();
        req.setAction(VerificationStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/verification/requests/" + UUID.randomUUID() + "/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
