package com.mentoredu.community.service;

import com.mentoredu.community.dto.CreateThreadRequest;
import com.mentoredu.community.dto.ThreadResponse;
import com.mentoredu.community.model.ThreadEntity;
import com.mentoredu.community.repository.ThreadRepository;
import com.mentoredu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        var user = userRepository.findByEmail(authorEmail).orElseThrow(() -> new RuntimeException("User not found"));
        ThreadEntity t = ThreadEntity.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .anonymous(request.isAnonymous())
                .author(user)
                .build();
        ThreadEntity saved = threadRepository.save(t);
        return toResponse(saved);
    }

    @Override
    public List<ThreadResponse> listRecent(int page, int size) {
        return threadRepository.findAll()
                .stream()
                .sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(size)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ThreadResponse get(UUID id) {
        return threadRepository.findById(id).map(this::toResponse).orElseThrow(() -> new RuntimeException("Thread not found"));
    }

    private ThreadResponse toResponse(ThreadEntity t) {
        var display = t.getAnonymous() ? "Anonymous" : t.getAuthor().getFirstName() + " " + t.getAuthor().getLastName();
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
