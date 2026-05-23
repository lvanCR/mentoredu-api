package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.service.ICommentService;
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

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ICommentService commentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US28 — Comment on forum answer — POST /api/v1/answers/{answerId}/comments
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void createComment_withValidBody_returns201() throws Exception {
        UUID answerId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        var request = validCommentRequest();
        var response = buildCommentResponse(answerId, threadId, "Exactamente, ese es el punto clave.", "Juan Pérez");

        when(commentService.create(eq(answerId), any(), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.answerId").value(answerId.toString()))
                .andExpect(jsonPath("$.threadId").value(threadId.toString()))
                .andExpect(jsonPath("$.body").value("Exactamente, ese es el punto clave."))
                .andExpect(jsonPath("$.authorDisplay").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createComment_withDetailedContent_returns201() throws Exception {
        UUID answerId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        var request = new CreateCommentRequest("Complementando la respuesta: recuerda que el Jacobiano en coordenadas polares es r.");
        var response = buildCommentResponse(answerId, threadId,
                "Complementando la respuesta: recuerda que el Jacobiano en coordenadas polares es r.",
                "María López");

        when(commentService.create(eq(answerId), any(), eq("teacher@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body")
                        .value("Complementando la respuesta: recuerda que el Jacobiano en coordenadas polares es r."))
                .andExpect(jsonPath("$.authorDisplay").value("María López"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createComment_withEmptyBody_returns400() throws Exception {
        UUID answerId = UUID.randomUUID();
        var request = new CreateCommentRequest("");

        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createComment_withNullBody_returns400() throws Exception {
        UUID answerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.body").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createComment_whenAnswerNotFound_returns404() throws Exception {
        UUID answerId = UUID.randomUUID();

        when(commentService.create(eq(answerId), any(), any()))
                .thenThrow(new AnswerNotFoundException("Answer not found: " + answerId));

        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Answer not found: " + answerId));
    }

    @Test
    void createComment_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/answers/{answerId}/comments", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentRequest())))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US28 — List comments by answer — GET /api/v1/answers/{answerId}/comments
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void listComments_whenAnswerExists_returns200() throws Exception {
        UUID answerId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        when(commentService.listByAnswer(eq(answerId)))
                .thenReturn(List.of(
                        buildCommentResponse(answerId, threadId, "Muy buena aclaración.", "Carlos R."),
                        buildCommentResponse(answerId, threadId, "Gracias, me ayudó mucho.", "Ana G.")
                ));

        mockMvc.perform(get("/api/v1/answers/{answerId}/comments", answerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].answerId").value(answerId.toString()))
                .andExpect(jsonPath("$[0].body").value("Muy buena aclaración."))
                .andExpect(jsonPath("$[1].body").value("Gracias, me ayudó mucho."));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listComments_whenNoComments_returns200WithEmptyList() throws Exception {
        UUID answerId = UUID.randomUUID();

        when(commentService.listByAnswer(eq(answerId))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/answers/{answerId}/comments", answerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void listComments_whenAnswerNotFound_returns404() throws Exception {
        UUID answerId = UUID.randomUUID();

        when(commentService.listByAnswer(eq(answerId)))
                .thenThrow(new AnswerNotFoundException("Answer not found: " + answerId));

        mockMvc.perform(get("/api/v1/answers/{answerId}/comments", answerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Answer not found: " + answerId));
    }

    @Test
    void listComments_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/answers/{answerId}/comments", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateCommentRequest validCommentRequest() {
        return new CreateCommentRequest("Exactamente, ese es el punto clave.");
    }

    private CommentResponse buildCommentResponse(UUID answerId, UUID threadId, String body, String author) {
        return CommentResponse.builder()
                .id(UUID.randomUUID())
                .answerId(answerId)
                .threadId(threadId)
                .body(body)
                .authorDisplay(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
