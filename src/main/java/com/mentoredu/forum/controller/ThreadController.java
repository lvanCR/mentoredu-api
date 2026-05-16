package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.service.IThreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final IThreadService threadService;

    @PostMapping
    public ResponseEntity<ThreadResponse> create(@Valid @RequestBody CreateThreadRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(201).body(threadService.create(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ThreadResponse>> recent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(threadService.listRecent(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreadResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.get(id));
    }
}
