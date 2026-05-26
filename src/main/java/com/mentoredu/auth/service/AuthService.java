package com.mentoredu.auth.service;

import com.mentoredu.auth.dto.ForgotPasswordRequest;
import com.mentoredu.auth.dto.LogoutRequest;
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
import com.mentoredu.auth.event.PasswordResetRequestedEvent;
import com.mentoredu.auth.event.UserRegisteredEvent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository               userRepository;
    private final RoleRepository               roleRepository;
    private final SessionRepository            sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder              passwordEncoder;
    private final JwtUtil                      jwtUtil;
    private final ApplicationEventPublisher    eventPublisher;

    @Value("${app.password-reset.expiration-minutes:60}")
    private long passwordResetExpirationMinutes;

    @Value("${app.session.max-active:5}")
    private int maxActiveSessions;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("El email ya está registrado: " + normalizedEmail);
        }

        var role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + request.getRole() + ". Ejecutar migraciones BD."));

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

        eventPublisher.publishEvent(
                new UserRegisteredEvent(saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getRole().getName()));

        String accessToken  = jwtUtil.generateAccessToken(saved);
        String refreshToken = createSession(saved);

        return RegisterResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .status(saved.getStatus().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationMs() / 1000)
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Email o contraseña inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email o contraseña inválidos");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("La cuenta no está activa");
        }

        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = createSession(user);

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
    // US02 — Logout
    // -------------------------------------------------------------------------

    @Transactional
    public void logout(LogoutRequest request) {
        Session session = sessionRepository.findByRefreshToken(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new InvalidCredentialsException("Token de sesión no encontrado o inválido"));

        if (session.getRevokedAt() != null) {
            throw new InvalidCredentialsException("La sesión ya fue revocada");
        }

        session.setRevokedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    // -------------------------------------------------------------------------
    // US03 — Request password recovery
    // -------------------------------------------------------------------------

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String genericMessage = "Si existe una cuenta con ese correo, recibirás un enlace de recuperación en breve.";

        var maybeUser = userRepository.findByEmail(normalizedEmail);
        if (maybeUser.isEmpty()) {
            // No revelar si el email existe o no (previene user enumeration — OWASP A07)
            log.debug("Password reset requested for non-existent email: {}", normalizedEmail);
            return ForgotPasswordResponse.builder().message(genericMessage).build();
        }

        User user = maybeUser.get();

        // Invalidate any previous active tokens for this user before issuing a new one
        passwordResetTokenRepository.invalidateAllByUserId(user.getId());

        String rawToken   = generateSecureToken();
        String tokenHash  = hashToken(rawToken);

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                .used(false)
                .build());

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getEmail(), rawToken));

        return ForgotPasswordResponse.builder().message(genericMessage).build();
    }

    // -------------------------------------------------------------------------
    // US26 — Reset password with token
    // -------------------------------------------------------------------------

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Token no encontrado o inválido"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("El token ya fue utilizado");
        }

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("El token ha expirado");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all active sessions — RN-03 / US26 alternativo exitoso
        sessionRepository.revokeAllActiveByUserId(user.getId(), LocalDateTime.now());

        return ResetPasswordResponse.builder()
                .message("Contraseña restablecida correctamente. Todas las sesiones activas fueron cerradas.")
                .build();
    }

    // -------------------------------------------------------------------------
    // F0.5 — POST /auth/refresh
    // -------------------------------------------------------------------------

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        Session session = sessionRepository.findByRefreshToken(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new InvalidCredentialsException("Token de sesión no encontrado o inválido"));

        if (session.getRevokedAt() != null) {
            throw new InvalidCredentialsException("El token de sesión fue revocado");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("El token de sesión ha expirado");
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

    private String createSession(User user) {
        revokeOldestSessionsIfOverLimit(user);
        String rawToken = generateSecureToken();
        sessionRepository.save(Session.builder()
                .user(user)
                .refreshToken(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationMs() / 1000))
                .build());
        return rawToken;
    }

    private void revokeOldestSessionsIfOverLimit(User user) {
        List<Session> active = sessionRepository.findAllActiveByUserId(user.getId(), LocalDateTime.now());
        if (active.size() >= maxActiveSessions) {
            int excess = active.size() - maxActiveSessions + 1;
            List<Session> toRevoke = active.subList(0, excess);
            LocalDateTime now = LocalDateTime.now();
            toRevoke.forEach(s -> s.setRevokedAt(now));
            sessionRepository.saveAll(toRevoke);
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(HexFormat.of().parseHex(rawToken));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
