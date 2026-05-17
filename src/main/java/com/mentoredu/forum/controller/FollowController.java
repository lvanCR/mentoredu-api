package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.FollowResponse;
import com.mentoredu.forum.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Foro — Seguimiento", description = "Seguimiento entre usuarios (US29).")
@SecurityRequirement(name = "bearerAuth")
public class FollowController {

    private final IFollowService followService;

    // -------------------------------------------------------------------------
    // US29 — Follow a user (toggle)
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/users/{targetUserId}/follow")
    @Operation(
        summary = "US29 - Seguir / dejar de seguir a un usuario",
        description = "Comportamiento toggle: si el usuario autenticado aún no sigue al objetivo, "
            + "crea la relación y devuelve 201 con los datos del usuario seguido. "
            + "Si ya lo sigue, elimina la relación y devuelve 204. "
            + "Devuelve 400 si se intenta seguir a uno mismo. "
            + "Devuelve 404 si el usuario objetivo no existe. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<FollowResponse> toggleFollow(@PathVariable UUID targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FollowResponse> result = followService.toggleFollow(targetUserId, auth.getName());
        return result.map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // -------------------------------------------------------------------------
    // US29 — List following
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/users/{userId}/following")
    @Operation(
        summary = "US29 - Listar usuarios que sigue un usuario",
        description = "Devuelve la lista de usuarios que el usuario identificado por userId sigue. "
            + "Devuelve 404 si el usuario no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    // -------------------------------------------------------------------------
    // US29 — List followers
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/users/{userId}/followers")
    @Operation(
        summary = "US29 - Listar seguidores de un usuario",
        description = "Devuelve la lista de usuarios que siguen al usuario identificado por userId. "
            + "Devuelve 404 si el usuario no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(followService.getFollowers(userId));
    }
}
