package com.mentoredu.profile.controller;

import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.service.IProfileService;
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
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gestión de perfiles de usuario.")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final IProfileService profileService;

    // -------------------------------------------------------------------------
    // US04 — Select account type
    // -------------------------------------------------------------------------

    @PostMapping("/account-type")
    @Operation(summary = "US04 - Seleccionar tipo de cuenta",
               description = "Crea el perfil base del usuario autenticado con el tipo de cuenta elegido (STUDENT, TEACHER, ORGANIZATION). Solo puede ejecutarse una vez por usuario.")
    public ResponseEntity<ProfileResponse> selectAccountType(@Valid @RequestBody SelectAccountTypeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.selectAccountType(auth.getName(), request));
    }

    // -------------------------------------------------------------------------
    // Pre-existing skeleton endpoints (not yet implementing a US)
    // -------------------------------------------------------------------------

    @GetMapping("/student/{userId}")
    @Operation(summary = "Obtener perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getStudentProfile(userId));
    }

    @PutMapping("/student/{userId}")
    @Operation(summary = "Actualizar perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(profileService.updateStudentProfile(userId, request));
    }
}
