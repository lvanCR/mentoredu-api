package com.mentoredu.forum.service;

import com.mentoredu.auth.service.UserService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.ThreadStatus;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.ReactionRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService implements IAnswerService {

    private static final String ANSWER = "ANSWER";

    private final AnswerRepository       answerRepository;
    private final ThreadRepository       threadRepository;
    private final ReactionRepository     reactionRepository;
    private final UserService            userService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AnswerResponse create(UUID threadId, CreateAnswerRequest request, String authorEmail) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Hilo no encontrado: " + threadId));

        if (thread.getStatus() == ThreadStatus.CLOSED) {
            throw new ThreadClosedException("El hilo está cerrado y no acepta nuevas respuestas: " + threadId);
        }

        var user = userService.findByEmailOrThrow(authorEmail);

        Answer answer = Answer.builder()
                .thread(thread)
                .author(user)
                .body(request.body())
                .isAccepted(false)
                .build();

        Answer saved = answerRepository.save(answer);

        if (!thread.getAuthor().getId().equals(user.getId())) {
            eventPublisher.publishEvent(new AnswerCreatedEvent(
                    saved.getId(), thread.getAuthor().getId(), user.getId(), thread.getTitle()));
        }
        return toResponse(saved, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AnswerResponse> listByThread(UUID threadId, int page, int size, String currentUserEmail) {
        if (!threadRepository.existsById(threadId)) {
            throw new ThreadNotFoundException("Thread not found: " + threadId);
        }
        UUID userId = userService.findByEmailOrThrow(currentUserEmail).getId();
        return PagedResponse.from(
                answerRepository.findAllByThread_IdOrderByCreatedAtAsc(threadId, PagedResponse.toPageRequest(page, size)),
                a -> toResponse(a, userId));
    }

    private AnswerResponse toResponse(Answer a, UUID currentUserId) {
        int likes    = (int) reactionRepository.countByTargetTypeAndTargetIdAndReactionType(ANSWER, a.getId(), "LIKE");
        int dislikes = (int) reactionRepository.countByTargetTypeAndTargetIdAndReactionType(ANSWER, a.getId(), "DISLIKE");
        String myReaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(currentUserId, ANSWER, a.getId())
                .map(r -> r.getReactionType())
                .orElse(null);

        return AnswerResponse.builder()
                .id(a.getId())
                .threadId(a.getThread().getId())
                .body(a.getBody())
                .accepted(Boolean.TRUE.equals(a.getIsAccepted()))
                .authorId(a.getAuthor().getId())
                .authorDisplay(a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName())
                .likeCount(likes)
                .dislikeCount(dislikes)
                .myReaction(myReaction)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
