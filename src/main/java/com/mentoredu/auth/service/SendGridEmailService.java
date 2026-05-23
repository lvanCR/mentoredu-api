package com.mentoredu.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SendGridEmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Async("emailExecutor")
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Recupera tu contraseña — MentorEdu");
            message.setText(
                "Hola,\n\n"
                + "Haz clic en el siguiente enlace para restablecer tu contraseña:\n\n"
                + frontendBaseUrl + "/reset-password?token=" + token + "\n\n"
                + "Este enlace expirará en 60 minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
                + "El equipo de MentorEdu"
            );
            mailSender.send(message);
            log.info("[MAIL] Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("[MAIL] Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        }
    }
}
