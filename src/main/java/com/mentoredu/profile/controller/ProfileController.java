package com.mentoredu.profile.controller;

import com.mentoredu.config.SecurityUtils;
import com.mentoredu.profile.dto.*;
import com.mentoredu.profile.service.IProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gestión de perfiles de usuario.")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Obtener mi perfil completo")
    public ResponseEntity<ProfileMeResponse> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile(SecurityUtils.currentEmail()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Actualizar datos comunes del perfil")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(SecurityUtils.currentEmail(), request));
    }

    @PostMapping("/student")
    @Operation(summary = "US04 - Crear perfil de estudiante")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentProfileResponse> createStudentProfile(
            @Valid @RequestBody CreateStudentProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createStudentProfile(SecurityUtils.currentEmail(), request));
    }

    @GetMapping("/student/{userId}")
    @Operation(summary = "US04 - Obtener perfil de estudiante por userId")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getStudentProfile(userId));
    }

    @PatchMapping("/student/me")
    @Operation(summary = "US04 - Actualizar perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @Valid @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(profileService.updateStudentProfile(SecurityUtils.currentEmail(), request));
    }

    @PostMapping("/teacher")
    @Operation(summary = "US05 - Crear perfil de docente")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TeacherProfileResponse> createTeacherProfile(
            @Valid @RequestBody CreateTeacherProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createTeacherProfile(SecurityUtils.currentEmail(), request));
    }

    @GetMapping("/teacher/me")
    @Operation(summary = "US05 - Obtener mi perfil de docente")
    public ResponseEntity<TeacherProfileResponse> getMyTeacherProfile() {
        return ResponseEntity.ok(profileService.getTeacherProfile(SecurityUtils.currentEmail()));
    }

    @PatchMapping("/teacher/me")
    @Operation(summary = "US05 - Actualizar perfil de docente")
    public ResponseEntity<TeacherProfileResponse> updateTeacherProfile(
            @Valid @RequestBody UpdateTeacherProfileRequest request) {
        return ResponseEntity.ok(profileService.updateTeacherProfile(SecurityUtils.currentEmail(), request));
    }

    @PostMapping("/academy")
    @Operation(summary = "US06 - Crear perfil de academia")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AcademyProfileResponse> createAcademyProfile(
            @Valid @RequestBody CreateAcademyProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createAcademyProfile(SecurityUtils.currentEmail(), request));
    }

    @GetMapping("/academy/me")
    @Operation(summary = "US06 - Obtener mi perfil de academia")
    public ResponseEntity<AcademyProfileResponse> getMyAcademyProfile() {
        return ResponseEntity.ok(profileService.getAcademyProfile(SecurityUtils.currentEmail()));
    }

    @PatchMapping("/academy/me")
    @Operation(summary = "US06 - Actualizar perfil de academia")
    public ResponseEntity<AcademyProfileResponse> updateAcademyProfile(
            @Valid @RequestBody UpdateAcademyProfileRequest request) {
        return ResponseEntity.ok(profileService.updateAcademyProfile(SecurityUtils.currentEmail(), request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Ver perfil público de cualquier usuario")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId, SecurityUtils.currentEmail()));
    }
}
