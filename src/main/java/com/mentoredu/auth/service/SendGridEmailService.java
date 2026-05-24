package com.mentoredu.auth.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SendGridEmailService implements IEmailService {

    private final SendGrid sendGrid;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Async("emailExecutor")
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
        String body =
            "Hola,\n\n"
            + "Haz clic en el siguiente enlace para restablecer tu contraseña:\n\n"
            + resetLink + "\n\n"
            + "Este enlace expirará en 60 minutos.\n\n"
            + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
            + "El equipo de MentorEdu";

        Mail mail = new Mail(
            new Email(fromEmail),
            "Recupera tu contraseña — MentorEdu",
            new Email(to),
            new Content("text/plain", body)
        );

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("[MAIL] SendGrid API error {} sending to {}: {}",
                    response.getStatusCode(), to, response.getBody());
            } else {
                log.info("[MAIL] Password reset email sent to {} (HTTP {})",
                    to, response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("[MAIL] Network error sending to {}: {}", to, e.getMessage(), e);
        }
    }
}
