package com.mentoredu.forum.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.service.IAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/threads")
@RequiredArgsConstructor
public class AnswerController {

    private final IAnswerService answerService;

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    @PostMapping("/{threadId}/answers")
    public ResponseEntity<AnswerResponse> create(
            @PathVariable UUID threadId,
            @Valid @RequestBody CreateAnswerRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(answerService.create(threadId, request, auth.getName()));
    }

    @GetMapping("/{threadId}/answers")
    public ResponseEntity<PagedResponse<AnswerResponse>> list(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(answerService.listByThread(threadId, page, size));
    }
}
