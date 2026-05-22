package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        return ResponseEntity.ok(
            notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).toList()
        );
    }

    @GetMapping("/me/pending")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        return ResponseEntity.ok(
            notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).toList()
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada: " + id));
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        return ResponseEntity.noContent().build();
    }
}
