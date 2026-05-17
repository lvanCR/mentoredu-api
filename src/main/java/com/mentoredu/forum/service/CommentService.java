package com.mentoredu.forum.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // US28 — Comment on forum answer
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public CommentResponse create(UUID answerId, CreateCommentRequest request, String authorEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Answer not found: " + answerId));

        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        Comment comment = Comment.builder()
                .answer(answer)
                .thread(answer.getThread())
                .author(user)
                .body(request.getBody())
                .build();

        return toResponse(commentRepository.save(comment));
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

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .answerId(c.getAnswer().getId())
                .threadId(c.getThread().getId())
                .body(c.getBody())
                .authorDisplay(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
