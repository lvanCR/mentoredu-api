package com.mentoredu.notifications.service;

import com.mentoredu.notifications.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    List<NotificationResponse> getPendingNotifications(String userEmail);
    NotificationResponse markAsRead(UUID notificationId, String userEmail);
}
