package com.mentoredu.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;
import com.mentoredu.library.exception.DuplicateSolutionException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.exception.SolutionsNotAllowedException;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.service.IResourceSolutionService;
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

@WebMvcTest(controllers = ResourceSolutionController.class)
class ResourceSolutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IResourceSolutionService solutionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US39 — Submit solution to academic resource
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: recurso con allows_solutions=true, PDF válido → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_withValidRequest_returns201() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setFileId(fileId);

        when(solutionService.submitSolution(eq(resourceId), any(), eq("student@example.com")))
                .thenReturn(buildSolutionResponse(solutionId, resourceId, fileId));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solutionId").value(solutionId.toString()))
                .andExpect(jsonPath("$.resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario error: allows_solutions=false → 403 (RN-47)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_whenAllowsSolutionsIsFalse_returns403() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setFileId(fileId);

        when(solutionService.submitSolution(eq(resourceId), any(), eq("student@example.com")))
                .thenThrow(new SolutionsNotAllowedException("Este recurso no acepta resoluciones (RN-47)"));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Este recurso no acepta resoluciones (RN-47)"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: solución duplicada → 409 (RN-45)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_whenDuplicateSolution_returns409() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setFileId(fileId);

        when(solutionService.submitSolution(eq(resourceId), any(), eq("student@example.com")))
                .thenThrow(new DuplicateSolutionException("Ya enviaste una resolución para este recurso (RN-45)"));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya enviaste una resolución para este recurso (RN-45)"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: recurso no existe → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_whenResourceNotFound_returns404() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setFileId(fileId);

        when(solutionService.submitSolution(eq(resourceId), any(), eq("student@example.com")))
                .thenThrow(new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario error: no autenticado → 401
    // -------------------------------------------------------------------------

    @Test
    void submitSolution_withoutAuthentication_returns401() throws Exception {
        UUID resourceId = UUID.randomUUID();

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setFileId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private SolutionResponse buildSolutionResponse(UUID solutionId, UUID resourceId, UUID fileId) {
        ResourceSolution solution = new ResourceSolution();
        solution.setId(solutionId);
        solution.setResourceId(resourceId);
        solution.setStudentId(UUID.randomUUID());
        solution.setFileId(fileId);
        solution.setStatus("SUBMITTED");
        solution.setSubmittedAt(LocalDateTime.now());
        return new SolutionResponse(solution);
    }
}
