package com.mentoredu.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.service.IReactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReactionController.class)
class ReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IReactionService reactionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US27 — React to thread
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_newReaction_returns201WithBody() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = validRequest("LIKE");
        var response = buildResponse("THREAD", threadId, "LIKE");

        when(reactionService.reactToThread(eq(threadId), any(), eq("user@example.com")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.targetType").value("THREAD"))
                .andExpect(jsonPath("$.targetId").value(threadId.toString()))
                .andExpect(jsonPath("$.reactionType").value("LIKE"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_sameReactionAgain_returns204Toggle() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = validRequest("LIKE");

        when(reactionService.reactToThread(eq(threadId), any(), eq("user@example.com")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_threadNotFound_returns404() throws Exception {
        UUID threadId = UUID.randomUUID();

        when(reactionService.reactToThread(eq(threadId), any(), any()))
                .thenThrow(new ThreadNotFoundException("Thread not found: " + threadId));

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Thread not found: " + threadId));
    }

    @Test
    void reactToThread_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/threads/{id}/reactions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_withBlankReactionType_returns400() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = validRequest("");

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.reactionType").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_withNullReactionType_returns400() throws Exception {
        UUID threadId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.reactionType").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToThread_differentReactionType_returns201WithUpdated() throws Exception {
        UUID threadId = UUID.randomUUID();
        var request = validRequest("HELPFUL");
        var response = buildResponse("THREAD", threadId, "HELPFUL");

        when(reactionService.reactToThread(eq(threadId), any(), eq("user@example.com")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/threads/{id}/reactions", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reactionType").value("HELPFUL"));
    }

    // =========================================================================
    // US27 — React to answer
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToAnswer_newReaction_returns201WithBody() throws Exception {
        UUID answerId = UUID.randomUUID();
        var request = validRequest("LIKE");
        var response = buildResponse("ANSWER", answerId, "LIKE");

        when(reactionService.reactToAnswer(eq(answerId), any(), eq("user@example.com")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/answers/{id}/reactions", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetType").value("ANSWER"))
                .andExpect(jsonPath("$.targetId").value(answerId.toString()))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToAnswer_sameReactionAgain_returns204Toggle() throws Exception {
        UUID answerId = UUID.randomUUID();

        when(reactionService.reactToAnswer(eq(answerId), any(), eq("user@example.com")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/answers/{id}/reactions", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToAnswer_answerNotFound_returns404() throws Exception {
        UUID answerId = UUID.randomUUID();

        when(reactionService.reactToAnswer(eq(answerId), any(), any()))
                .thenThrow(new AnswerNotFoundException("Answer not found: " + answerId));

        mockMvc.perform(post("/api/v1/answers/{id}/reactions", answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Answer not found: " + answerId));
    }

    @Test
    void reactToAnswer_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/answers/{id}/reactions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US27 — React to comment
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToComment_newReaction_returns201WithBody() throws Exception {
        UUID commentId = UUID.randomUUID();
        var request = validRequest("LIKE");
        var response = buildResponse("COMMENT", commentId, "LIKE");

        when(reactionService.reactToComment(eq(commentId), any(), eq("user@example.com")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/comments/{id}/reactions", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetType").value("COMMENT"))
                .andExpect(jsonPath("$.targetId").value(commentId.toString()))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToComment_sameReactionAgain_returns204Toggle() throws Exception {
        UUID commentId = UUID.randomUUID();

        when(reactionService.reactToComment(eq(commentId), any(), eq("user@example.com")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/comments/{id}/reactions", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void reactToComment_commentNotFound_returns404() throws Exception {
        UUID commentId = UUID.randomUUID();

        when(reactionService.reactToComment(eq(commentId), any(), any()))
                .thenThrow(new CommentNotFoundException("Comment not found: " + commentId));

        mockMvc.perform(post("/api/v1/comments/{id}/reactions", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Comment not found: " + commentId));
    }

    @Test
    void reactToComment_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/comments/{id}/reactions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("LIKE"))))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateReactionRequest validRequest(String reactionType) {
        var r = new CreateReactionRequest();
        r.setReactionType(reactionType);
        return r;
    }

    private ReactionResponse buildResponse(String targetType, UUID targetId, String reactionType) {
        return ReactionResponse.builder()
                .id(UUID.randomUUID())
                .targetType(targetType)
                .targetId(targetId)
                .reactionType(reactionType)
                .userId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
