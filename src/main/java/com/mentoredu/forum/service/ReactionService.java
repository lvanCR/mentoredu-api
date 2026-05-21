package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.model.Reaction;
import com.mentoredu.forum.model.Thread;
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

    private final ReactionRepository reactionRepository;
    private final ThreadRepository threadRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // US27 — React to forum content (toggle behavior per RN-19 spirit)
    //
    // Toggle rule:
    //   - Same reactionType already exists → delete (returns Optional.empty)
    //   - Different reactionType exists     → replace (returns Optional.of updated)
    //   - No existing reaction              → create  (returns Optional.of new)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToThread(UUID threadId, CreateReactionRequest request, String userEmail) {
        User user = loadUser(userEmail);
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + threadId));

        Optional<Reaction> existing = reactionRepository.findByUserAndThread(user, thread);
        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getReactionType().equals(request.getReactionType())) {
                reactionRepository.delete(reaction);
                return Optional.empty();
            }
            reaction.setReactionType(request.getReactionType());
            return Optional.of(toResponse(reactionRepository.save(reaction), "THREAD", threadId, user.getId()));
        }

        Reaction created = reactionRepository.save(Reaction.builder()
                .user(user)
                .thread(thread)
                .reactionType(request.getReactionType())
                .build());
        return Optional.of(toResponse(created, "THREAD", threadId, user.getId()));
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToAnswer(UUID answerId, CreateReactionRequest request, String userEmail) {
        User user = loadUser(userEmail);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Answer not found: " + answerId));

        Optional<Reaction> existing = reactionRepository.findByUserAndAnswer(user, answer);
        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getReactionType().equals(request.getReactionType())) {
                reactionRepository.delete(reaction);
                return Optional.empty();
            }
            reaction.setReactionType(request.getReactionType());
            return Optional.of(toResponse(reactionRepository.save(reaction), "ANSWER", answerId, user.getId()));
        }

        Reaction created = reactionRepository.save(Reaction.builder()
                .user(user)
                .answer(answer)
                .reactionType(request.getReactionType())
                .build());
        return Optional.of(toResponse(created, "ANSWER", answerId, user.getId()));
    }

    @Override
    @Transactional
    public Optional<ReactionResponse> reactToComment(UUID commentId, CreateReactionRequest request, String userEmail) {
        User user = loadUser(userEmail);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + commentId));

        Optional<Reaction> existing = reactionRepository.findByUserAndComment(user, comment);
        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getReactionType().equals(request.getReactionType())) {
                reactionRepository.delete(reaction);
                return Optional.empty();
            }
            reaction.setReactionType(request.getReactionType());
            return Optional.of(toResponse(reactionRepository.save(reaction), "COMMENT", commentId, user.getId()));
        }

        Reaction created = reactionRepository.save(Reaction.builder()
                .user(user)
                .comment(comment)
                .reactionType(request.getReactionType())
                .build());
        return Optional.of(toResponse(created, "COMMENT", commentId, user.getId()));
    }

    private User loadUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private ReactionResponse toResponse(Reaction r, String targetType, UUID targetId, UUID userId) {
        return ReactionResponse.builder()
                .id(r.getId())
                .targetType(targetType)
                .targetId(targetId)
                .reactionType(r.getReactionType())
                .userId(userId)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
