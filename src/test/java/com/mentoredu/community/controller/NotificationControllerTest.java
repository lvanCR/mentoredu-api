package com.mentoredu.community.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.service.INotificationService;
import com.mentoredu.config.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para NotificationController.
 * Cubre: US27 — Ver todas, ver pendientes y marcar como leída.
 * Tipos de notificación: new_follower, answer_received, comment_received, reaction_received,
 *   solution_submitted, feedback_received, verification_processed, association_resolved.
 * Validado en producción (CONFIRMACION_TESTS.md HU-27):
 *   27.1 GET /me → 200; 27.2 GET /me/pending → 200; 27.3 PATCH /{id}/read → 204;
 *   27.4 sin notificaciones → lista vacía; 27.5 sin auth → 401.
 */
@WebMvcTest(
    controllers = NotificationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private INotificationService notificationService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── GET /notifications/me (US27) ───────────────────────────────────────────

    /** US27 Escenario 27.1 — ver todas las notificaciones (leídas + no leídas) → 200 */
    @Test
    @WithMockUser(username = "carlos@example.com")
    void getMyNotifications_authenticated_returns200WithPagedList() throws Exception {
        var notification = NotificationResponse.builder()
            .id(UUID.randomUUID()).type("answer_received")
            .payload(Map.of("threadId", UUID.randomUUID().toString()))
            .createdAt(LocalDateTime.now()).build();
        var paged = new PagedResponse<>(List.of(notification), 0, 20, 1L, 1, true);
        when(notificationService.getMyNotifications(eq("carlos@example.com"), eq(0), eq(20), any()))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/notifications/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].type").value("answer_received"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    /** US27 — filtrar por tipo de notificación → 200 */
    @Test
    @WithMockUser(username = "carlos@example.com")
    void getMyNotifications_withTypeFilter_returns200() throws Exception {
        var paged = new PagedResponse<NotificationResponse>(List.of(), 0, 20, 0L, 0, true);
        when(notificationService.getMyNotifications(any(), eq(0), eq(20), eq("new_follower")))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/notifications/me").param("type", "new_follower"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /** US27 Escenario 27.4 — sin notificaciones → lista vacía con totalElements=0 */
    @Test
    @WithMockUser(username = "newuser@example.com")
    void getMyNotifications_noNotifications_returns200WithEmpty() throws Exception {
        var paged = new PagedResponse<NotificationResponse>(List.of(), 0, 20, 0L, 0, true);
        when(notificationService.getMyNotifications(any(), anyInt(), anyInt(), any()))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/notifications/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    /** US27 Escenario 27.5 — sin autenticación → 401 */
    @Test
    void getMyNotifications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /notifications/me/pending (US27) ──────────────────────────────────

    /** US27 Escenario 27.2 — ver notificaciones no leídas (readAt=null) → 200 */
    @Test
    @WithMockUser(username = "carlos@example.com")
    void getPendingNotifications_authenticated_returns200WithUnread() throws Exception {
        var unread = NotificationResponse.builder()
            .id(UUID.randomUUID()).type("solution_submitted")
            .payload(Map.of("solutionId", UUID.randomUUID().toString()))
            .readAt(null) // no leída
            .createdAt(LocalDateTime.now()).build();
        var paged = new PagedResponse<>(List.of(unread), 0, 20, 1L, 1, true);
        when(notificationService.getPendingNotifications(eq("carlos@example.com"), eq(0), eq(20), any()))
            .thenReturn(paged);

        mockMvc.perform(get("/api/v1/notifications/me/pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].type").value("solution_submitted"))
            .andExpect(jsonPath("$.content[0].readAt").doesNotExist());
    }

    /** GET /notifications/me/pending — sin autenticación → 401 */
    @Test
    void getPendingNotifications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me/pending"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /notifications/{id}/read (US27) ─────────────────────────────────

    /** US27 Escenario 27.3 — marcar notificación como leída → 204 No Content
     * Confirmado en HU-27: retorna 204 (no 200). */
    @Test
    @WithMockUser(username = "carlos@example.com")
    void markAsRead_existingNotification_returns204() throws Exception {
        var notifId = UUID.randomUUID();
        doNothing().when(notificationService).markAsRead(eq(notifId), eq("carlos@example.com"));

        mockMvc.perform(patch("/api/v1/notifications/" + notifId + "/read"))
            .andExpect(status().isNoContent());
    }

    /** US27 — notificación inexistente o de otro usuario → 404 */
    @Test
    @WithMockUser(username = "carlos@example.com")
    void markAsRead_notFound_returns404() throws Exception {
        var notifId = UUID.randomUUID();
        doThrow(new NotificationNotFoundException("Notificación no encontrada"))
            .when(notificationService).markAsRead(eq(notifId), any());

        mockMvc.perform(patch("/api/v1/notifications/" + notifId + "/read"))
            .andExpect(status().isNotFound());
    }

    /** PATCH /notifications/{id}/read — sin autenticación → 401 */
    @Test
    void markAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/" + UUID.randomUUID() + "/read"))
            .andExpect(status().isUnauthorized());
    }
}
