package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.model.VerificationDoc;
import com.mentoredu.community.model.VerificationRequest;
import com.mentoredu.community.repository.VerificationDocRepository;
import com.mentoredu.community.repository.VerificationRequestRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "Verificación", description = "Solicitudes de verificación de identidad (US22, US23)")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final VerificationRequestRepository verificationRepository;
    private final VerificationDocRepository     docRepository;
    private final UserRepository                userRepository;

    // -------------------------------------------------------------------------
    // US22 — Solicitar verificación
    // -------------------------------------------------------------------------

    @PostMapping("/requests")
    @Operation(summary = "US22 - Solicitar verificación de identidad")
    public ResponseEntity<VerificationResponse> submit(@Valid @RequestBody CreateVerificationRequest request) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (verificationRepository.existsByUserIdAndStatus(user.getId(), "PENDING")) {
            throw new DuplicateVerificationException("Ya tienes una solicitud de verificación pendiente");
        }

        VerificationRequest vr = VerificationRequest.builder()
                .user(user)
                .entityType(request.getEntityType())
                .status("PENDING")
                .build();

        VerificationRequest saved = verificationRepository.save(vr);

        // Persist docs linked to the saved request
        List<VerificationDoc> docs = request.getDocuments().stream()
                .map(d -> VerificationDoc.builder()
                        .request(saved)
                        .documentType(d.getDocumentType())
                        .fileUrl(d.getFileUrl())
                        .build())
                .toList();
        docRepository.saveAll(docs);

        // Reload to include docs in response
        VerificationRequest withDocs = verificationRepository.findById(saved.getId())
                .orElseThrow();

        return ResponseEntity.status(HttpStatus.CREATED).body(new VerificationResponse(withDocs));
    }

    // -------------------------------------------------------------------------
    // US22 — Listar mis solicitudes
    // -------------------------------------------------------------------------

    @GetMapping("/requests/me")
    @Operation(summary = "US22 - Ver mis solicitudes de verificación")
    public ResponseEntity<List<VerificationResponse>> myRequests() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        return ResponseEntity.ok(
                verificationRepository.findByUserId(user.getId())
                        .stream().map(VerificationResponse::new).toList()
        );
    }

    // -------------------------------------------------------------------------
    // US23 — Listar solicitudes pendientes (MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @GetMapping("/requests")
    @Operation(summary = "US23 - Listar todas las solicitudes de verificación (moderadores)")
    public ResponseEntity<List<VerificationResponse>> allRequests() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!hasModeratorRole(auth)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(
                verificationRepository.findAll()
                        .stream().map(VerificationResponse::new).toList()
        );
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

        VerificationRequest vr = verificationRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException("Solicitud no encontrada: " + id));

        if (!"PENDING".equals(vr.getStatus())) {
            throw new DuplicateVerificationException("La solicitud ya fue procesada con estado: " + vr.getStatus());
        }

        // RN-17: el rechazo requiere notas obligatorias
        if ("REJECTED".equals(request.getAction()) &&
                (request.getNotes() == null || request.getNotes().isBlank())) {
            throw new IllegalArgumentException("El rechazo requiere una razón (notes) obligatoria (RN-17)");
        }

        User reviewer = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        vr.setStatus(request.getAction());
        vr.setNotes(request.getNotes());
        vr.setReviewedBy(reviewer);
        vr.setReviewedAt(LocalDateTime.now());

        return ResponseEntity.ok(new VerificationResponse(verificationRepository.save(vr)));
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
