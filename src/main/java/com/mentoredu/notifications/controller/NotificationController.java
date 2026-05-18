package com.mentoredu.notifications.controller;

import com.mentoredu.notifications.dto.NotificationResponse;
import com.mentoredu.notifications.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del usuario (EP-09).")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final INotificationService notificationService;

    // -------------------------------------------------------------------------
    // US25 — View pending notifications
    // -------------------------------------------------------------------------

    @GetMapping("/me/pending")
    @Operation(
        summary = "US25 - Consultar notificaciones pendientes",
        description = "Devuelve las notificaciones no leídas del usuario autenticado, "
            + "ordenadas por fecha descendente (más recientes primero). "
            + "Si no hay notificaciones pendientes, devuelve lista vacía sin error. "
            + "Solo se devuelven notificaciones cuyo campo read_at sea nulo (RN-30). "
            + "Las notificaciones son generadas exclusivamente por eventos del sistema (RN-29). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<List<NotificationResponse>> getPending() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificationService.getPendingNotifications(auth.getName()));
    }

    // -------------------------------------------------------------------------
    // Mark notification as read (complemento de US25 — RN-30)
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/read")
    @Operation(
        summary = "Marcar notificación como leída",
        description = "Marca una notificación específica como leída estableciendo su campo read_at. "
            + "Operación idempotente: si ya estaba leída, devuelve la notificación sin modificarla. "
            + "Solo el propietario de la notificación puede marcarla (RN-30). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificationService.markAsRead(id, auth.getName()));
    }
}
