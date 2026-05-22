package com.mentoredu.notifications.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.feedback.event.FeedbackGivenEvent;
import com.mentoredu.feedback.event.SolutionReviewedEvent;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.event.CommentCreatedEvent;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.gamification.event.BadgeAwardedEvent;
import com.mentoredu.gamification.event.LevelUpEvent;
import com.mentoredu.moderation.event.AppealResolvedEvent;
import com.mentoredu.notifications.dto.NotificationResponse;
import com.mentoredu.notifications.exception.NotificationNotFoundException;
import com.mentoredu.notifications.model.Notification;
import com.mentoredu.notifications.repository.NotificationRepository;
import com.mentoredu.verification.event.VerificationProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getPendingNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));
        return notificationRepository
                .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notificación no encontrada: " + notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return new NotificationResponse(notification);
    }

    // =========================================================================
    // Event listeners — fire AFTER the business transaction commits.
    // @Async dispatches each to the dedicated "notificationExecutor" thread pool.
    // Failures here are isolated and cannot roll back the committed business tx.
    // =========================================================================

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        saveNotification(event.threadAuthorId(),
                "ANSWER_RECEIVED",
                "Nueva respuesta en tu hilo",
                "Alguien respondió tu pregunta.");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        saveNotification(event.answerAuthorId(),
                "COMMENT_RECEIVED",
                "Nuevo comentario en tu respuesta",
                "Alguien comentó: \"" + event.truncatedBody() + "\"");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackGiven(FeedbackGivenEvent event) {
        saveNotification(event.targetUserId(),
                "FEEDBACK_RECEIVED",
                "Tienes nueva retroalimentación",
                "Recibiste feedback académico de " + event.authorName() + ".");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSolutionReviewed(SolutionReviewedEvent event) {
        saveNotification(event.studentId(),
                "SOLUTION_REVIEWED",
                "Tu solución fue evaluada",
                "Tu resolución de \"" + event.resourceTitle() + "\" fue evaluada.");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationProcessed(VerificationProcessedEvent event) {
        String result = "VERIFIED".equals(event.newStatus()) ? "aprobada" : "rechazada";
        saveNotification(event.userId(),
                "VERIFICATION_PROCESSED",
                "Tu verificación fue procesada",
                "Tu solicitud de verificación (" + event.entityType().toLowerCase() + ") fue " + result + ".");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppealResolved(AppealResolvedEvent event) {
        saveNotification(event.userId(),
                "APPEAL_RESOLVED",
                "Tu apelación fue procesada",
                "Tu apelación fue resuelta con estado: " + event.newStatus() + ".");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBadgeAwarded(BadgeAwardedEvent event) {
        saveNotification(event.userId(),
                "BADGE_EARNED",
                "¡Nueva insignia desbloqueada!",
                "Obtuviste la insignia \"" + event.badgeName() + "\".");
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLevelUp(LevelUpEvent event) {
        saveNotification(event.userId(),
                "LEVEL_UP",
                "¡Subiste de nivel!",
                "¡Felicitaciones! Alcanzaste el nivel " + event.newLevel() + ".");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void saveNotification(UUID userId, String type, String title, String message) {
        userRepository.findById(userId).ifPresent(user ->
            notificationRepository.save(
                Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .build()
            )
        );
    }
}
