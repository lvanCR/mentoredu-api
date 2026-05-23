package com.mentoredu.pedagogy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.exception.DuplicateSolutionException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.service.ISolutionService;
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

@WebMvcTest(SolutionController.class)
class SolutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ISolutionService solutionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US18 — Enviar resolución a un ejercicio
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_withFileUrl_returns201() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var request = new SubmitSolutionRequest("https://cloudinary.com/sol.pdf", null);
        var response = buildSolutionResponse(resourceId, "https://cloudinary.com/sol.pdf", null);

        when(solutionService.submit(eq(resourceId), any(), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.fileUrl").value("https://cloudinary.com/sol.pdf"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_withContent_returns201() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var request = new SubmitSolutionRequest(null, "Mi resolución detallada del ejercicio.");
        var response = buildSolutionResponse(resourceId, null, "Mi resolución detallada del ejercicio.");

        when(solutionService.submit(eq(resourceId), any(), eq("student@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Mi resolución detallada del ejercicio."))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_duplicate_returns409() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var request = new SubmitSolutionRequest("https://cloudinary.com/sol.pdf", null);

        when(solutionService.submit(eq(resourceId), any(), eq("student@example.com")))
                .thenThrow(new DuplicateSolutionException("Ya enviaste una resolución para este ejercicio"));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya enviaste una resolución para este ejercicio"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void submitSolution_resourceNotFound_returns404() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var request = new SubmitSolutionRequest(null, "Contenido de resolución");

        when(solutionService.submit(eq(resourceId), any(), eq("student@example.com")))
                .thenThrow(new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void submitSolution_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/resources/{resourceId}/solutions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitSolutionRequest(null, "contenido"))))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US20 — Ver mi resolución
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void getMySolution_withFeedback_returns200() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var sol = buildSolutionResponse(resourceId, null, "Mi resolución.");
        var wrapped = new MySolutionWithFeedbackResponse(sol, null);

        when(solutionService.getMyWithFeedback(eq(resourceId), eq("student@example.com"))).thenReturn(wrapped);

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solution.resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.solution.content").value("Mi resolución."))
                .andExpect(jsonPath("$.solution.status").value("PENDING"))
                .andExpect(jsonPath("$.solution.submittedAt").exists())
                .andExpect(jsonPath("$.feedback").doesNotExist());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void getMySolution_whenNotFound_returns404() throws Exception {
        UUID resourceId = UUID.randomUUID();

        when(solutionService.getMyWithFeedback(eq(resourceId), eq("student@example.com")))
                .thenThrow(new SolutionNotFoundException("No tienes resolución para este ejercicio"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No tienes resolución para este ejercicio"));
    }

    @Test
    void getMySolution_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US17 — Ver resoluciones de un ejercicio (autor/docente-academia)
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void listSolutions_asAuthor_returns200() throws Exception {
        UUID resourceId = UUID.randomUUID();
        var sol1 = buildSolutionResponse(resourceId, null, "Resolución A");
        var sol2 = buildSolutionResponse(resourceId, "https://file.com/b.pdf", null);

        when(solutionService.listByResource(eq(resourceId), eq("teacher@example.com")))
                .thenReturn(List.of(sol1, sol2));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$[1].fileUrl").value("https://file.com/b.pdf"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void listSolutions_notAuthorized_returns403() throws Exception {
        UUID resourceId = UUID.randomUUID();

        when(solutionService.listByResource(eq(resourceId), eq("student@example.com")))
                .thenThrow(new SolutionAccessDeniedException("Solo el autor del ejercicio puede ver las resoluciones"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions", resourceId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Solo el autor del ejercicio puede ver las resoluciones"));
    }

    @Test
    void listSolutions_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private SolutionResponse buildSolutionResponse(UUID resourceId, String fileUrl, String content) {
        return SolutionResponse.builder()
                .id(UUID.randomUUID())
                .resourceId(resourceId)
                .studentId(UUID.randomUUID())
                .fileUrl(fileUrl)
                .content(content)
                .status("PENDING")
                .submittedAt(LocalDateTime.now())
                .build();
    }
}
