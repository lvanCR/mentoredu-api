package com.mentoredu.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.dto.ForgotPasswordRequest;
import com.mentoredu.auth.dto.ForgotPasswordResponse;
import com.mentoredu.auth.dto.LoginRequest;
import com.mentoredu.auth.dto.LoginResponse;
import com.mentoredu.auth.dto.RefreshTokenRequest;
import com.mentoredu.auth.dto.RefreshTokenResponse;
import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.dto.RegisterResponse;
import com.mentoredu.auth.dto.ResetPasswordRequest;
import com.mentoredu.auth.dto.ResetPasswordResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void register_withValidData_returns201AndUserInfo() throws Exception {
        var response = RegisterResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .role("STUDENT")
                .status("ACTIVE")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test.signature")
                .refreshToken("550e8400-e29b-41d4-a716-446655440000")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: juan@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void register_withBlankFirstName_returns400() throws Exception {
        var request = validRequest();
        request.setFirstName("");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").exists());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        var request = validRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void register_withWeakPassword_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("weak");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_withPasswordMissingUppercase_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("nouppercase1");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordMissingDigit_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("NoDigitPassword");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // F0.3 — Registrar como TEACHER → 201 con rol TEACHER
    @Test
    void register_asTeacher_returns201WithTeacherRole() throws Exception {
        var response = RegisterResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Ana")
                .lastName("García")
                .email("ana@example.com")
                .role("TEACHER")
                .status("ACTIVE")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test.signature")
                .refreshToken("550e8400-e29b-41d4-a716-446655440001")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        when(authService.register(any())).thenReturn(response);

        var request = validRequest();
        request.setRole("TEACHER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    // F0.3 — Registrar como ACADEMY → 201 con rol ACADEMY
    @Test
    void register_asAcademy_returns201WithAcademyRole() throws Exception {
        var response = RegisterResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Corp")
                .lastName("SA")
                .email("corp@example.com")
                .role("ACADEMY")
                .status("ACTIVE")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test.signature")
                .refreshToken("550e8400-e29b-41d4-a716-446655440002")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        when(authService.register(any())).thenReturn(response);

        var request = validRequest();
        request.setRole("ACADEMY");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ACADEMY"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    // F0.3 — Rol prohibido (MODERATOR) → 400 Bad Request
    @Test
    void register_withForbiddenRole_returns400() throws Exception {
        var request = validRequest();
        request.setRole("MODERATOR");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.role").exists());
    }

    // F0.3 — Sin campo role → 400 Bad Request
    @Test
    void register_withMissingRole_returns400() throws Exception {
        var request = validRequest();
        request.setRole(null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.role").exists());
    }

    private RegisterRequest validRequest() {
        var r = new RegisterRequest();
        r.setFirstName("Juan");
        r.setLastName("Pérez");
        r.setEmail("juan@example.com");
        r.setPassword("Password123");
        r.setRole("STUDENT");
        return r;
    }

    // -------------------------------------------------------------------------
    // US02 — Login
    // -------------------------------------------------------------------------

    @Test
    void login_withValidCredentials_returns200WithTokens() throws Exception {
        var response = LoginResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test.signature")
                .refreshToken("550e8400-e29b-41d4-a716-446655440000")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(LoginResponse.UserInfo.builder()
                        .id(UUID.randomUUID())
                        .email("juan@example.com")
                        .firstName("Juan")
                        .lastName("Pérez")
                        .role("STUDENT")
                        .build())
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("juan@example.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    @Test
    void login_withInvalidPassword_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void login_withNonExistentEmail_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        var req = validLoginRequest();
        req.setEmail("nobody@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void login_withInactiveAccount_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Account is not active"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Account is not active"));
    }

    @Test
    void login_withMissingEmail_returns400() throws Exception {
        var req = validLoginRequest();
        req.setEmail(null);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void login_withInvalidEmailFormat_returns400() throws Exception {
        var req = validLoginRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void login_withMissingPassword_returns400() throws Exception {
        var req = validLoginRequest();
        req.setPassword(null);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    private LoginRequest validLoginRequest() {
        var r = new LoginRequest();
        r.setEmail("juan@example.com");
        r.setPassword("Password123");
        return r;
    }

    // -------------------------------------------------------------------------
    // US03 — Request password recovery
    // -------------------------------------------------------------------------

    @Test
    void forgotPassword_withRegisteredEmail_returns200WithMessage() throws Exception {
        var response = ForgotPasswordResponse.builder()
                .message("Si existe una cuenta con ese correo, recibirás un enlace de recuperación en breve.")
                .build();

        when(authService.forgotPassword(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validForgotPasswordRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void forgotPassword_withUnknownEmail_returns404() throws Exception {
        when(authService.forgotPassword(any()))
                .thenThrow(new EmailNotFoundException("No account found for: nobody@example.com"));

        var req = validForgotPasswordRequest();
        req.setEmail("nobody@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void forgotPassword_withInvalidEmailFormat_returns400() throws Exception {
        var req = validForgotPasswordRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void forgotPassword_withMissingEmail_returns400() throws Exception {
        var req = new ForgotPasswordRequest();

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    private ForgotPasswordRequest validForgotPasswordRequest() {
        var r = new ForgotPasswordRequest();
        r.setEmail("juan@example.com");
        return r;
    }

    // -------------------------------------------------------------------------
    // US26 — Reset password with token
    // -------------------------------------------------------------------------

    @Test
    void resetPassword_withValidToken_returns200() throws Exception {
        var response = ResetPasswordResponse.builder()
                .message("Password reset successfully. All active sessions have been closed.")
                .build();

        when(authService.resetPassword(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResetPasswordRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetPassword_withExpiredToken_returns400() throws Exception {
        when(authService.resetPassword(any()))
                .thenThrow(new InvalidTokenException("Token has expired"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResetPasswordRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token has expired"));
    }

    @Test
    void resetPassword_withAlreadyUsedToken_returns400() throws Exception {
        when(authService.resetPassword(any()))
                .thenThrow(new InvalidTokenException("Token has already been used"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResetPasswordRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token has already been used"));
    }

    @Test
    void resetPassword_withWeakPassword_returns400() throws Exception {
        var req = validResetPasswordRequest();
        req.setNewPassword("weak");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.newPassword").exists());
    }

    @Test
    void resetPassword_withNonExistentToken_returns400() throws Exception {
        when(authService.resetPassword(any()))
                .thenThrow(new InvalidTokenException("Token not found or invalid"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResetPasswordRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token not found or invalid"));
    }

    @Test
    void resetPassword_withMissingToken_returns400() throws Exception {
        var req = new ResetPasswordRequest();
        req.setNewPassword("NewPassword123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.token").exists());
    }

    private ResetPasswordRequest validResetPasswordRequest() {
        var r = new ResetPasswordRequest();
        r.setToken("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2");
        r.setNewPassword("NewPassword123");
        return r;
    }

    // -------------------------------------------------------------------------
    // F0.5 — POST /auth/refresh
    // -------------------------------------------------------------------------

    @Test
    void refresh_withValidToken_returns200WithNewAccessToken() throws Exception {
        var response = RefreshTokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.new.token")
                .expiresIn(900L)
                .build();

        when(authService.refresh(any())).thenReturn(response);

        var request = new RefreshTokenRequest();
        request.setRefreshToken("550e8400-e29b-41d4-a716-446655440000");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void refresh_withRevokedToken_returns401() throws Exception {
        when(authService.refresh(any()))
                .thenThrow(new InvalidCredentialsException("Refresh token has been revoked"));

        var request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked"));
    }

    @Test
    void refresh_withExpiredToken_returns401() throws Exception {
        when(authService.refresh(any()))
                .thenThrow(new InvalidCredentialsException("Refresh token has expired"));

        var request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has expired"));
    }

    @Test
    void refresh_withMissingToken_returns400() throws Exception {
        var request = new RefreshTokenRequest();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.refreshToken").exists());
    }
}
