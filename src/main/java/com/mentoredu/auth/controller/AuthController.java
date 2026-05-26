package com.mentoredu.auth.controller;

import com.mentoredu.auth.dto.ForgotPasswordRequest;
import com.mentoredu.auth.dto.ForgotPasswordResponse;
import com.mentoredu.auth.dto.LoginRequest;
import com.mentoredu.auth.dto.LoginResponse;
import com.mentoredu.auth.dto.LogoutRequest;
import com.mentoredu.auth.dto.RefreshTokenRequest;
import com.mentoredu.auth.dto.RefreshTokenResponse;
import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.dto.RegisterResponse;
import com.mentoredu.auth.dto.ResetPasswordRequest;
import com.mentoredu.auth.dto.ResetPasswordResponse;
import com.mentoredu.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login, logout y recuperación de contraseña (US01–US03).")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "US01 - Registrar cuenta con email y rol")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "US02 - Iniciar sesión")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "US02 - Cerrar sesión")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "US03 - Solicitar recuperación de contraseña")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "US03 - Restablecer contraseña con token")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "US02 - Renovar access token con refresh token")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
