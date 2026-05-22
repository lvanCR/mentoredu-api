package com.mentoredu.community.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.event.CommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        userRepository.findById(event.threadAuthorId()).ifPresent(recipient ->
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .type("ANSWER_RECEIVED")
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
                    .type("COMMENT_RECEIVED")
                    .payload(Map.of(
                        "commentId",    event.commentId().toString(),
                        "truncatedBody", event.truncatedBody()
                    ))
                    .build())
        );
    }
}
