package com.mentoredu.forum.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.config.SecurityUtils;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.service.IAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/threads")
@RequiredArgsConstructor
@Tag(name = "Foro — Respuestas", description = "Publicar y listar respuestas en hilos del foro (US13).")
@SecurityRequirement(name = "bearerAuth")
public class AnswerController {

    private final IAnswerService answerService;

    @PostMapping("/{threadId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "US13 - Responder a un hilo de foro")
    public ResponseEntity<AnswerResponse> create(
            @PathVariable UUID threadId,
            @Valid @RequestBody CreateAnswerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(answerService.create(threadId, request, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{threadId}/answers")
    @io.swagger.v3.oas.annotations.Operation(summary = "US13 - Listar respuestas de un hilo")
    public ResponseEntity<PagedResponse<AnswerResponse>> list(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(answerService.listByThread(threadId, page, size, SecurityUtils.currentEmail()));
    }
}
