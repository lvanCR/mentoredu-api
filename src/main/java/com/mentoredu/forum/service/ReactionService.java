package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Reaction;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.forum.repository.ReactionRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToThread(UUID threadId, CreateReactionRequest request, String userEmail) {
        threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + threadId));
        return toggle(loadUser(userEmail), THREAD, threadId, request.getReactionType());
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToAnswer(UUID answerId, CreateReactionRequest request, String userEmail) {
        answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Answer not found: " + answerId));
        return toggle(loadUser(userEmail), ANSWER, answerId, request.getReactionType());
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToComment(UUID commentId, CreateReactionRequest request, String userEmail) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + commentId));
        return toggle(loadUser(userEmail), COMMENT, commentId, request.getReactionType());
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
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
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
