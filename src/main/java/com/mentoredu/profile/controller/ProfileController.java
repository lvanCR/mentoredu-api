package com.mentoredu.profile.controller;

import com.mentoredu.profile.dto.*;
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

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    // -------------------------------------------------------------------------
    // GET /profiles/me
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @Operation(summary = "Obtener mi perfil completo")
    public ResponseEntity<ProfileMeResponse> getMyProfile() {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.getMyProfile(a.getName()));
    }

    // -------------------------------------------------------------------------
    // US04 — Select account type
    // -------------------------------------------------------------------------

    @PostMapping("/account-type")
    @Operation(summary = "US04 - Seleccionar tipo de cuenta (STUDENT, TEACHER, ACADEMY)")
    public ResponseEntity<ProfileResponse> selectAccountType(@Valid @RequestBody SelectAccountTypeRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.selectAccountType(a.getName(), request));
    }

    // -------------------------------------------------------------------------
    // US05 — Update common profile data
    // -------------------------------------------------------------------------

    @PatchMapping("/me")
    @Operation(summary = "US05 - Actualizar datos comunes del perfil")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.updateProfile(a.getName(), request));
    }

    // -------------------------------------------------------------------------
    // US06 — Create student profile
    // -------------------------------------------------------------------------

    @PostMapping("/student")
    @Operation(summary = "US06 - Crear perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> createStudentProfile(
            @Valid @RequestBody CreateStudentProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createStudentProfile(a.getName(), request));
    }

    @GetMapping("/student/{userId}")
    @Operation(summary = "Obtener perfil de estudiante por userId")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getStudentProfile(userId));
    }

    @PatchMapping("/student/me")
    @Operation(summary = "Actualizar perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @Valid @RequestBody UpdateStudentProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.updateStudentProfile(a.getName(), request));
    }

    // -------------------------------------------------------------------------
    // Create / update teacher profile
    // -------------------------------------------------------------------------

    @PostMapping("/teacher")
    @Operation(summary = "Crear perfil de docente")
    public ResponseEntity<TeacherProfileResponse> createTeacherProfile(
            @Valid @RequestBody CreateTeacherProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createTeacherProfile(a.getName(), request));
    }

    @PatchMapping("/teacher/me")
    @Operation(summary = "Actualizar perfil de docente")
    public ResponseEntity<TeacherProfileResponse> updateTeacherProfile(
            @Valid @RequestBody UpdateTeacherProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.updateTeacherProfile(a.getName(), request));
    }

    // -------------------------------------------------------------------------
    // Create / update academy profile (US06)
    // -------------------------------------------------------------------------

    @PostMapping("/academy")
    @Operation(summary = "US06 - Crear perfil de academia")
    public ResponseEntity<AcademyProfileResponse> createAcademyProfile(
            @Valid @RequestBody CreateAcademyProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createAcademyProfile(a.getName(), request));
    }

    @PatchMapping("/academy/me")
    @Operation(summary = "US06 - Actualizar perfil de academia")
    public ResponseEntity<AcademyProfileResponse> updateAcademyProfile(
            @Valid @RequestBody UpdateAcademyProfileRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.updateAcademyProfile(a.getName(), request));
    }

    // -------------------------------------------------------------------------
    // GET /profiles/{userId} — public profile (US06 E4)
    // -------------------------------------------------------------------------

    @GetMapping("/{userId}")
    @Operation(summary = "US06 - Ver perfil público de cualquier usuario")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable UUID userId) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }
}
