package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.model.TeacherAcademyLink;
import com.mentoredu.community.service.IAssociationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssociationController.class)
class AssociationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IAssociationService associationService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US24 — Docente solicita asociarse
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_asTeacher_returns201() throws Exception {
        UUID academyProfileId = UUID.randomUUID();
        AssociationResponse response = new AssociationResponse(buildLink(UUID.randomUUID(), academyProfileId));

        when(associationService.requestAssociation(any(), eq("teacher@example.com"))).thenReturn(response);

        var request = new CreateAssociationRequest(academyProfileId);

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_whenDuplicate_returns409() throws Exception {
        when(associationService.requestAssociation(any(), eq("teacher@example.com")))
                .thenThrow(new DuplicateAssociationException("Ya existe una solicitud de asociación con esta academia"));

        var request = new CreateAssociationRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_asNonTeacher_returns403() throws Exception {
        when(associationService.requestAssociation(any(), eq("teacher@example.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo docentes pueden solicitar asociaciones"));

        var request = new CreateAssociationRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestAssociation_withoutAuth_returns401() throws Exception {
        var request = new CreateAssociationRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US24 — Ver mis asociaciones (docente)
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getMyAssociations_returns200() throws Exception {
        UUID teacherProfileId = UUID.randomUUID();
        AssociationResponse response = new AssociationResponse(buildLink(teacherProfileId, UUID.randomUUID()));

        when(associationService.getMyAssociations("teacher@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/associations/teacher-academy/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyAssociations_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/associations/teacher-academy/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US24 — Ver solicitudes recibidas (academia)
    // =========================================================================

    @Test
    @WithMockUser(username = "academy@example.com")
    void getAcademyRequests_returns200() throws Exception {
        AssociationResponse response = new AssociationResponse(buildLink(UUID.randomUUID(), UUID.randomUUID()));

        when(associationService.getAcademyRequests("academy@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/associations/teacher-academy/academy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void getAcademyRequests_asNonAcademy_returns403() throws Exception {
        when(associationService.getAcademyRequests("student@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo academias pueden ver solicitudes recibidas"));

        mockMvc.perform(get("/api/v1/associations/teacher-academy/academy"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // US24 — Academia acepta solicitud
    // =========================================================================

    @Test
    @WithMockUser(username = "academy@example.com")
    void acceptAssociation_returns200WithAccepted() throws Exception {
        UUID id = UUID.randomUUID();
        TeacherAcademyLink accepted = TeacherAcademyLink.builder()
                .id(id)
                .teacherProfileId(UUID.randomUUID())
                .academyProfileId(UUID.randomUUID())
                .status("ACCEPTED")
                .requestedAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();

        when(associationService.acceptAssociation(eq(id), eq("academy@example.com")))
                .thenReturn(new AssociationResponse(accepted));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void acceptAssociation_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(associationService.acceptAssociation(eq(id), eq("academy@example.com")))
                .thenThrow(new AssociationNotFoundException("Solicitud no encontrada: " + id));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void acceptAssociation_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US24 — Academia rechaza solicitud
    // =========================================================================

    @Test
    @WithMockUser(username = "academy@example.com")
    void rejectAssociation_returns200WithRejected() throws Exception {
        UUID id = UUID.randomUUID();
        TeacherAcademyLink rejected = TeacherAcademyLink.builder()
                .id(id)
                .teacherProfileId(UUID.randomUUID())
                .academyProfileId(UUID.randomUUID())
                .status("REJECTED")
                .requestedAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();

        when(associationService.rejectAssociation(eq(id), eq("academy@example.com")))
                .thenReturn(new AssociationResponse(rejected));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/reject", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void rejectAssociation_whenAlreadyResolved_returns409() throws Exception {
        UUID id = UUID.randomUUID();

        when(associationService.rejectAssociation(eq(id), eq("academy@example.com")))
                .thenThrow(new DuplicateAssociationException("La solicitud ya fue resuelta con estado: ACCEPTED"));

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/reject", id))
                .andExpect(status().isConflict());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private TeacherAcademyLink buildLink(UUID teacherProfileId, UUID academyProfileId) {
        return TeacherAcademyLink.builder()
                .id(UUID.randomUUID())
                .teacherProfileId(teacherProfileId)
                .academyProfileId(academyProfileId)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
