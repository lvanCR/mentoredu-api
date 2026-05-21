package com.mentoredu.forum.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.model.Thread;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThreadService implements IThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // US16 — Create forum thread
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse create(CreateThreadRequest request, String authorEmail) {
        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        Thread thread = Thread.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .subjectId(request.getSubjectId())
                .anonymous(request.isAnonymous())
                .author(user)
                .build();

        return toResponse(threadRepository.save(thread));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThreadResponse> listRecent(int page, int size) {
        return threadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ThreadResponse get(UUID id) {
        return threadRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + id));
    }

    // -------------------------------------------------------------------------
    // US18 — Close forum thread (RN-17: only OPEN/CLOSED states; RN-18: author only)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse close(UUID id, String requesterEmail) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + id));

        if (!thread.getAuthor().getEmail().equals(requesterEmail)) {
            throw new ThreadNotOwnedException("Only the author can close this thread: " + id);
        }

        if ("CLOSED".equals(thread.getStatus())) {
            throw new ThreadClosedException("Thread is already closed: " + id);
        }

        thread.setStatus("CLOSED");
        return toResponse(threadRepository.save(thread));
    }

    // -------------------------------------------------------------------------
    // Mapping (RN-16: anonymous threads show "Anónimo" but store author internally)
    // -------------------------------------------------------------------------

    private ThreadResponse toResponse(Thread t) {
        String display = Boolean.TRUE.equals(t.getAnonymous())
                ? "Anónimo"
                : t.getAuthor().getFirstName() + " " + t.getAuthor().getLastName();

        return ThreadResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .body(t.getBody())
                .anonymous(Boolean.TRUE.equals(t.getAnonymous()))
                .authorDisplay(display)
                .subjectId(t.getSubjectId())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
