package com.mentoredu.academy.controller;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CampusResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.dto.CreateCampusRequest;
import com.mentoredu.academy.dto.CreateCycleRequest;
import com.mentoredu.academy.dto.CreateProgramRequest;
import com.mentoredu.academy.dto.CycleResponse;
import com.mentoredu.academy.dto.ProgramResponse;
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

import java.util.UUID;

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

    // -------------------------------------------------------------------------
    // US11 — Register academic offering: program
    // -------------------------------------------------------------------------

    @PostMapping("/{academyId}/programs")
    @Operation(
        summary = "US11 - Registrar programa académico",
        description = "Registra un nuevo programa de preparación dentro de una academia. "
            + "El nombre del programa debe ser único por academia. "
            + "Requiere ser la organización propietaria de la academia. "
            + "Valores sugeridos para modality: PRESENCIAL, VIRTUAL, HIBRIDO. "
            + "Valores sugeridos para intensity: NORMAL, INTENSIVO."
    )
    public ResponseEntity<ProgramResponse> createProgram(
            @PathVariable UUID academyId,
            @Valid @RequestBody CreateProgramRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(academyService.createProgram(auth.getName(), academyId, request));
    }

    // -------------------------------------------------------------------------
    // US11 — Register academic offering: cycle
    // -------------------------------------------------------------------------

    @PostMapping("/{academyId}/cycles")
    @Operation(
        summary = "US11 - Registrar ciclo académico",
        description = "Registra un nuevo ciclo de estudio dentro de una academia. "
            + "El nombre del ciclo debe ser único por academia. "
            + "Requiere ser la organización propietaria de la academia. "
            + "endDate debe ser posterior a startDate. "
            + "Valores sugeridos para cycleType: REGULAR, INTENSIVO, VACACIONAL."
    )
    public ResponseEntity<CycleResponse> createCycle(
            @PathVariable UUID academyId,
            @Valid @RequestBody CreateCycleRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(academyService.createCycle(auth.getName(), academyId, request));
    }

    // -------------------------------------------------------------------------
    // US36 — Register campus for academy
    // -------------------------------------------------------------------------

    @PostMapping("/{academyId}/campuses")
    @Operation(
        summary = "US36 - Registrar sede de academia",
        description = "Registra una nueva sede física para una academia. "
            + "El nombre de la sede debe ser único por academia (RN-39). "
            + "Requiere ser la organización propietaria de la academia (RN-40)."
    )
    public ResponseEntity<CampusResponse> createCampus(
            @PathVariable UUID academyId,
            @Valid @RequestBody CreateCampusRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(academyService.createCampus(auth.getName(), academyId, request));
    }
}
