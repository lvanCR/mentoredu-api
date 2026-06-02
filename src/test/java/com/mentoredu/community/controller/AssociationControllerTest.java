package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.exception.*;
import com.mentoredu.community.service.IAssociationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para AssociationController.
 * Cubre: US24 — Asociar docente a academia (solicitar, listar, aceptar, rechazar).
 * Regla RN-18: asociación queda PENDING hasta que la academia la resuelva.
 * Validado en producción (CONFIRMACION_TESTS.md HU-24):
 *   24.1 TEACHER solicita → 201; 24.2 duplicada → 409; 24.3 academia acepta → 200;
 *   24.4 academia rechaza → 200; 24.5 no-academia resuelve → 403; 24.6 sin auth → 401.
 * Endpoint real: /api/v1/associations/teacher-academy (no /verification/teacher-links).
 */
@WebMvcTest(
    controllers = AssociationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AssociationControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IAssociationService associationService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /associations/teacher-academy (US24) ─────────────────────────────

    /** US24 Escenario 24.1 — TEACHER solicita asociación → 201 con status=PENDING */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_byTeacher_returns201() throws Exception {
        var response = Mockito.mock(AssociationResponse.class);
        Mockito.when(response.getStatus()).thenReturn("PENDING");
        when(associationService.requestAssociation(any(), eq("teacher@example.com"))).thenReturn(response);

        var req = new CreateAssociationRequest(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /** US24 / RN-18 Escenario 24.2 — solicitud duplicada → 409 Conflict */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_duplicate_returns409() throws Exception {
        when(associationService.requestAssociation(any(), any()))
            .thenThrow(new DuplicateAssociationException("Ya existe una solicitud de asociación con esta academia"));

        var req = new CreateAssociationRequest(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US24 — academia no encontrada → 404 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_academyNotFound_returns404() throws Exception {
        when(associationService.requestAssociation(any(), any()))
            .thenThrow(new AcademyNotFoundException("Academia no encontrada"));

        var req = new CreateAssociationRequest(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    /** US24 — academyProfileId ausente → 400 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_missingAcademyProfileId_returns400() throws Exception {
        var req = new CreateAssociationRequest(null);
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.academyProfileId").exists());
    }

    /** US24 — requiere ser TEACHER (TeacherProfileRequired) → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void requestAssociation_noTeacherProfile_returns403() throws Exception {
        when(associationService.requestAssociation(any(), any()))
            .thenThrow(new TeacherProfileRequiredException("Solo docentes pueden solicitar asociaciones"));

        var req = new CreateAssociationRequest(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** POST /associations/teacher-academy — sin autenticación → 401 */
    @Test
    void requestAssociation_unauthenticated_returns401() throws Exception {
        var req = new CreateAssociationRequest(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /associations/teacher-academy/me (US24) ───────────────────────────

    /** US24 — TEACHER ve sus asociaciones → 200 con lista */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void myAssociations_asTeacher_returns200() throws Exception {
        when(associationService.getMyAssociations(eq("teacher@example.com"))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/associations/teacher-academy/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    /** GET /associations/teacher-academy/me — sin autenticación → 401 */
    @Test
    void myAssociations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/associations/teacher-academy/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /associations/teacher-academy/academy (US24) ─────────────────────

    /** US24 — ACADEMY ve solicitudes recibidas → 200 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void academyRequests_asAcademy_returns200() throws Exception {
        when(associationService.getAcademyRequests(eq("academy@example.com"))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/associations/teacher-academy/academy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ── PATCH /associations/teacher-academy/{id}/accept (US24) ───────────────

    /** US24 Escenario 24.3 — ACADEMY acepta solicitud → 200 con status=ACCEPTED */
    @Test
    @WithMockUser(username = "academy@example.com")
    void accept_byAcademy_returns200WithAcceptedStatus() throws Exception {
        var linkId = UUID.randomUUID();
        var response = Mockito.mock(AssociationResponse.class);
        Mockito.when(response.getStatus()).thenReturn("ACCEPTED");
        when(associationService.acceptAssociation(eq(linkId), eq("academy@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + linkId + "/accept"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    /** US24 Escenario 24.5 — STUDENT intenta aceptar solicitud → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void accept_byStudent_returns403() throws Exception {
        when(associationService.acceptAssociation(any(), any()))
            .thenThrow(new NotAssociationOwnerException("Solo academias pueden resolver solicitudes"));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + UUID.randomUUID() + "/accept"))
            .andExpect(status().isForbidden());
    }

    /** US24 — asociación inexistente → 404 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void accept_notFound_returns404() throws Exception {
        when(associationService.acceptAssociation(any(), any()))
            .thenThrow(new AssociationNotFoundException("Asociación no encontrada"));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + UUID.randomUUID() + "/accept"))
            .andExpect(status().isNotFound());
    }

    /** PATCH /accept — sin autenticación → 401 */
    @Test
    void accept_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + UUID.randomUUID() + "/accept"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /associations/teacher-academy/{id}/reject (US24) ───────────────

    /** US24 Escenario 24.4 — ACADEMY rechaza solicitud → 200 con status=REJECTED */
    @Test
    @WithMockUser(username = "academy@example.com")
    void reject_byAcademy_returns200WithRejectedStatus() throws Exception {
        var linkId = UUID.randomUUID();
        var response = Mockito.mock(AssociationResponse.class);
        Mockito.when(response.getStatus()).thenReturn("REJECTED");
        when(associationService.rejectAssociation(eq(linkId), eq("academy@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + linkId + "/reject"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    /** PATCH /reject — sin autenticación → 401 */
    @Test
    void reject_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/associations/teacher-academy/" + UUID.randomUUID() + "/reject"))
            .andExpect(status().isUnauthorized());
    }
}
