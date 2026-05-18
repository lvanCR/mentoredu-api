package com.mentoredu.notifications.dto;

import com.mentoredu.notifications.model.Notification;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationResponse {
    private final UUID id;
    private final UUID userId;
    private final String type;
    private final String title;
    private final String message;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUser().getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.read = notification.getReadAt() != null;
        this.createdAt = notification.getCreatedAt();
    }
}
