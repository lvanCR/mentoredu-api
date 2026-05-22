package com.mentoredu.verification.controller;

import com.mentoredu.verification.dto.ProcessVerificationRequest;
import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;
import com.mentoredu.verification.service.IVerificationService;
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
@RequestMapping("/api/v1/verification/requests")
@RequiredArgsConstructor
@Tag(name = "Verificación", description = "Solicitudes de verificación de identidad para docentes (US21) y academias (US22).")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final IVerificationService verificationService;

    // -------------------------------------------------------------------------
    // US21 / US22 — Submit verification request
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US21/US22 - Enviar solicitud de verificación",
        description = "Registra una solicitud de verificación con documento adjunto. "
            + "entityType acepta: TEACHER (US21) o ACADEMY (US22). "
            + "El rol del usuario debe coincidir con el entityType solicitado (RN-25). "
            + "Solo puede existir una solicitud PENDING por entidad (RN-24). "
            + "El documento es obligatorio (RN-23). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<VerificationRequestResponse> submit(@Valid @RequestBody SubmitVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(verificationService.submit(auth.getName(), request));
    }

    // -------------------------------------------------------------------------
    // US21 / US22 — Get own verification requests
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @Operation(
        summary = "US21/US22 - Consultar mis solicitudes de verificación",
        description = "Devuelve el historial de solicitudes de verificación del usuario autenticado, "
            + "ordenadas por fecha descendente. Lista vacía si no hay solicitudes. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<List<VerificationRequestResponse>> getMyRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(verificationService.getMyRequests(auth.getName()));
    }

    // -------------------------------------------------------------------------
    // F3.1 — Process verification request (MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @PatchMapping("/{requestId}/process")
    @Operation(
        summary = "F3.1 - Procesar solicitud de verificación",
        description = "Aprueba o rechaza una solicitud de verificación pendiente. "
            + "Solo moderadores y administradores pueden ejecutar esta acción. "
            + "status acepta: VERIFIED o REJECTED. "
            + "Notifica al solicitante tras el commit de la transacción. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<VerificationRequestResponse> processRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ProcessVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(verificationService.processRequest(requestId, request.getStatus(), auth.getName()));
    }
}
