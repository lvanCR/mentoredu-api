package com.mentoredu.community.controller;

import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.service.INotificationService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "US27 - Ver todas mis notificaciones. Filtro opcional: ?type=new_follower|answer_received|etc.")
    public ResponseEntity<PagedResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String type) {
        return ResponseEntity.ok(notificationService.getMyNotifications(SecurityUtils.currentEmail(), page, size, type));
    }

    @GetMapping("/me/pending")
    @Operation(summary = "US27 - Ver mis notificaciones no leídas. Filtro opcional: ?type=new_follower|answer_received|etc.")
    public ResponseEntity<PagedResponse<NotificationResponse>> getPendingNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String type) {
        return ResponseEntity.ok(notificationService.getPendingNotifications(SecurityUtils.currentEmail(), page, size, type));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "US27 - Marcar una notificación como leída")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id, SecurityUtils.currentEmail());
        return ResponseEntity.noContent().build();
    }
}
