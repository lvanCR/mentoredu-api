package com.mentoredu.auth.listener;

import com.mentoredu.auth.event.PasswordResetRequestedEvent;
import com.mentoredu.auth.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final IEmailService emailService;

    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            emailService.sendPasswordResetEmail(event.email(), event.rawToken());
        } catch (Exception e) {
            log.error("Error enviando email de recuperación a {}: {}", event.email(), e.getMessage(), e);
        }
    }
}
