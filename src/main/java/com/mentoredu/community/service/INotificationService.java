package com.mentoredu.community.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.NotificationResponse;

import java.util.UUID;

public interface INotificationService {
    PagedResponse<NotificationResponse> getMyNotifications(String userEmail, int page, int size, String type);
    PagedResponse<NotificationResponse> getPendingNotifications(String userEmail, int page, int size, String type);
    void markAsRead(UUID notificationId, String userEmail);
}
