package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.community.model.Follow;
import com.mentoredu.community.repository.FollowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowRepository followRepository;

    @MockitoBean
    private UserRepository userRepository;

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
        User follower = userWithId(UUID.randomUUID());
        User followed = userWithId(targetId);

        when(userRepository.findByEmail("follower@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(followed));
        when(followRepository.findByFollowerIdAndFollowedId(follower.getId(), targetId)).thenReturn(Optional.empty());
        when(followRepository.save(any(Follow.class))).thenReturn(new Follow());

        mockMvc.perform(post("/api/v1/users/{userId}/follow", targetId))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void toggleFollow_alreadyFollowing_returns204() throws Exception {
        UUID targetId = UUID.randomUUID();
        User follower = userWithId(UUID.randomUUID());
        User followed = userWithId(targetId);
        Follow existing = Follow.builder().follower(follower).followed(followed).build();

        when(userRepository.findByEmail("follower@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(followed));
        when(followRepository.findByFollowerIdAndFollowedId(follower.getId(), targetId)).thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/v1/users/{userId}/follow", targetId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_selfFollow_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(followRepository.findByFollowerIdAndFollowedId(any(), any()))
                .thenThrow(new SelfFollowException("No puedes seguirte a ti mismo"));

        mockMvc.perform(post("/api/v1/users/{userId}/follow", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void toggleFollow_targetUserNotFound_returns400() throws Exception {
        UUID unknownId = UUID.randomUUID();
        User follower = userWithId(UUID.randomUUID());

        when(userRepository.findByEmail("follower@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/users/{userId}/follow", unknownId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleFollow_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/follow", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User userWithId(UUID id) {
        return User.builder().id(id).email("user@example.com").build();
    }
}
