package com.mentoredu.community.service;

import com.mentoredu.community.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    /**
     * Obtiene todas las notificaciones del usuario.
     * @param userEmail Email del usuario.
     * @return Lista de notificaciones ordenadas por fecha descendente.
     */
    List<NotificationResponse> getMyNotifications(String userEmail);

    /**
     * Obtiene solo las notificaciones pendientes (no leídas) del usuario.
     * @param userEmail Email del usuario.
     * @return Lista de notificaciones pendientes.
     */
    List<NotificationResponse> getPendingNotifications(String userEmail);

    /**
     * Marca una notificación como leída, verificando que pertenece al usuario autenticado.
     * @param notificationId ID de la notificación.
     * @param userEmail Email del usuario autenticado.
     */
    void markAsRead(UUID notificationId, String userEmail);
}
