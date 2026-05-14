package com.mentoredu.community.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.CreateThreadRequest;
import com.mentoredu.community.dto.ThreadResponse;
import com.mentoredu.community.model.Thread;
import com.mentoredu.community.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreadService implements IThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;

    @Override
    public ThreadResponse create(CreateThreadRequest request, String authorEmail) {
        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Thread thread = Thread.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .anonymous(request.isAnonymous())
                .author(user)
                .build();
        return toResponse(threadRepository.save(thread));
    }

    @Override
    public List<ThreadResponse> listRecent(int page, int size) {
        return threadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ThreadResponse get(UUID id) {
        return threadRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Thread no encontrado"));
    }

    private ThreadResponse toResponse(Thread t) {
        String display = Boolean.TRUE.equals(t.getAnonymous())
                ? "Anónimo"
                : t.getAuthor().getFirstName() + " " + t.getAuthor().getLastName();
        return ThreadResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .body(t.getBody())
                .anonymous(t.getAnonymous())
                .authorDisplay(display)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
