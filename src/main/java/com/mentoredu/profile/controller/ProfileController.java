package com.mentoredu.profile.controller;

import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.service.IProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gestión de perfiles de usuario (US03, US04).")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping("/student/{userId}")
    @Operation(summary = "US04 - Obtener perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getStudentProfile(userId));
    }

    @PutMapping("/student/{userId}")
    @Operation(summary = "US03 - Actualizar perfil de estudiante")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(profileService.updateStudentProfile(userId, request));
    }
}
