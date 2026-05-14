package com.mentoredu.notification.service;

import com.mentoredu.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    List<NotificationResponse> getUserNotifications(String userEmail);
    NotificationResponse markAsRead(UUID notificationId);
}
