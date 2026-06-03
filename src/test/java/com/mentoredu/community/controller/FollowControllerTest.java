package com.mentoredu.community.controller;

import com.mentoredu.auth.exception.UserNotFoundException;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.community.service.IFollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para FollowController.
 * Cubre: US21 — Seguir/dejar de seguir a un usuario (toggle).
 * Regla RN-21: usuario no puede seguirse a sí mismo.
 * Validado en producción (CONFIRMACION_TESTS.md HU-21):
 *   21.1 seguir → 201; 21.2 toggle off → 204; 21.3 seguirse a sí mismo → 400;
 *   21.4 usuario inexistente → 404; 21.5 sin auth → 401.
 */
@WebMvcTest(
    controllers = FollowController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class FollowControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IFollowService followService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /users/{userId}/follow (US21) ────────────────────────────────────

    /** US21 Escenario 21.1 — seguir a otro usuario por primera vez → 201 No Content */
    @Test
    @WithMockUser(username = "maria@example.com")
    void toggleFollow_newFollow_returns201() throws Exception {
        var userId = UUID.randomUUID();
        when(followService.toggleFollow(eq(userId), eq("maria@example.com"))).thenReturn(true);

        mockMvc.perform(post("/api/v1/users/" + userId + "/follow"))
            .andExpect(status().isCreated());
    }

    /** US21 Escenario 21.2 — toggle off (ya seguido) → 204 No Content */
    @Test
    @WithMockUser(username = "maria@example.com")
    void toggleFollow_existingFollow_returns204() throws Exception {
        var userId = UUID.randomUUID();
        when(followService.toggleFollow(eq(userId), eq("maria@example.com"))).thenReturn(false);

        mockMvc.perform(post("/api/v1/users/" + userId + "/follow"))
            .andExpect(status().isNoContent());
    }

    /** US21 Escenario 21.3 / RN-21 — seguirse a sí mismo → 400 Bad Request */
    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_selfFollow_returns400() throws Exception {
        var userId = UUID.randomUUID();
        when(followService.toggleFollow(eq(userId), any()))
            .thenThrow(new SelfFollowException("No puedes seguirte a ti mismo (RN-21)"));

        mockMvc.perform(post("/api/v1/users/" + userId + "/follow"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /** US21 Escenario 21.4 — usuario inexistente → 404 Not Found */
    @Test
    @WithMockUser(username = "user@example.com")
    void toggleFollow_userNotFound_returns404() throws Exception {
        var userId = UUID.randomUUID();
        when(followService.toggleFollow(eq(userId), any()))
            .thenThrow(new UserNotFoundException("Usuario no encontrado: " + userId));

        mockMvc.perform(post("/api/v1/users/" + userId + "/follow"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** US21 Escenario 21.5 — sin autenticación → 401 */
    @Test
    void toggleFollow_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + UUID.randomUUID() + "/follow"))
            .andExpect(status().isUnauthorized());
    }
}
