package com.mentoredu.academy.controller;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.service.IAcademyService;
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

@RestController
@RequestMapping("/api/v1/academies")
@RequiredArgsConstructor
@Tag(name = "Academy", description = "Gestión de academias de preparación.")
@SecurityRequirement(name = "bearerAuth")
public class AcademyController {

    private final IAcademyService academyService;

    // -------------------------------------------------------------------------
    // US33 — Create academy
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US33 - Crear academia",
        description = "Registra una nueva academia para la organización autenticada. "
            + "Requiere perfil de tipo ORGANIZATION (US04) y perfil de organización creado (US10). "
            + "El nombre de la academia debe ser único por organización."
    )
    public ResponseEntity<AcademyResponse> createAcademy(
            @Valid @RequestBody CreateAcademyRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(academyService.createAcademy(auth.getName(), request));
    }
}
