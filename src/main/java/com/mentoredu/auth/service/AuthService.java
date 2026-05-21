package com.mentoredu.auth.service;

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
import com.mentoredu.auth.entity.AuthProvider;
import com.mentoredu.auth.entity.PasswordResetToken;
import com.mentoredu.auth.entity.Session;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.entity.UserStatus;
import com.mentoredu.auth.exception.EmailAlreadyExistsException;
import com.mentoredu.auth.exception.EmailNotFoundException;
import com.mentoredu.auth.exception.InvalidCredentialsException;
import com.mentoredu.auth.exception.InvalidTokenException;
import com.mentoredu.auth.repository.PasswordResetTokenRepository;
import com.mentoredu.auth.repository.RoleRepository;
import com.mentoredu.auth.repository.SessionRepository;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository               userRepository;
    private final RoleRepository               roleRepository;
    private final SessionRepository            sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder              passwordEncoder;
    private final JwtUtil                      jwtUtil;

    @Value("${app.password-reset.expiration-minutes:60}")
    private long passwordResetExpirationMinutes;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already registered: " + normalizedEmail);
        }

        var role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Role not found: " + request.getRole() + ". Run DB migrations."));

        var user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.EMAIL)
                .status(UserStatus.ACTIVE)
                .role(role)
                .build();

        var saved = userRepository.save(user);

        return RegisterResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .status(saved.getStatus().name())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Account is not active");
        }

        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();

        sessionRepository.save(Session.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationMs() / 1000))
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationMs() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole().getName())
                        .build())
                .build();
    }

    // -------------------------------------------------------------------------
    // US03 — Request password recovery
    // -------------------------------------------------------------------------

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new EmailNotFoundException("No account found for: " + normalizedEmail));

        // Invalidate any previous active tokens for this user before issuing a new one
        passwordResetTokenRepository.invalidateAllByUserId(user.getId());

        String rawToken = generateSecureToken();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                .used(false)
                .build());

        return ForgotPasswordResponse.builder()
                .message("Recovery token generated. In production this would be sent via email.")
                .token(rawToken)
                .build();
    }

    // -------------------------------------------------------------------------
    // US26 — Reset password with token
    // -------------------------------------------------------------------------

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Token not found or invalid"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all active sessions — RN-03 / US26 alternativo exitoso
        sessionRepository.revokeAllActiveByUserId(user.getId(), LocalDateTime.now());

        return ResetPasswordResponse.builder()
                .message("Password reset successfully. All active sessions have been closed.")
                .build();
    }

    // -------------------------------------------------------------------------
    // F0.5 — POST /auth/refresh
    // -------------------------------------------------------------------------

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        Session session = sessionRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found or invalid"));

        if (session.getRevokedAt() != null) {
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        String accessToken = jwtUtil.generateAccessToken(session.getUser());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtUtil.getAccessExpirationMs() / 1000)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
