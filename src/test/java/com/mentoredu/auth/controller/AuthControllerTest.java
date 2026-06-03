package com.mentoredu.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.dto.*;
import com.mentoredu.auth.exception.EmailAlreadyExistsException;
import com.mentoredu.auth.exception.EmailNotFoundException;
import com.mentoredu.auth.exception.InvalidCredentialsException;
import com.mentoredu.auth.exception.InvalidTokenException;
import com.mentoredu.auth.service.AuthService;
import com.mentoredu.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para AuthController.
 * Cubre: US01 (registro), US02 (login/logout/refresh), US03 (forgot-password/reset-password).
 * Reglas de negocio: RN-01 (rol único en registro), RN-02 (contraseñas cifradas).
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private AuthService authService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── US01 — Registrar cuenta ────────────────────────────────────────────────
    // RN-01: rol asignado en registro (STUDENT|TEACHER|ACADEMY), RN-02: contraseña cifrada

    /** US01 Escenario 1 — Registro exitoso como STUDENT → 201 con tokens */
    @Test
    void register_asStudent_returns201WithTokens() throws Exception {
        when(authService.register(any())).thenReturn(registerResponse("STUDENT"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRegisterRequest("STUDENT"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("juan@example.com"))
            .andExpect(jsonPath("$.role").value("STUDENT"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(jsonPath("$.passwordHash").doesNotExist()); // RN-02: nunca exponer hash
    }

    /** US01 — Registro exitoso como TEACHER → 201 con rol TEACHER */
    @Test
    void register_asTeacher_returns201WithTeacherRole() throws Exception {
        when(authService.register(any())).thenReturn(registerResponse("TEACHER"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRegisterRequest("TEACHER"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("TEACHER"));
    }

    /** US01 — Registro exitoso como ACADEMY → 201 con rol ACADEMY */
    @Test
    void register_asAcademy_returns201WithAcademyRole() throws Exception {
        when(authService.register(any())).thenReturn(registerResponse("ACADEMY"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRegisterRequest("ACADEMY"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("ACADEMY"));
    }

    /** US01 Escenario 2 — Email duplicado → 409 Conflict */
    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
            .thenThrow(new EmailAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validRegisterRequest("STUDENT"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US01 Escenario 3 — Rol MODERATOR (prohibido en registro) → 400 */
    @Test
    void register_withForbiddenRole_returns400() throws Exception {
        var req = validRegisterRequest("MODERATOR"); // RN-01: solo STUDENT|TEACHER|ACADEMY
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.role").exists());
    }

    /** US01 Escenario 3 — Rol ADMIN (prohibido en registro) → 400 */
    @Test
    void register_withAdminRole_returns400() throws Exception {
        var req = validRegisterRequest("ADMIN");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.role").exists());
    }

    /** US01 Escenario 3 — Sin campo role → 400 */
    @Test
    void register_withMissingRole_returns400() throws Exception {
        var req = validRegisterRequest(null);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.role").exists());
    }

    /** US01 Escenario 4 — firstName en blanco → 400 */
    @Test
    void register_withBlankFirstName_returns400() throws Exception {
        var req = validRegisterRequest("STUDENT");
        req.setFirstName("");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.firstName").exists());
    }

    /** US01 Escenario 4 — Email inválido → 400 */
    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        var req = validRegisterRequest("STUDENT");
        req.setEmail("not-an-email");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());
    }

    /** US01 / RN-02 — Contraseña débil (< 8 chars) → 400 */
    @Test
    void register_withWeakPassword_returns400() throws Exception {
        var req = validRegisterRequest("STUDENT");
        req.setPassword("weak");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    /** US01 / RN-02 — Contraseña sin mayúscula → 400 */
    @Test
    void register_withPasswordMissingUppercase_returns400() throws Exception {
        var req = validRegisterRequest("STUDENT");
        req.setPassword("nouppercase1");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /** US01 / RN-02 — Contraseña sin dígito → 400 */
    @Test
    void register_withPasswordMissingDigit_returns400() throws Exception {
        var req = validRegisterRequest("STUDENT");
        req.setPassword("NoDigitPassword");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── US02 — Login ───────────────────────────────────────────────────────────

    /** US02 Escenario 1 — Login exitoso → 200 con access + refresh tokens */
    @Test
    void login_withValidCredentials_returns200WithTokens() throws Exception {
        var response = LoginResponse.builder()
            .accessToken("access.token").refreshToken("refresh-uuid")
            .tokenType("Bearer").expiresIn(900L)
            .user(LoginResponse.UserInfo.builder()
                .id(UUID.randomUUID()).email("juan@example.com")
                .firstName("Juan").lastName("Pérez").role("STUDENT").build())
            .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validLoginRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(jsonPath("$.user.email").value("juan@example.com"))
            .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    /** US02 Escenario 2 — Contraseña incorrecta → 401 */
    @Test
    void login_withInvalidPassword_returns401() throws Exception {
        when(authService.login(any()))
            .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validLoginRequest())))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /** US02 — Email inexistente → 401 (no revelar si el email existe) */
    @Test
    void login_withNonExistentEmail_returns401() throws Exception {
        when(authService.login(any()))
            .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        var req = validLoginRequest();
        req.setEmail("nobody@example.com");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    /** US02 — email en blanco → 400 */
    @Test
    void login_withMissingEmail_returns400() throws Exception {
        var req = validLoginRequest();
        req.setEmail(null);
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());
    }

    /** US02 — password en blanco → 400 */
    @Test
    void login_withMissingPassword_returns400() throws Exception {
        var req = validLoginRequest();
        req.setPassword(null);
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    // ── US02 — Logout ──────────────────────────────────────────────────────────

    /** US02 Escenario 3 — Logout exitoso → 204 No Content */
    @Test
    void logout_withValidToken_returns204() throws Exception {
        var req = new LogoutRequest();
        req.setRefreshToken("valid-refresh-token");
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());
    }

    /** US02 — Token de logout inválido → 401 */
    @Test
    void logout_withInvalidToken_returns401() throws Exception {
        doThrow(new InvalidCredentialsException("Session not found")).when(authService).logout(any());

        var req = new LogoutRequest();
        req.setRefreshToken("bad-token");
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /** US02 — Token ya revocado → 401 */
    @Test
    void logout_withAlreadyRevokedToken_returns401() throws Exception {
        doThrow(new InvalidCredentialsException("Session is already revoked")).when(authService).logout(any());

        var req = new LogoutRequest();
        req.setRefreshToken("revoked-token");
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Session is already revoked"));
    }

    /** US02 — refreshToken ausente → 400 */
    @Test
    void logout_withMissingToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new LogoutRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.refreshToken").exists());
    }

    // ── US02 — Refresh token ───────────────────────────────────────────────────

    /** US02 Escenario 5 — Renovación exitosa → 200 con nuevo access token */
    @Test
    void refresh_withValidToken_returns200WithNewAccessToken() throws Exception {
        var response = RefreshTokenResponse.builder()
            .accessToken("new.access.token").expiresIn(900L).build();
        when(authService.refresh(any())).thenReturn(response);

        var req = new RefreshTokenRequest();
        req.setRefreshToken("valid-refresh-uuid");
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.expiresIn").value(900));
    }

    /** US02 Escenario 4 — Refresh token revocado → 401 */
    @Test
    void refresh_withRevokedToken_returns401() throws Exception {
        when(authService.refresh(any()))
            .thenThrow(new InvalidCredentialsException("Refresh token has been revoked"));

        var req = new RefreshTokenRequest();
        req.setRefreshToken("revoked-token");
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Refresh token has been revoked"));
    }

    /** US02 Escenario 4 — Refresh token expirado → 401 */
    @Test
    void refresh_withExpiredToken_returns401() throws Exception {
        when(authService.refresh(any()))
            .thenThrow(new InvalidCredentialsException("Refresh token has expired"));

        var req = new RefreshTokenRequest();
        req.setRefreshToken("expired-token");
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    /** US02 — refreshToken ausente en /refresh → 400 */
    @Test
    void refresh_withMissingToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshTokenRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.refreshToken").exists());
    }

    // ── US03 — Recuperar contraseña ────────────────────────────────────────────

    /** US03 Escenario 1 — Email registrado → 200 con mensaje genérico */
    @Test
    void forgotPassword_withRegisteredEmail_returns200WithGenericMessage() throws Exception {
        var response = ForgotPasswordResponse.builder()
            .message("Si existe una cuenta con ese correo, recibirás un enlace en breve.").build();
        when(authService.forgotPassword(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(forgotPasswordRequest("juan@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.token").doesNotExist()); // nunca exponer el token en la respuesta
    }

    /** US03 Escenario 2 — Email no registrado → 404 (en este endpoint sí retorna 404) */
    @Test
    void forgotPassword_withUnknownEmail_returns404() throws Exception {
        when(authService.forgotPassword(any()))
            .thenThrow(new EmailNotFoundException("No account found"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(forgotPasswordRequest("nobody@example.com"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** US03 — Email con formato inválido → 400 */
    @Test
    void forgotPassword_withInvalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(forgotPasswordRequest("not-an-email"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());
    }

    /** US03 — Email ausente → 400 */
    @Test
    void forgotPassword_withMissingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ForgotPasswordRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());
    }

    /** US03 Escenario 3 — Token válido → restablecimiento exitoso 200 */
    @Test
    void resetPassword_withValidToken_returns200() throws Exception {
        var response = ResetPasswordResponse.builder().message("Password reset successfully.").build();
        when(authService.resetPassword(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validResetRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    /** US03 Escenario 4 — Token expirado → 400 Bad Request */
    @Test
    void resetPassword_withExpiredToken_returns400() throws Exception {
        when(authService.resetPassword(any())).thenThrow(new InvalidTokenException("Token has expired"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validResetRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Token has expired"));
    }

    /** US03 Escenario 4 — Token ya usado → 400 */
    @Test
    void resetPassword_withAlreadyUsedToken_returns400() throws Exception {
        when(authService.resetPassword(any())).thenThrow(new InvalidTokenException("Token has already been used"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validResetRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Token has already been used"));
    }

    /** US03 / RN-02 — Nueva contraseña débil → 400 con campo newPassword */
    @Test
    void resetPassword_withWeakNewPassword_returns400() throws Exception {
        var req = validResetRequest();
        req.setNewPassword("weak");
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.newPassword").exists());
    }

    /** US03 — Token ausente en reset → 400 con campo token */
    @Test
    void resetPassword_withMissingToken_returns400() throws Exception {
        var req = new ResetPasswordRequest();
        req.setNewPassword("NewPassword123");
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.token").exists());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private RegisterRequest validRegisterRequest(String role) {
        var r = new RegisterRequest();
        r.setFirstName("Juan");
        r.setLastName("Pérez");
        r.setEmail("juan@example.com");
        r.setPassword("Password123");
        r.setRole(role);
        return r;
    }

    private RegisterResponse registerResponse(String role) {
        return RegisterResponse.builder()
            .id(UUID.randomUUID()).firstName("Juan").lastName("Pérez")
            .email("juan@example.com").role(role).status("ACTIVE")
            .accessToken("access.token").refreshToken("refresh-uuid")
            .tokenType("Bearer").expiresIn(900L).build();
    }

    private LoginRequest validLoginRequest() {
        var r = new LoginRequest();
        r.setEmail("juan@example.com");
        r.setPassword("Password123");
        return r;
    }

    private ForgotPasswordRequest forgotPasswordRequest(String email) {
        var r = new ForgotPasswordRequest();
        r.setEmail(email);
        return r;
    }

    private ResetPasswordRequest validResetRequest() {
        var r = new ResetPasswordRequest();
        r.setToken("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2");
        r.setNewPassword("NewPassword123");
        return r;
    }
}
