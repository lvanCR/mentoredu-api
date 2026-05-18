package com.mentoredu.notifications.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.notifications.dto.NotificationResponse;
import com.mentoredu.notifications.exception.NotificationNotFoundException;
import com.mentoredu.notifications.model.Notification;
import com.mentoredu.notifications.service.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
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
    // US25 — GET /api/v1/notifications/me/pending
    // =========================================================================

    // Gherkin: Exitoso — tiene notificaciones pendientes → 200 con lista no vacía ordenada por fecha
    @Test
    @WithMockUser(username = "user@example.com")
    void getPending_withNotifications_returns200WithList() throws Exception {
        UUID userId = UUID.randomUUID();
        List<NotificationResponse> responses = List.of(
                buildResponse(userId, "FORUM_REPLY", "Alguien respondió tu hilo"),
                buildResponse(userId, "FOLLOW", "Tienes un nuevo seguidor")
        );

        when(notificationService.getPendingNotifications("user@example.com")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("FORUM_REPLY"))
                .andExpect(jsonPath("$[0].title").value("Alguien respondió tu hilo"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].type").value("FOLLOW"));
    }

    // Gherkin: Error — no autenticado → 401 Unauthorized
    @Test
    void getPending_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isUnauthorized());
    }

    // Gherkin: Alternativo exitoso — sin notificaciones pendientes → 200 lista vacía sin error
    @Test
    @WithMockUser(username = "user@example.com")
    void getPending_withNoNotifications_returns200WithEmptyList() throws Exception {
        when(notificationService.getPendingNotifications("user@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Gherkin: Alternativo error — servicio no disponible → 500 Internal Server Error
    @Test
    @WithMockUser(username = "user@example.com")
    void getPending_serviceUnavailable_returns500() throws Exception {
        when(notificationService.getPendingNotifications("user@example.com"))
                .thenThrow(new IllegalStateException("Database connection failed"));

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isInternalServerError());
    }

    // Extra: usuario no registrado en el sistema → 404 Not Found
    @Test
    @WithMockUser(username = "ghost@example.com")
    void getPending_userNotFound_returns404() throws Exception {
        when(notificationService.getPendingNotifications("ghost@example.com"))
                .thenThrow(new UserNotFoundException("Usuario no encontrado: ghost@example.com"));

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado: ghost@example.com"));
    }

    // =========================================================================
    // PATCH /api/v1/notifications/{id}/read — Mark as read
    // =========================================================================

    // Exitoso — notificación existente y propia → 200 con readAt establecido
    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_ownNotification_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationResponse response = buildReadResponse(userId, notifId, "FORUM_REPLY", "Tu hilo recibió una respuesta");

        when(notificationService.markAsRead(eq(notifId), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notifId.toString()))
                .andExpect(jsonPath("$.read").value(true));
    }

    // Idempotente — ya estaba leída → 200 sin error
    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_alreadyRead_returns200Idempotent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        NotificationResponse response = buildReadResponse(userId, notifId, "FOLLOW", "Nuevo seguidor");

        when(notificationService.markAsRead(eq(notifId), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    // Notificación no existe o no pertenece al usuario → 404 Not Found
    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_notificationNotFound_returns404() throws Exception {
        UUID notifId = UUID.randomUUID();

        when(notificationService.markAsRead(any(UUID.class), eq("user@example.com")))
                .thenThrow(new NotificationNotFoundException("Notificación no encontrada: " + notifId));

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Notificación no encontrada: " + notifId));
    }

    // Sin autenticación → 401 Unauthorized
    @Test
    void markAsRead_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private NotificationResponse buildResponse(UUID userId, String type, String title) {
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .build();

        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .user(user)
                .type(type)
                .title(title)
                .message("Mensaje de notificación de prueba")
                .readAt(null)
                .createdAt(LocalDateTime.now())
                .build();

        return new NotificationResponse(notification);
    }

    private NotificationResponse buildReadResponse(UUID userId, UUID notifId, String type, String title) {
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .build();

        Notification notification = Notification.builder()
                .id(notifId)
                .user(user)
                .type(type)
                .title(title)
                .message("Mensaje de notificación de prueba")
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        return new NotificationResponse(notification);
    }
}
