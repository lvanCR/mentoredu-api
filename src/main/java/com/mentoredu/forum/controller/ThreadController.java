package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.service.IThreadService;
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
@RequestMapping("/api/v1/threads")
@RequiredArgsConstructor
@Tag(name = "Foro", description = "Gestión de hilos de discusión (EP-05).")
@SecurityRequirement(name = "bearerAuth")
public class ThreadController {

    private final IThreadService threadService;

    // -------------------------------------------------------------------------
    // US16 — Create forum thread
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US16 - Crear hilo de foro",
        description = "Crea un hilo de discusión sobre una duda concreta. "
            + "El campo subjectId es obligatorio (RN-16): el hilo debe asociarse a un tema específico. "
            + "Si anonymous=true, el autor se muestra como 'Anónimo' en las respuestas, "
            + "pero el sistema conserva author_user_id internamente para moderación (RN-04-trazabilidad). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<ThreadResponse> create(@Valid @RequestBody CreateThreadRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(threadService.create(request, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US16 — List recent threads (read, paginated)
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "US16 - Listar hilos recientes",
        description = "Devuelve los hilos de discusión ordenados del más reciente al más antiguo. "
            + "Parámetros de paginación: page (default 0) y size (default 10). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<List<ThreadResponse>> listRecent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(threadService.listRecent(page, size));
    }

    // -------------------------------------------------------------------------
    // US16 — Get thread by ID
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
        summary = "US16 - Obtener hilo por ID",
        description = "Devuelve los datos de un hilo de discusión existente. "
            + "Devuelve 404 si el hilo no existe. Requiere autenticación JWT."
    )
    public ResponseEntity<ThreadResponse> getById(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(threadService.get(id));
    }
}
