package com.mentoredu.forum.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService implements IAnswerService {

    private final AnswerRepository answerRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // US17 — Reply to forum thread
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AnswerResponse create(UUID threadId, CreateAnswerRequest request, String authorEmail) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + threadId));

        // RN-17: a closed thread does not accept new replies
        if ("CLOSED".equals(thread.getStatus())) {
            throw new ThreadClosedException("Thread is closed and does not accept new replies: " + threadId);
        }

        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        Answer answer = Answer.builder()
                .thread(thread)
                .author(user)
                .body(request.getBody())
                .isAccepted(false)
                .build();

        return toResponse(answerRepository.save(answer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> listByThread(UUID threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw new ThreadNotFoundException("Thread not found: " + threadId);
        }
        return answerRepository.findAllByThread_IdOrderByCreatedAtAsc(threadId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private AnswerResponse toResponse(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .threadId(a.getThread().getId())
                .body(a.getBody())
                .accepted(Boolean.TRUE.equals(a.getIsAccepted()))
                .authorDisplay(a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
