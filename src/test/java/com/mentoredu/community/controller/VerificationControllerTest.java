package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.service.IVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VerificationController.class)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IVerificationService verificationService;
    @MockitoBean private JwtUtil               jwtUtil;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US22 — Solicitar verificación
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void submitVerification_withDocs_returns201() throws Exception {
        var request = validCreateRequest();
        var response = VerificationResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .entityType("TEACHER")
                .status("PENDING")
                .documents(List.of())
                .build();

        when(verificationService.submit(any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.entityType").value("TEACHER"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void submitVerification_whenPendingExists_returns409() throws Exception {
        when(verificationService.submit(any(), anyString()))
                .thenThrow(new DuplicateVerificationException("Ya tienes una solicitud de verificación pendiente"));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void submitVerification_withNoDocuments_returns400() throws Exception {
        var request = new CreateVerificationRequest();
        request.setEntityType("TEACHER");
        request.setDocuments(List.of());

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void submitVerification_withMissingEntityType_returns400() throws Exception {
        var request = new CreateVerificationRequest();
        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/docs/dni.pdf");
        request.setDocuments(List.of(doc));

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitVerification_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US22 — Ver mis solicitudes
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void myRequests_whenAuthenticated_returns200() throws Exception {
        var response = VerificationResponse.builder()
                .id(UUID.randomUUID())
                .entityType("TEACHER")
                .status("PENDING")
                .build();

        when(verificationService.getMyRequests("teacher@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/verification/requests/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].entityType").value("TEACHER"));
    }

    @Test
    void myRequests_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/verification/requests/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US23 — Aprobar verificación
    // =========================================================================

    @Test
    @WithMockUser(username = "mod@example.com", roles = {"MODERATOR"})
    void reviewVerification_approve_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        var response = VerificationResponse.builder()
                .id(requestId)
                .status("APPROVED")
                .build();

        var request = new ReviewVerificationRequest();
        request.setAction("APPROVED");

        when(verificationService.review(eq(requestId), any(), eq("mod@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = {"MODERATOR"})
    void reviewVerification_rejectWithoutNotes_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new ReviewVerificationRequest();
        request.setAction("REJECTED");

        when(verificationService.review(eq(id), any(), anyString()))
                .thenThrow(new IllegalArgumentException("El rechazo requiere una razón (notes) obligatoria (RN-17)"));

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = {"MODERATOR"})
    void reviewVerification_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(verificationService.review(eq(id), any(), anyString()))
                .thenThrow(new VerificationNotFoundException("Solicitud no encontrada: " + id));

        var request = new ReviewVerificationRequest();
        request.setAction("APPROVED");

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reviewVerification_asRegularUser_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new ReviewVerificationRequest();
        request.setAction("APPROVED");

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewVerification_withoutAuth_returns401() throws Exception {
        var request = new ReviewVerificationRequest();
        request.setAction("APPROVED");

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateVerificationRequest validCreateRequest() {
        var request = new CreateVerificationRequest();
        request.setEntityType("TEACHER");
        var doc = new CreateVerificationRequest.DocInput();
        doc.setDocumentType("DNI");
        doc.setFileUrl("uploads/docs/dni.pdf");
        request.setDocuments(List.of(doc));
        return request;
    }
}
