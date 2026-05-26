package com.mentoredu.community.service;

import com.mentoredu.auth.service.UserService;
import com.mentoredu.community.event.AssociationResolvedEvent;
import com.mentoredu.community.event.UserFollowedEvent;
import com.mentoredu.community.event.VerificationProcessedEvent;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.event.CommentCreatedEvent;
import com.mentoredu.forum.event.ReactionCreatedEvent;
import com.mentoredu.pedagogy.event.FeedbackGivenEvent;
import com.mentoredu.pedagogy.event.SolutionSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserService            userService;

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        try {
            userService.findById(event.threadAuthorId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("answer_received")
                        .payload(Map.of("answerId", event.answerId().toString(),
                                        "threadTitle", event.threadTitle()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting answer_received notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        try {
            userService.findById(event.answerAuthorId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("comment_received")
                        .payload(Map.of("commentId", event.commentId().toString(),
                                        "truncatedBody", event.truncatedBody()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting comment_received notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationProcessed(VerificationProcessedEvent event) {
        try {
            userService.findById(event.requesterId()).ifPresent(recipient -> {
                var payload = new HashMap<String, Object>();
                payload.put("requestId",  event.requestId().toString());
                payload.put("entityType", event.entityType());
                payload.put("status",     event.status().name());
                if (event.notes() != null) payload.put("notes", event.notes());
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("verification_processed").payload(payload).build());
            });
        } catch (DataAccessException e) {
            log.error("Error persisting verification_processed notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssociationResolved(AssociationResolvedEvent event) {
        try {
            userService.findById(event.teacherUserId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("association_resolved")
                        .payload(Map.of("linkId", event.linkId().toString(),
                                        "academyProfileId", event.academyProfileId().toString(),
                                        "status", event.status().name()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting association_resolved notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserFollowed(UserFollowedEvent event) {
        try {
            userService.findById(event.followedId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("new_follower")
                        .payload(Map.of("followerId", event.followerId().toString()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting new_follower notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReactionCreated(ReactionCreatedEvent event) {
        try {
            userService.findById(event.contentAuthorId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("reaction_received")
                        .payload(Map.of("reactorId", event.reactorId().toString(),
                                        "targetType", event.targetType(),
                                        "targetId", event.targetId().toString(),
                                        "reactionType", event.reactionType()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting reaction_received notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSolutionSubmitted(SolutionSubmittedEvent event) {
        try {
            userService.findById(event.exerciseAuthorId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("solution_submitted")
                        .payload(Map.of("solutionId", event.solutionId().toString(),
                                        "resourceId", event.resourceId().toString(),
                                        "studentId",  event.studentId().toString()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting solution_submitted notification for event {}: {}", event, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackGiven(FeedbackGivenEvent event) {
        try {
            userService.findById(event.studentId()).ifPresent(recipient ->
                notificationRepository.save(Notification.builder()
                        .user(recipient).type("feedback_received")
                        .payload(Map.of("feedbackId", event.feedbackId().toString(),
                                        "solutionId", event.solutionId().toString(),
                                        "resourceId", event.resourceId().toString()))
                        .build())
            );
        } catch (DataAccessException e) {
            log.error("Error persisting feedback_received notification for event {}: {}", event, e.getMessage(), e);
        }
    }
}
