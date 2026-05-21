package com.mentoredu.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.library.dto.SolutionDetailResponse;
import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;
import com.mentoredu.library.exception.DuplicateSolutionException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.exception.SolutionAccessDeniedException;
import com.mentoredu.library.exception.SolutionNotFoundException;
import com.mentoredu.library.exception.SolutionsNotAllowedException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceFile;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.service.IResourceSolutionService;
import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    // F2.2 — GET /api/v1/resources/{resourceId}/solutions
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getSolutions_asTeacherAuthor_returns200WithList() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(solutionService.getSolutionsForResource(eq(resourceId), eq("teacher@example.com")))
                .thenReturn(List.of(buildSolutionDetailResponse(solutionId, resourceId, fileId)));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].solutionId").value(solutionId.toString()))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getSolutions_whenNotResourceAuthor_returns403() throws Exception {
        UUID resourceId = UUID.randomUUID();

        when(solutionService.getSolutionsForResource(eq(resourceId), eq("teacher@example.com")))
                .thenThrow(new SolutionAccessDeniedException(
                        "Solo el autor del recurso puede ver sus resoluciones (RN-46)"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions", resourceId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void getSolutionById_whenNotFound_returns404() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        when(solutionService.getSolutionById(eq(resourceId), eq(solutionId), eq("teacher@example.com")))
                .thenThrow(new SolutionNotFoundException("Solución no encontrada: " + solutionId));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/{solutionId}", resourceId, solutionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // US41 — View my solution and received feedback
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: solución enviada + feedback ya registrado → 200 con feedback
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void getMySolution_withFeedback_returns200WithFeedback() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        MySolutionWithFeedbackResponse response = buildMySolutionResponse(resourceId, fileId, true);

        when(solutionService.getMySubmittedSolution(eq(resourceId), eq("student@example.com")))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solutionId").exists())
                .andExpect(jsonPath("$.resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.resourceTitle").value("Simulacro UNI 2024"))
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.feedback").exists())
                .andExpect(jsonPath("$.feedback.body").value("Buen desarrollo en los primeros pasos."))
                .andExpect(jsonPath("$.feedback.score").value(8.5))
                .andExpect(jsonPath("$.feedback.authorName").value("Juan Quispe"));
    }

    // -------------------------------------------------------------------------
    // Escenario exitoso: solución enviada pero aún sin feedback → 200 con feedback=null
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void getMySolution_withoutFeedback_returns200WithNullFeedback() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        MySolutionWithFeedbackResponse response = buildMySolutionResponse(resourceId, fileId, false);

        when(solutionService.getMySubmittedSolution(eq(resourceId), eq("student@example.com")))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.feedback").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // Escenario error: estudiante no envió ninguna resolución → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void getMySolution_whenNoSolutionExists_returns404() throws Exception {
        UUID resourceId = UUID.randomUUID();

        when(solutionService.getMySubmittedSolution(eq(resourceId), eq("student@example.com")))
                .thenThrow(new SolutionNotFoundException(
                        "No has enviado ninguna resolución para este recurso"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: no autenticado → 401
    // -------------------------------------------------------------------------

    @Test
    void getMySolution_withoutAuthentication_returns401() throws Exception {
        UUID resourceId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/resources/{resourceId}/solutions/mine", resourceId))
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

    private SolutionDetailResponse buildSolutionDetailResponse(UUID solutionId, UUID resourceId, UUID fileId) {
        ResourceSolution solution = new ResourceSolution();
        solution.setId(solutionId);
        solution.setResourceId(resourceId);
        solution.setStudentId(UUID.randomUUID());
        solution.setFileId(fileId);
        solution.setStatus("SUBMITTED");
        solution.setSubmittedAt(LocalDateTime.now());

        ResourceFile file = new ResourceFile();
        file.setId(fileId);
        file.setFileUrl("http://localhost/uploads/" + fileId + ".pdf");
        return new SolutionDetailResponse(solution, file);
    }

    private MySolutionWithFeedbackResponse buildMySolutionResponse(UUID resourceId, UUID fileId, boolean withFeedback) {
        ResourceSolution solution = new ResourceSolution();
        solution.setId(UUID.randomUUID());
        solution.setResourceId(resourceId);
        solution.setStudentId(UUID.randomUUID());
        solution.setFileId(fileId);
        solution.setStatus(withFeedback ? "REVIEWED" : "SUBMITTED");
        solution.setSubmittedAt(LocalDateTime.now());

        AcademicResource resource = new AcademicResource();
        resource.setId(resourceId);
        resource.setTitle("Simulacro UNI 2024");

        ResourceFile file = new ResourceFile();
        file.setId(fileId);
        file.setFileUrl("http://localhost/uploads/" + fileId + ".pdf");

        FeedbackEntry feedbackEntry = null;
        if (withFeedback) {
            User author = new User();
            author.setId(UUID.randomUUID());
            author.setFirstName("Juan");
            author.setLastName("Quispe");

            feedbackEntry = new FeedbackEntry();
            feedbackEntry.setId(UUID.randomUUID());
            feedbackEntry.setAuthor(author);
            feedbackEntry.setBody("Buen desarrollo en los primeros pasos.");
            feedbackEntry.setScore(new BigDecimal("8.5"));
            feedbackEntry.setCreatedAt(LocalDateTime.now());
        }

        return new MySolutionWithFeedbackResponse(solution, resource, file, feedbackEntry);
    }
}
