package com.mentoredu.community.controller;

import com.mentoredu.community.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Seguimientos", description = "Seguir y dejar de seguir usuarios (US21)")
@SecurityRequirement(name = "bearerAuth")
public class FollowController {

    private final IFollowService followService;

    @PostMapping("/{userId}/follow")
    @Operation(summary = "US21 - Seguir o dejar de seguir a un usuario (toggle)")
    public ResponseEntity<Void> toggleFollow(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean followed = followService.toggleFollow(userId, auth.getName());
        
        return followed 
            ? ResponseEntity.status(HttpStatus.CREATED).build()
            : ResponseEntity.noContent().build();
    }
}
