package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.exception.UserNotFoundException;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.event.ReactionCreatedEvent;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Reaction;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.forum.repository.ReactionRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionService implements IReactionService {

    private static final String THREAD  = "THREAD";
    private static final String ANSWER  = "ANSWER";
    private static final String COMMENT = "COMMENT";

    private final ReactionRepository reactionRepository;
    private final ThreadRepository   threadRepository;
    private final AnswerRepository   answerRepository;
    private final CommentRepository  commentRepository;
    private final UserRepository     userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToThread(UUID threadId, CreateReactionRequest request, String userEmail) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Hilo no encontrado: " + threadId));
        var reactor = loadUser(userEmail);
        Optional<ReactionResponse> result = toggle(reactor, THREAD, threadId, request.reactionType());
        if (result.isPresent() && !reactor.getId().equals(thread.getAuthor().getId())) {
            eventPublisher.publishEvent(new ReactionCreatedEvent(
                    reactor.getId(), thread.getAuthor().getId(), THREAD, threadId, request.reactionType()));
        }
        return result;
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToAnswer(UUID answerId, CreateReactionRequest request, String userEmail) {
        var answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Respuesta no encontrada: " + answerId));
        var reactor = loadUser(userEmail);
        Optional<ReactionResponse> result = toggle(reactor, ANSWER, answerId, request.reactionType());
        if (result.isPresent() && !reactor.getId().equals(answer.getAuthor().getId())) {
            eventPublisher.publishEvent(new ReactionCreatedEvent(
                    reactor.getId(), answer.getAuthor().getId(), ANSWER, answerId, request.reactionType()));
        }
        return result;
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToComment(UUID commentId, CreateReactionRequest request, String userEmail) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentario no encontrado: " + commentId));
        var reactor = loadUser(userEmail);
        Optional<ReactionResponse> result = toggle(reactor, COMMENT, commentId, request.reactionType());
        if (result.isPresent() && !reactor.getId().equals(comment.getAuthor().getId())) {
            eventPublisher.publishEvent(new ReactionCreatedEvent(
                    reactor.getId(), comment.getAuthor().getId(), COMMENT, commentId, request.reactionType()));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Toggle: same type → remove; different type → replace; none → create
    // -------------------------------------------------------------------------

    private Optional<ReactionResponse> toggle(User user, String targetType, UUID targetId, String reactionType) {
        Optional<Reaction> existing = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);

        if (existing.isPresent()) {
            Reaction r = existing.get();
            if (r.getReactionType().equals(reactionType)) {
                reactionRepository.delete(r);
                return Optional.empty();
            }
            r.setReactionType(reactionType);
            return Optional.of(toResponse(reactionRepository.save(r)));
        }

        Reaction created = reactionRepository.save(Reaction.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .reactionType(reactionType)
                .build());
        return Optional.of(toResponse(created));
    }

    private User loadUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + email));
    }

    private ReactionResponse toResponse(Reaction r) {
        return ReactionResponse.builder()
                .id(r.getId())
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .reactionType(r.getReactionType())
                .userId(r.getUser().getId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
