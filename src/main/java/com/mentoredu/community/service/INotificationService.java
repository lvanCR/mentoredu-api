package com.mentoredu.community.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.NotificationResponse;

import java.util.UUID;

public interface INotificationService {
    PagedResponse<NotificationResponse> getMyNotifications(String userEmail, int page, int size);
    PagedResponse<NotificationResponse> getPendingNotifications(String userEmail, int page, int size);
    void markAsRead(UUID notificationId, String userEmail);
}
