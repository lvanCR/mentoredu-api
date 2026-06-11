package com.mentoredu.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!prod")
public class DevEmailService implements IEmailService {

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        log.info("[DEV] Password reset link for {}: {}/reset-password?token={}",
                to, frontendBaseUrl, token);
    }

    @Override
    public void sendContactNotification(String name, String email, String phone, String category, String institution) {
        log.info("[DEV] Contact form — name: {}, email: {}, role: {}, institution: {}, phone: {}",
                name, email, category, institution, phone);
        log.info("[DEV] Confirmation email would be sent to: {}", email);
    }
}
