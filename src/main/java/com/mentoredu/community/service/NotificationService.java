package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.event.AssociationResolvedEvent;
import com.mentoredu.community.event.UserFollowedEvent;
import com.mentoredu.community.event.VerificationProcessedEvent;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.event.CommentCreatedEvent;
import com.mentoredu.forum.event.ReactionCreatedEvent;
import com.mentoredu.pedagogy.event.FeedbackGivenEvent;
import com.mentoredu.pedagogy.event.SolutionSubmittedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getPendingNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).toList();
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada: " + notificationId));

        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        userRepository.findById(event.threadAuthorId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("answer_received")
                    .payload(Map.of(
                        "answerId",    event.answerId().toString(),
                        "threadTitle", event.threadTitle()
                    ))
                    .build())
        );
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        userRepository.findById(event.answerAuthorId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("comment_received")
                    .payload(Map.of(
                        "commentId",    event.commentId().toString(),
                        "truncatedBody", event.truncatedBody()
                    ))
                    .build())
        );
    }

    // -------------------------------------------------------------------------
    // US23 — verification_processed
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationProcessed(VerificationProcessedEvent event) {
        userRepository.findById(event.requesterId()).ifPresent(recipient -> {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("requestId",  event.requestId().toString());
            payload.put("entityType", event.entityType());
            payload.put("status",     event.status());
            if (event.notes() != null) payload.put("notes", event.notes());
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("verification_processed")
                    .payload(payload)
                    .build());
        });
    }

    // -------------------------------------------------------------------------
    // US24 — association_resolved
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssociationResolved(AssociationResolvedEvent event) {
        userRepository.findById(event.teacherUserId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("association_resolved")
                    .payload(Map.of(
                        "linkId",           event.linkId().toString(),
                        "academyProfileId", event.academyProfileId().toString(),
                        "status",           event.status()
                    ))
                    .build())
        );
    }

    // -------------------------------------------------------------------------
    // US21 — new_follower
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserFollowed(UserFollowedEvent event) {
        userRepository.findById(event.followedId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("new_follower")
                    .payload(Map.of("followerId", event.followerId().toString()))
                    .build())
        );
    }

    // -------------------------------------------------------------------------
    // US14 — reaction_received
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReactionCreated(ReactionCreatedEvent event) {
        userRepository.findById(event.contentAuthorId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("reaction_received")
                    .payload(Map.of(
                        "reactorId",    event.reactorId().toString(),
                        "targetType",   event.targetType(),
                        "targetId",     event.targetId().toString(),
                        "reactionType", event.reactionType()
                    ))
                    .build())
        );
    }

    // -------------------------------------------------------------------------
    // US18 — solution_submitted
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSolutionSubmitted(SolutionSubmittedEvent event) {
        userRepository.findById(event.exerciseAuthorId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("solution_submitted")
                    .payload(Map.of(
                        "solutionId", event.solutionId().toString(),
                        "resourceId", event.resourceId().toString(),
                        "studentId",  event.studentId().toString()
                    ))
                    .build())
        );
    }

    // -------------------------------------------------------------------------
    // US19 — feedback_received
    // -------------------------------------------------------------------------

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackGiven(FeedbackGivenEvent event) {
        userRepository.findById(event.studentId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("feedback_received")
                    .payload(Map.of(
                        "feedbackId", event.feedbackId().toString(),
                        "solutionId", event.solutionId().toString(),
                        "resourceId", event.resourceId().toString()
                    ))
                    .build())
        );
    }
}
