package com.mentoredu.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.LevelProgressResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.service.IGamificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GamificationController.class)
class GamificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IGamificationService gamificationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US30 — GET /api/v1/gamification/users/{userId}/points
    // =========================================================================

    // Gherkin: Exitoso — accion recompensada procesada → devuelve puntos acumulados
    @Test
    @WithMockUser(username = "user@example.com")
    void getPoints_withAccumulatedXP_returns200WithPoints() throws Exception {
        UUID userId = UUID.randomUUID();
        when(gamificationService.getPoints(userId)).thenReturn(new PointsResponse(userId, 15));

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/points", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.points").value(15));
    }

    // Gherkin: Alternativo exitoso — usuario nuevo sin acciones → devuelve 0 puntos sin error
    @Test
    @WithMockUser(username = "user@example.com")
    void getPoints_newUser_returns200WithZeroPoints() throws Exception {
        UUID userId = UUID.randomUUID();
        when(gamificationService.getPoints(userId)).thenReturn(new PointsResponse(userId, 0));

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/points", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(0));
    }

    // Gherkin: Error — no autenticado → 401 Unauthorized
    @Test
    void getPoints_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/users/{userId}/points", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // Gherkin: Alternativo error — nivel recalculado al acumular XP suficiente → delegado al servicio
    @Test
    @WithMockUser(username = "user@example.com")
    void getPoints_afterLevelUp_returns200WithUpdatedPoints() throws Exception {
        UUID userId = UUID.randomUUID();
        // 105 XP → nivel 2 (recalculo ocurre en el servicio al llamar awardPoints)
        when(gamificationService.getPoints(userId)).thenReturn(new PointsResponse(userId, 105));

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/points", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(105));
    }

    // =========================================================================
    // GET /api/v1/gamification/users/{userId}/coins
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void getCoins_withBalance_returns200WithCoins() throws Exception {
        UUID userId = UUID.randomUUID();
        when(gamificationService.getCoins(userId)).thenReturn(new CoinsResponse(userId, 50));

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/coins", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.coins").value(50));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getCoins_noWallet_returns200WithZero() throws Exception {
        UUID userId = UUID.randomUUID();
        when(gamificationService.getCoins(userId)).thenReturn(new CoinsResponse(userId, 0));

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/coins", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(0));
    }

    @Test
    void getCoins_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/users/{userId}/coins", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // POST /api/v1/gamification/users/{userId}/coins/redeem
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void redeemCoins_withSufficientBalance_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        CoinsRequest request = new CoinsRequest();
        request.setAmount(10);
        when(gamificationService.redeemCoins(eq(userId), any(CoinsRequest.class)))
                .thenReturn(new CoinsResponse(userId, 40));

        mockMvc.perform(post("/api/v1/gamification/users/{userId}/coins/redeem", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(40));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void redeemCoins_insufficientBalance_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        CoinsRequest request = new CoinsRequest();
        request.setAmount(999);
        when(gamificationService.redeemCoins(eq(userId), any(CoinsRequest.class)))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente de monedas"));

        mockMvc.perform(post("/api/v1/gamification/users/{userId}/coins/redeem", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Saldo insuficiente de monedas"));
    }

    @Test
    void redeemCoins_withoutAuth_returns401() throws Exception {
        CoinsRequest request = new CoinsRequest();
        request.setAmount(5);

        mockMvc.perform(post("/api/v1/gamification/users/{userId}/coins/redeem", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US31 — GET /api/v1/gamification/users/{userId}/level
    // =========================================================================

    // Gherkin: Exitoso — registro activo → 200 con nivel, experiencia y porcentaje
    @Test
    @WithMockUser(username = "user@example.com")
    void getLevelProgress_withActiveProgress_returns200WithLevelInfo() throws Exception {
        UUID userId = UUID.randomUUID();
        LevelProgressResponse response = new LevelProgressResponse(
                userId, 2, 105, new BigDecimal("5.00"), LocalDateTime.now()
        );
        when(gamificationService.getLevelProgress(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/level", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.currentLevel").value(2))
                .andExpect(jsonPath("$.experience").value(105))
                .andExpect(jsonPath("$.progressPercentage").value(5.00));
    }

    // Gherkin: Error — no autenticado → 401 Unauthorized
    @Test
    void getLevelProgress_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/users/{userId}/level", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // Gherkin: Alternativo exitoso — usuario nuevo sin experiencia → nivel 1 con 0 XP
    @Test
    @WithMockUser(username = "user@example.com")
    void getLevelProgress_newUser_returns200WithInitialState() throws Exception {
        UUID userId = UUID.randomUUID();
        LevelProgressResponse response = new LevelProgressResponse(
                userId, 1, 0, BigDecimal.ZERO, null
        );
        when(gamificationService.getLevelProgress(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/level", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.experience").value(0))
                .andExpect(jsonPath("$.progressPercentage").value(0));
    }

    // Gherkin: Alternativo error — registro no existe → sistema lo inicializa y devuelve estado base
    @Test
    @WithMockUser(username = "user@example.com")
    void getLevelProgress_progressNotExists_returns200WithBaseState() throws Exception {
        UUID userId = UUID.randomUUID();
        // El servicio inicializa el registro y devuelve el estado base (nivel 1, 0 XP)
        LevelProgressResponse response = new LevelProgressResponse(
                userId, 1, 0, BigDecimal.ZERO, LocalDateTime.now()
        );
        when(gamificationService.getLevelProgress(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamification/users/{userId}/level", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.experience").value(0));
    }
}
