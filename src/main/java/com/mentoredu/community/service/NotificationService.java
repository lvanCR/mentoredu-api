package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.NotificationResponse;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.model.Notification;
import com.mentoredu.community.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService            userService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getMyNotifications(String userEmail, int page, int size, String type) {
        User user = userService.findByEmailOrThrow(userEmail);
        var pageable = PagedResponse.toPageRequest(page, size);
        var result = (type != null && !type.isBlank())
                ? notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PagedResponse.from(result, NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getPendingNotifications(String userEmail, int page, int size, String type) {
        User user = userService.findByEmailOrThrow(userEmail);
        var pageable = PagedResponse.toPageRequest(page, size);
        var result = (type != null && !type.isBlank())
                ? notificationRepository.findByUserIdAndTypeAndReadAtIsNullOrderByCreatedAtDesc(user.getId(), type, pageable)
                : notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId(), pageable);
        return PagedResponse.from(result, NotificationResponse::from);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, String userEmail) {
        User user = userService.findByEmailOrThrow(userEmail);

        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada: " + notificationId));

        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}