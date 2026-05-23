package com.mentoredu.community.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Consultar y marcar notificaciones (US27)")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/me")
    @Operation(summary = "US27 - Ver todas mis notificaciones")
    public ResponseEntity<PagedResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(notificationService.getMyNotifications(auth.getName(), page, size));
    }

    @GetMapping("/me/pending")
    @Operation(summary = "US27 - Ver mis notificaciones no leídas")
    public ResponseEntity<PagedResponse<NotificationResponse>> getPendingNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(notificationService.getPendingNotifications(auth.getName(), page, size));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "US27 - Marcar una notificación como leída")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        notificationService.markAsRead(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }
}
