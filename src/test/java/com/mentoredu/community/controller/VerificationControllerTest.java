package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.model.VerificationDoc;
import com.mentoredu.community.model.VerificationRequest;
import com.mentoredu.auth.entity.User;
import com.mentoredu.community.repository.VerificationDocRepository;
import com.mentoredu.community.repository.VerificationRequestRepository;
import com.mentoredu.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VerificationController.class)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private VerificationRequestRepository verificationRepository;
    @MockitoBean private VerificationDocRepository     docRepository;
    @MockitoBean private UserRepository                userRepository;
    @MockitoBean private JwtUtil                       jwtUtil;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US22 — Solicitar verificación
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void submitVerification_withDocs_returns201() throws Exception {
        User user = buildUser("teacher@example.com");
        VerificationRequest saved = buildVerificationRequest(user);
        List<VerificationDoc> docs = List.of(buildDoc(saved));
        saved.setDocs(docs);

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(verificationRepository.existsByUserIdAndStatus(user.getId(), "PENDING")).thenReturn(false);
        when(verificationRepository.save(any())).thenReturn(saved);
        when(docRepository.saveAll(any())).thenReturn(docs);
        when(verificationRepository.findById(saved.getId())).thenReturn(Optional.of(saved));

        var request = validCreateRequest();

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
        User user = buildUser("teacher@example.com");

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(verificationRepository.existsByUserIdAndStatus(user.getId(), "PENDING")).thenReturn(true);

        mockMvc.perform(post("/api/v1/verification/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
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
        User user = buildUser("teacher@example.com");
        VerificationRequest vr = buildVerificationRequest(user);
        vr.setDocs(List.of());

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(verificationRepository.findByUserId(user.getId())).thenReturn(List.of(vr));

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
        User mod = buildUser("mod@example.com");
        User user = buildUser("teacher@example.com");
        VerificationRequest vr = buildVerificationRequest(user);
        vr.setDocs(List.of());
        vr.setStatus("APPROVED");

        when(userRepository.findByEmail("mod@example.com")).thenReturn(Optional.of(mod));
        when(verificationRepository.findById(vr.getId())).thenReturn(Optional.of(buildVerificationRequest(user)));
        when(verificationRepository.save(any())).thenReturn(vr);

        var request = new ReviewVerificationRequest();
        request.setAction("APPROVED");

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", vr.getId())
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

        User user = buildUser("teacher@example.com");
        VerificationRequest vr = buildVerificationRequest(user);
        when(verificationRepository.findById(id)).thenReturn(Optional.of(vr));

        mockMvc.perform(patch("/api/v1/verification/requests/{id}/review", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "mod@example.com", roles = {"MODERATOR"})
    void reviewVerification_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(verificationRepository.findById(id)).thenReturn(Optional.empty());

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

    private User buildUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    private VerificationRequest buildVerificationRequest(User user) {
        return VerificationRequest.builder()
                .id(UUID.randomUUID())
                .user(user)
                .entityType("TEACHER")
                .status("PENDING")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    private VerificationDoc buildDoc(VerificationRequest request) {
        return VerificationDoc.builder()
                .id(UUID.randomUUID())
                .request(request)
                .documentType("DNI")
                .fileUrl("uploads/docs/dni.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
