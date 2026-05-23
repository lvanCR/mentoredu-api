package com.mentoredu.community.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.service.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private INotificationService notificationService;

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
        var response = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .type("new_follower")
                .payload(Map.of("key", "value"))
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.getMyNotifications("user@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].type").value("new_follower"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyNotifications_emptyList_returns200() throws Exception {
        when(notificationService.getMyNotifications("user@example.com")).thenReturn(List.of());

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
        var n1 = NotificationResponse.builder().type("answer_received").build();
        var n2 = NotificationResponse.builder().type("reaction_received").build();

        when(notificationService.getPendingNotifications("user@example.com")).thenReturn(List.of(n1, n2));

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
        when(notificationService.getPendingNotifications("user@example.com")).thenReturn(List.of());

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

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_whenNotFound_returns404() throws Exception {
        UUID notifId = UUID.randomUUID();

        doThrow(new NotificationNotFoundException("Notificación no encontrada: " + notifId))
                .when(notificationService).markAsRead(eq(notifId), anyString());

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notificación no encontrada: " + notifId));
    }

    @Test
    void markAsRead_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
