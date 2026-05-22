package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.model.TeacherAcademyLink;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.profile.model.AcademyProfile;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
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
@RequestMapping("/api/v1/associations")
@RequiredArgsConstructor
@Tag(name = "Asociaciones", description = "Vinculación docente-academia (US24)")
@SecurityRequirement(name = "bearerAuth")
public class AssociationController {

    private final TeacherAcademyLinkRepository linkRepository;
    private final ProfileRepository            profileRepository;
    private final TeacherProfileRepository     teacherProfileRepository;
    private final AcademyProfileRepository     academyProfileRepository;
    private final UserRepository               userRepository;

    // -------------------------------------------------------------------------
    // US24 — Docente solicita unirse a una academia
    // -------------------------------------------------------------------------

    @PostMapping("/teacher-academy")
    @Operation(summary = "US24 - Docente solicita asociarse a una academia")
    public ResponseEntity<AssociationResponse> request(@Valid @RequestBody CreateAssociationRequest request) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = resolveUser(auth);
        Profile profile = resolveProfile(user);

        if (!teacherProfileRepository.existsById(profile.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AcademyProfile academy = academyProfileRepository.findById(request.getAcademyProfileId())
                .orElseThrow(() -> new IllegalArgumentException("Academia no encontrada: " + request.getAcademyProfileId()));

        if (linkRepository.findByTeacherProfileIdAndAcademyProfileId(
                profile.getId(), academy.getProfileId()).isPresent()) {
            throw new DuplicateAssociationException("Ya existe una solicitud de asociación con esta academia");
        }

        TeacherAcademyLink link = TeacherAcademyLink.builder()
                .teacherProfileId(profile.getId())
                .academyProfileId(academy.getProfileId())
                .status("PENDING")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(new AssociationResponse(linkRepository.save(link)));
    }

    // -------------------------------------------------------------------------
    // Docente: ver mis asociaciones
    // -------------------------------------------------------------------------

    @GetMapping("/teacher-academy/me")
    @Operation(summary = "US24 - Ver mis asociaciones (como docente)")
    public ResponseEntity<List<AssociationResponse>> myAssociations() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = resolveUser(auth);
        Profile profile = resolveProfile(user);

        return ResponseEntity.ok(
                linkRepository.findByTeacherProfileId(profile.getId())
                        .stream().map(AssociationResponse::new).toList()
        );
    }

    // -------------------------------------------------------------------------
    // Academia: ver solicitudes pendientes
    // -------------------------------------------------------------------------

    @GetMapping("/teacher-academy/academy")
    @Operation(summary = "US24 - Ver solicitudes de asociación recibidas (como academia)")
    public ResponseEntity<List<AssociationResponse>> academyRequests() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = resolveUser(auth);
        Profile profile = resolveProfile(user);

        if (!academyProfileRepository.existsById(profile.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                linkRepository.findByAcademyProfileId(profile.getId())
                        .stream().map(AssociationResponse::new).toList()
        );
    }

    // -------------------------------------------------------------------------
    // US24 — Academia acepta o rechaza solicitud
    // -------------------------------------------------------------------------

    @PatchMapping("/teacher-academy/{id}/accept")
    @Operation(summary = "US24 - Academia acepta solicitud de asociación")
    public ResponseEntity<AssociationResponse> accept(@PathVariable UUID id) {
        return resolve(id, "ACCEPTED");
    }

    @PatchMapping("/teacher-academy/{id}/reject")
    @Operation(summary = "US24 - Academia rechaza solicitud de asociación")
    public ResponseEntity<AssociationResponse> reject(@PathVariable UUID id) {
        return resolve(id, "REJECTED");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<AssociationResponse> resolve(UUID id, String newStatus) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = resolveUser(auth);
        Profile profile = resolveProfile(user);

        if (!academyProfileRepository.existsById(profile.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TeacherAcademyLink link = linkRepository.findById(id)
                .orElseThrow(() -> new AssociationNotFoundException("Solicitud de asociación no encontrada: " + id));

        if (!link.getAcademyProfileId().equals(profile.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"PENDING".equals(link.getStatus())) {
            throw new DuplicateAssociationException("La solicitud ya fue resuelta con estado: " + link.getStatus());
        }

        link.setStatus(newStatus);
        link.setResolvedAt(LocalDateTime.now());

        return ResponseEntity.ok(new AssociationResponse(linkRepository.save(link)));
    }

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }

    private Profile resolveProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Perfil no encontrado para: " + user.getEmail()));
    }
}
