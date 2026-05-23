package com.mentoredu.community.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.community.service.IFollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
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
    // US21 — Seguir a un usuario
    // =========================================================================

    @Test
    @WithMockUser(username = "follower@example.com")
    void toggleFollow_firstTime_returns201() throws Exception {
        UUID targetId = UUID.randomUUID();
        when(followService.toggleFollow(eq(targetId), eq("follower@example.com"))).thenReturn(true);

        mockMvc.perform(post("/api/v1/users/{userId}/follow", targetId))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void toggleFollow_alreadyFollowing_returns204() throws Exception {
        UUID targetId = UUID.randomUUID();
        when(followService.toggleFollow(eq(targetId), eq("follower@example.com"))).thenReturn(false);

        mockMvc.perform(post("/api/v1/users/{userId}/follow", targetId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_selfFollow_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        when(followService.toggleFollow(eq(userId), anyString()))
                .thenThrow(new SelfFollowException("No puedes seguirte a ti mismo"));

        mockMvc.perform(post("/api/v1/users/{userId}/follow", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void toggleFollow_targetUserNotFound_returns400() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(followService.toggleFollow(eq(unknownId), anyString()))
                .thenThrow(new IllegalArgumentException("Usuario objetivo no encontrado"));

        mockMvc.perform(post("/api/v1/users/{userId}/follow", unknownId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleFollow_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/follow", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
