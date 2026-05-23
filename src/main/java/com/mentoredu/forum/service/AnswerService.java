package com.mentoredu.forum.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.event.AnswerCreatedEvent;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService implements IAnswerService {

    private final AnswerRepository     answerRepository;
    private final ThreadRepository     threadRepository;
    private final UserRepository       userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AnswerResponse create(UUID threadId, CreateAnswerRequest request, String authorEmail) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + threadId));

        if ("CLOSED".equals(thread.getStatus())) {
            throw new ThreadClosedException("Thread is closed and does not accept new replies: " + threadId);
        }

        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

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
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AnswerResponse> listByThread(UUID threadId, int page, int size) {
        if (!threadRepository.existsById(threadId)) {
            throw new ThreadNotFoundException("Thread not found: " + threadId);
        }
        return PagedResponse.from(
                answerRepository.findAllByThread_IdOrderByCreatedAtAsc(threadId, PageRequest.of(page, size)),
                this::toResponse);
    }

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
