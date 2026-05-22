package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US27 — Ver mis notificaciones
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyNotifications_authenticated_returns200WithList() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);
        Notification notif = buildNotification(user, "new_follower", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].type").value("new_follower"))
                .andExpect(jsonPath("$[0].readAt").isEmpty());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyNotifications_emptyList_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyNotifications_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US27 — Ver notificaciones pendientes
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getPendingNotifications_authenticated_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);
        Notification notif1 = buildNotification(user, "answer_received", null);
        Notification notif2 = buildNotification(user, "reaction_received", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notif1, notif2));

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("answer_received"))
                .andExpect(jsonPath("$[1].type").value("reaction_received"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getPendingNotifications_noPending_returns200EmptyList() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getPendingNotifications_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US27 — Marcar notificación como leída
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_whenExists_returns204() throws Exception {
        UUID notifId = UUID.randomUUID();
        User user = userWithId(UUID.randomUUID());
        Notification notif = buildNotification(user, "new_follower", null);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_whenNotFound_returns404() throws Exception {
        UUID notifId = UUID.randomUUID();

        when(notificationRepository.findById(notifId))
                .thenThrow(new NotificationNotFoundException("Notificación no encontrada: " + notifId));

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Notificación no encontrada: " + notifId));
    }

    @Test
    void markAsRead_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User userWithId(UUID id) {
        return User.builder().id(id).email("user@example.com").build();
    }

    private Notification buildNotification(User user, String type, LocalDateTime readAt) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .user(user)
                .type(type)
                .payload(Map.of("key", "value"))
                .readAt(readAt)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
