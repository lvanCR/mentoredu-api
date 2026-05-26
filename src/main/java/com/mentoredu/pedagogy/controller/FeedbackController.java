package com.mentoredu.pedagogy.controller;

import com.mentoredu.config.SecurityUtils;
import com.mentoredu.pedagogy.dto.CreateFeedbackRequest;
import com.mentoredu.pedagogy.dto.FeedbackResponse;
import com.mentoredu.pedagogy.service.IFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solutions")
@RequiredArgsConstructor
@Tag(name = "Pedagogía — Feedback", description = "Registrar y consultar feedback correctivo de resoluciones (US19).")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping("/{solutionId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "US19 - Dar feedback correctivo a una resolución")
    public ResponseEntity<FeedbackResponse> create(
            @PathVariable UUID solutionId,
            @Valid @RequestBody CreateFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(feedbackService.create(solutionId, request, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{solutionId}/feedback")
    @io.swagger.v3.oas.annotations.Operation(summary = "US19 - Obtener feedback de una resolución")
    public ResponseEntity<FeedbackResponse> get(@PathVariable UUID solutionId) {
        SecurityUtils.requireAuth();
        return ResponseEntity.ok(feedbackService.getBySolution(solutionId));
    }
}
