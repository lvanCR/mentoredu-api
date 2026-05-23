package com.mentoredu.pedagogy.controller;

import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.service.ISolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class SolutionController {

    private final ISolutionService solutionService;

    @PostMapping("/{resourceId}/solutions")
    public ResponseEntity<SolutionResponse> submit(
            @PathVariable UUID resourceId,
            @Valid @RequestBody SubmitSolutionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(solutionService.submit(resourceId, request, auth.getName()));
    }

    @GetMapping("/{resourceId}/solutions/mine")
    public ResponseEntity<MySolutionWithFeedbackResponse> getMine(@PathVariable UUID resourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(solutionService.getMyWithFeedback(resourceId, auth.getName()));
    }

    @GetMapping("/{resourceId}/solutions")
    public ResponseEntity<List<SolutionResponse>> list(@PathVariable UUID resourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(solutionService.listByResource(resourceId, auth.getName()));
    }
}
