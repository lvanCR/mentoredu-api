package com.mentoredu.community.controller;

import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.service.IAssociationService;
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
@RequestMapping("/api/v1/associations")
@RequiredArgsConstructor
@Tag(name = "Asociaciones", description = "Vinculación docente-academia (US24)")
@SecurityRequirement(name = "bearerAuth")
public class AssociationController {

    private final IAssociationService associationService;

    @PostMapping("/teacher-academy")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "US24 - Docente solicita asociarse a una academia")
    public ResponseEntity<AssociationResponse> request(@Valid @RequestBody CreateAssociationRequest request) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(associationService.requestAssociation(request, auth.getName()));
    }

    @GetMapping("/teacher-academy/me")
    @Operation(summary = "US24 - Ver mis asociaciones (como docente)")
    public ResponseEntity<List<AssociationResponse>> myAssociations() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(associationService.getMyAssociations(auth.getName()));
    }

    @GetMapping("/teacher-academy/academy")
    @Operation(summary = "US24 - Ver solicitudes de asociación recibidas (como academia)")
    public ResponseEntity<List<AssociationResponse>> academyRequests() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(associationService.getAcademyRequests(auth.getName()));
    }

    @PatchMapping("/teacher-academy/{id}/accept")
    @Operation(summary = "US24 - Academia acepta solicitud de asociación")
    public ResponseEntity<AssociationResponse> accept(@PathVariable UUID id) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(associationService.acceptAssociation(id, auth.getName()));
    }

    @PatchMapping("/teacher-academy/{id}/reject")
    @Operation(summary = "US24 - Academia rechaza solicitud de asociación")
    public ResponseEntity<AssociationResponse> reject(@PathVariable UUID id) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(associationService.rejectAssociation(id, auth.getName()));
    }

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }
}
