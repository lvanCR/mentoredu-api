package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;
import com.mentoredu.forum.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
@Tag(name = "Foro — Comentarios", description = "Comentarios sobre respuestas del foro (US15).")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final ICommentService commentService;

    // -------------------------------------------------------------------------
    // US15 — Comment on forum answer
    // -------------------------------------------------------------------------

    @PostMapping("/{answerId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "US15 - Comentar una respuesta del foro",
        description = "Publica un comentario sobre una respuesta existente. "
            + "El body no puede estar vacío. "
            + "Devuelve 404 si la respuesta no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID answerId,
            @Valid @RequestBody CreateCommentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(answerId, request, auth.getName()));
    }

    @GetMapping("/{answerId}/comments")
    @Operation(
        summary = "US15 - Listar comentarios de una respuesta",
        description = "Devuelve todos los comentarios de una respuesta ordenados del más antiguo al más reciente. "
            + "Devuelve 404 si la respuesta no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<List<CommentResponse>> listByAnswer(@PathVariable UUID answerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(commentService.listByAnswer(answerId));
    }
}
