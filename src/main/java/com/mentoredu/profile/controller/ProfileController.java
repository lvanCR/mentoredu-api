package com.mentoredu.profile.controller;

import com.mentoredu.profile.dto.CreateStudentProfileRequest;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateProfileRequest;
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
    // US05 — Update common profile data
    // -------------------------------------------------------------------------

    @PatchMapping("/me")
    @Operation(summary = "US05 - Actualizar datos comunes del perfil",
               description = "Actualiza displayName, avatarUrl, city y/o bio del perfil del usuario autenticado. El tipo de cuenta (profileType) no puede modificarse por este endpoint.")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profileService.updateProfile(auth.getName(), request));
    }

    // -------------------------------------------------------------------------
    // US06 — Create student profile
    // -------------------------------------------------------------------------

    @PostMapping("/student")
    @Operation(summary = "US06 - Crear perfil de estudiante",
               description = "Crea el perfil académico del usuario autenticado. Requiere que la cuenta sea de tipo STUDENT (seleccionada previamente en US04). Solo puede ejecutarse una vez por usuario (RN-08).")
    public ResponseEntity<StudentProfileResponse> createStudentProfile(
            @Valid @RequestBody CreateStudentProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createStudentProfile(auth.getName(), request));
    }

    // -------------------------------------------------------------------------
    // Skeleton endpoints — pendientes de implementar en próximas US
    // -------------------------------------------------------------------------

    @GetMapping("/student/{userId}")
    @Operation(summary = "Obtener perfil de estudiante (skeleton — US pendiente)")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getStudentProfile(userId));
    }

    @PatchMapping("/student/me")
    @Operation(summary = "Actualizar perfil de estudiante (skeleton — US07)")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @RequestBody UpdateStudentProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profileService.updateStudentProfile(auth.getName(), request));
    }
}
