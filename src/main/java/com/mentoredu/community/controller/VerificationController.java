package com.mentoredu.community.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.service.IVerificationService;
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
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "Verificación", description = "Solicitudes de verificación de identidad (US22, US23)")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final IVerificationService verificationService;

    // -------------------------------------------------------------------------
    // US22 — Solicitar verificación
    // -------------------------------------------------------------------------

    @PostMapping("/requests")
    @Operation(summary = "US22 - Solicitar verificación de identidad")
    public ResponseEntity<VerificationResponse> submit(@Valid @RequestBody CreateVerificationRequest request) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(verificationService.submit(request, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US22 — Listar mis solicitudes
    // -------------------------------------------------------------------------

    @GetMapping("/requests/me")
    @Operation(summary = "US22 - Ver mis solicitudes de verificación")
    public ResponseEntity<List<VerificationResponse>> myRequests() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(verificationService.getMyRequests(auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US23 — Listar solicitudes pendientes (MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @GetMapping("/requests")
    @Operation(summary = "US23 - Listar todas las solicitudes de verificación (moderadores)")
    public ResponseEntity<PagedResponse<VerificationResponse>> allRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!hasModeratorRole(auth)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(verificationService.getAllRequests(page, size));
    }

    // -------------------------------------------------------------------------
    // US23 — Aprobar o rechazar verificación (MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @PatchMapping("/requests/{id}/review")
    @Operation(summary = "US23 - Aprobar o rechazar solicitud de verificación")
    public ResponseEntity<VerificationResponse> review(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewVerificationRequest request) {

        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!hasModeratorRole(auth)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(verificationService.review(id, request, auth.getName()));
    }

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    private boolean hasModeratorRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")
                            || a.getAuthority().equals("ROLE_ADMIN"));
    }
}
