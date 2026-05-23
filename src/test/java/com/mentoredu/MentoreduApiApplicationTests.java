package com.mentoredu;

import com.mentoredu.auth.entity.AuthProvider;
import com.mentoredu.auth.entity.Role;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.entity.UserStatus;
import com.mentoredu.auth.repository.RoleRepository;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MentoreduApiApplicationTests {

    @Autowired private ApplicationEventPublisher  eventPublisher;
    @Autowired private NotificationRepository     notificationRepository;
    @Autowired private UserRepository             userRepository;
    @Autowired private RoleRepository             roleRepository;
    @Autowired private TransactionTemplate        transactionTemplate;

    private UUID testUserId;

    @BeforeEach
    void createTestUser() {
        Role role = roleRepository.findByName("STUDENT").orElseGet(() -> {
            Role r = new Role();
            r.setName("STUDENT");
            r.setDescription("Estudiante");
            return roleRepository.save(r);
        });

        User user = userRepository.save(User.builder()
                .firstName("Notif")
                .lastName("Test")
                .email("notif." + System.currentTimeMillis() + "@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .provider(AuthProvider.EMAIL)
                .status(UserStatus.ACTIVE)
                .role(role)
                .build());

        testUserId = user.getId();
    }

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
        if (testUserId != null) {
            userRepository.deleteById(testUserId);
        }
    }

    @Test
    void contextLoads() {
    }

    // -------------------------------------------------------------------------
    // F3.1 Fase 4 — Verificación de integración AFTER_COMMIT
    // Publica AnswerCreatedEvent dentro de una transacción real (TransactionTemplate
    // hace commit) y verifica que el listener @Async persiste la notificación.
    // -------------------------------------------------------------------------

    @Test
    void answerCreatedEvent_afterCommit_persistsNotificationForThreadAuthor()
            throws InterruptedException {

        UUID answerAuthorId = UUID.randomUUID(); // distinto al autor del hilo

        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new AnswerCreatedEvent(
                    UUID.randomUUID(),
                    testUserId,
                    answerAuthorId,
                    "¿Cómo resuelvo integrales definidas?"));
            return null;
        });

        // Esperar a que el listener @Async("notificationExecutor") persista
        Thread.sleep(500);

        List<Notification> pending = notificationRepository
                .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(testUserId,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        assertFalse(pending.isEmpty(), "Debe existir al menos una notificación pendiente");
        assertEquals("answer_received", pending.get(0).getType());
    }
}
