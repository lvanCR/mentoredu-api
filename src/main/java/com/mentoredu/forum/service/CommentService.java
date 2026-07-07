package com.mentoredu.forum.service;

import com.mentoredu.auth.service.UserService;
import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;
import com.mentoredu.forum.dto.UpdateBodyRequest;
import com.mentoredu.forum.event.CommentCreatedEvent;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserService    userService;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // US28 — Comment on forum answer
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public CommentResponse create(UUID answerId, CreateCommentRequest request, String authorEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Respuesta no encontrada: " + answerId));

        var user = userService.findByEmailOrThrow(authorEmail);

        Comment comment = Comment.builder()
                .answer(answer)
                .thread(answer.getThread())
                .author(user)
                .body(request.body())
                .build();

        Comment saved = commentRepository.save(comment);
        UUID answerAuthorId = answer.getAuthor().getId();
        if (!answerAuthorId.equals(user.getId())) {
            String truncated = request.body().length() > 50
                    ? request.body().substring(0, 50) + "..."
                    : request.body();
            eventPublisher.publishEvent(new CommentCreatedEvent(
                    saved.getId(), answer.getThread().getId(), answerAuthorId, user.getId(), truncated));
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listByAnswer(UUID answerId) {
        if (!answerRepository.existsById(answerId)) {
            throw new AnswerNotFoundException("Answer not found: " + answerId);
        }
        return commentRepository.findAllByAnswer_IdOrderByCreatedAtAsc(answerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse update(UUID answerId, UUID commentId, UpdateBodyRequest request, String requesterEmail) {
        Comment comment = findCommentInAnswer(answerId, commentId);
        var requester = userService.findByEmailOrThrow(requesterEmail);
        requireCommentOwnerOrAdmin(comment, requester);
        comment.setBody(request.body().trim());
        return toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void delete(UUID answerId, UUID commentId, String requesterEmail) {
        Comment comment = findCommentInAnswer(answerId, commentId);
        var requester = userService.findByEmailOrThrow(requesterEmail);
        requireCommentOwnerOrAdmin(comment, requester);
        commentRepository.delete(comment);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .answerId(c.getAnswer().getId())
                .threadId(c.getThread().getId())
                .body(c.getBody())
                .authorId(c.getAuthor().getId())
                .authorDisplay(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName())
                .authorAvatarUrl(avatarUrl(c.getAuthor().getId()))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private String avatarUrl(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(profile -> profile.getAvatarUrl())
                .orElse(null);
    }

    private Comment findCommentInAnswer(UUID answerId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentario no encontrado: " + commentId));
        if (comment.getAnswer() == null || !comment.getAnswer().getId().equals(answerId)) {
            throw new CommentNotFoundException("El comentario no pertenece a esta respuesta");
        }
        return comment;
    }

    private void requireCommentOwnerOrAdmin(Comment comment, com.mentoredu.auth.entity.User requester) {
        boolean owner = comment.getAuthor().getId().equals(requester.getId());
        boolean admin = "ADMIN".equals(requester.getRole().getName());
        if (!owner && !admin) {
            throw new ThreadNotOwnedException("Solo el autor o un administrador puede modificar este comentario");
        }
    }

}
