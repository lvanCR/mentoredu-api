package com.mentoredu.forum.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.dto.FollowResponse;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.forum.service.IFollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FollowController.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IFollowService followService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US29 — toggleFollow: POST /api/v1/users/{targetUserId}/follow
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_newFollow_returns201WithBody() throws Exception {
        UUID targetId = UUID.randomUUID();
        FollowResponse response = buildFollowResponse(targetId, "Ana", "García", "ana@example.com");

        when(followService.toggleFollow(eq(targetId), eq("user@example.com")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/users/{targetUserId}/follow", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(targetId.toString()))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("García"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_unfollowExisting_returns204() throws Exception {
        UUID targetId = UUID.randomUUID();

        when(followService.toggleFollow(eq(targetId), eq("user@example.com")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/users/{targetUserId}/follow", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_selfFollow_returns400() throws Exception {
        UUID targetId = UUID.randomUUID();

        when(followService.toggleFollow(eq(targetId), eq("user@example.com")))
                .thenThrow(new IllegalArgumentException("No puedes seguirte a ti mismo"));

        mockMvc.perform(post("/api/v1/users/{targetUserId}/follow", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("No puedes seguirte a ti mismo"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_targetUserNotFound_returns404() throws Exception {
        UUID targetId = UUID.randomUUID();

        when(followService.toggleFollow(eq(targetId), any()))
                .thenThrow(new UserNotFoundException("User not found: " + targetId));

        mockMvc.perform(post("/api/v1/users/{targetUserId}/follow", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + targetId));
    }

    @Test
    void toggleFollow_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/{targetUserId}/follow", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US29 — getFollowing: GET /api/v1/users/{userId}/following
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowing_existingUser_returns200WithList() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        when(followService.getFollowing(eq(userId)))
                .thenReturn(List.of(
                        buildFollowResponse(user1Id, "Pedro", "Martínez", "pedro@example.com"),
                        buildFollowResponse(user2Id, "Laura", "Sánchez", "laura@example.com")
                ));

        mockMvc.perform(get("/api/v1/users/{userId}/following", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Pedro"))
                .andExpect(jsonPath("$[1].firstName").value("Laura"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowing_noFollowing_returns200WithEmptyList() throws Exception {
        UUID userId = UUID.randomUUID();

        when(followService.getFollowing(eq(userId))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/{userId}/following", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowing_userNotFound_returns404() throws Exception {
        UUID userId = UUID.randomUUID();

        when(followService.getFollowing(eq(userId)))
                .thenThrow(new UserNotFoundException("User not found: " + userId));

        mockMvc.perform(get("/api/v1/users/{userId}/following", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + userId));
    }

    @Test
    void getFollowing_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/following", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US29 — getFollowers: GET /api/v1/users/{userId}/followers
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowers_existingUser_returns200WithList() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID follower1Id = UUID.randomUUID();
        UUID follower2Id = UUID.randomUUID();

        when(followService.getFollowers(eq(userId)))
                .thenReturn(List.of(
                        buildFollowResponse(follower1Id, "Carlos", "Ruiz", "carlos@example.com"),
                        buildFollowResponse(follower2Id, "Sofía", "Torres", "sofia@example.com")
                ));

        mockMvc.perform(get("/api/v1/users/{userId}/followers", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Carlos"))
                .andExpect(jsonPath("$[1].firstName").value("Sofía"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowers_noFollowers_returns200WithEmptyList() throws Exception {
        UUID userId = UUID.randomUUID();

        when(followService.getFollowers(eq(userId))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/{userId}/followers", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getFollowers_userNotFound_returns404() throws Exception {
        UUID userId = UUID.randomUUID();

        when(followService.getFollowers(eq(userId)))
                .thenThrow(new UserNotFoundException("User not found: " + userId));

        mockMvc.perform(get("/api/v1/users/{userId}/followers", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + userId));
    }

    @Test
    void getFollowers_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/followers", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private FollowResponse buildFollowResponse(UUID id, String firstName, String lastName, String email) {
        return new FollowResponse(id, firstName, lastName, email);
    }
}
