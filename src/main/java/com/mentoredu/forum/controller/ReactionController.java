package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;
import com.mentoredu.forum.service.IReactionService;
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

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Foro — Reacciones", description = "Reacciones a hilos, respuestas y comentarios (US27).")
@SecurityRequirement(name = "bearerAuth")
public class ReactionController {

    private final IReactionService reactionService;

    // -------------------------------------------------------------------------
    // US27 — React to thread
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/threads/{id}/reactions")
    @Operation(
        summary = "US27 - Reaccionar a un hilo",
        description = "Registra o elimina la reacción del usuario autenticado sobre un hilo. "
            + "Comportamiento toggle: si el usuario ya tiene la misma reacción, se elimina (204). "
            + "Si es una reacción distinta, se reemplaza (200). Si no existe, se crea (201). "
            + "Devuelve 404 si el hilo no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<ReactionResponse> reactToThread(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReactionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<ReactionResponse> result = reactionService.reactToThread(id, request, auth.getName());
        return result.map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // -------------------------------------------------------------------------
    // US27 — React to answer
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/answers/{id}/reactions")
    @Operation(
        summary = "US27 - Reaccionar a una respuesta",
        description = "Registra o elimina la reacción del usuario autenticado sobre una respuesta. "
            + "Comportamiento toggle: misma reacción = elimina (204), distinta = reemplaza (201). "
            + "Devuelve 404 si la respuesta no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<ReactionResponse> reactToAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReactionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<ReactionResponse> result = reactionService.reactToAnswer(id, request, auth.getName());
        return result.map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // -------------------------------------------------------------------------
    // US27 — React to comment
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/comments/{id}/reactions")
    @Operation(
        summary = "US27 - Reaccionar a un comentario",
        description = "Registra o elimina la reacción del usuario autenticado sobre un comentario. "
            + "Comportamiento toggle: misma reacción = elimina (204), distinta = reemplaza (201). "
            + "Devuelve 404 si el comentario no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<ReactionResponse> reactToComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReactionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<ReactionResponse> result = reactionService.reactToComment(id, request, auth.getName());
        return result.map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
