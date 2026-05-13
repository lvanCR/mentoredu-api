package com.mentoredu.community.controller;

import com.mentoredu.community.dto.CreateThreadRequest;
import com.mentoredu.community.dto.ThreadResponse;
import com.mentoredu.community.service.IThreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final IThreadService threadService;

    @PostMapping
    public ResponseEntity<ThreadResponse> create(@Valid @RequestBody CreateThreadRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        var resp = threadService.create(request, userDetails.getUsername());
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<ThreadResponse>> recent(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(threadService.listRecent(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreadResponse> get(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(threadService.get(id));
    }
}
