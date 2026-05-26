package com.mentoredu.community.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.model.VerificationStatus;
import com.mentoredu.community.service.IVerificationService;
import com.mentoredu.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "Verificación", description = "Solicitudes de verificación de identidad (US22, US23)")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final IVerificationService verificationService;

    @PostMapping("/requests")
    @Operation(summary = "US22 - Solicitar verificación de identidad")
    public ResponseEntity<VerificationResponse> submit(@Valid @RequestBody CreateVerificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(verificationService.submit(request, SecurityUtils.currentEmail()));
    }

    @GetMapping("/requests/me")
    @Operation(summary = "US22 - Ver mis solicitudes de verificación")
    public ResponseEntity<PagedResponse<VerificationResponse>> myRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(verificationService.getMyRequests(SecurityUtils.currentEmail(), page, size));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "US23 - Listar todas las solicitudes de verificación (moderadores). Filtro opcional: ?status=PENDING|APPROVED|REJECTED")
    public ResponseEntity<PagedResponse<VerificationResponse>> allRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    VerificationStatus status) {
        SecurityUtils.requireAnyRole("MODERATOR", "ADMIN");
        return ResponseEntity.ok(verificationService.getAllRequests(status, page, size));
    }

    @PatchMapping("/requests/{id}/review")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "US23 - Aprobar o rechazar solicitud de verificación")
    public ResponseEntity<VerificationResponse> review(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewVerificationRequest request) {
        SecurityUtils.requireAnyRole("MODERATOR", "ADMIN");
        return ResponseEntity.ok(verificationService.review(id, request, SecurityUtils.currentEmail()));
    }
}
