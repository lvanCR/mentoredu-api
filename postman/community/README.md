# Community (US21–US27)

Confianza y engagement: seguimiento, verificación, asociaciones, moderación y notificaciones.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US21 | Seguir / dejar de seguir a un usuario (toggle) | `POST /api/v1/users/{id}/follow` | `US21-follow-user/` |
| US22 | Solicitar verificación de identidad | `POST /api/v1/verification/requests` | `US22-teacher-verification/` · `US22-academy-verification/` |
| US22 | Ver mis solicitudes de verificación | `GET /api/v1/verification/requests/me` | `US22-teacher-verification/` |
| US23 | Listar y revisar solicitudes (MODERATOR/ADMIN) | `GET /api/v1/verification/requests` · `PATCH /api/v1/verification/requests/{id}/review` | `US23-review-verification/` |
| US24 | Solicitar/aceptar/rechazar asociación docente-academia | `POST /api/v1/associations` · `PATCH /api/v1/associations/{id}/accept` · `PATCH /api/v1/associations/{id}/reject` | `US24-associate-teacher/` |
| US25 | Reportar contenido inapropiado | `POST /api/v1/moderation/reports` | `US25-report-content/` |
| US26 | Resolver un reporte (MODERATOR/ADMIN) | `GET /api/v1/moderation/reports` · `PATCH /api/v1/moderation/reports/{id}/resolve` | `US26-resolve-report/` |
| US27 | Ver mis notificaciones | `GET /api/v1/notifications/me` · `GET /api/v1/notifications/me/pending` · `PATCH /api/v1/notifications/{id}/read` | `US27-notifications/` |

## Variables de entorno

`api_v1`, `access_token`, `teacher_token`, `academy_token`, `moderator_token`, `target_user_id`, `report_id`, `verification_id`, `association_id`, `notification_id`

## Notas

- US21 funciona como toggle: primera llamada → 201 (seguir); segunda → 204 (dejar de seguir).
- Un usuario no puede seguirse a sí mismo → 400 (RN-21).
- US22 requiere al menos un documento adjunto (RN-16).
- Un rechazo en US23 requiere razón obligatoria (`notes`) (RN-17).
- US24: la asociación queda PENDIENTE hasta que la academia la resuelve (RN-18).
- US26: toda resolución de reporte genera una entrada en el log de auditoría (RN-19).
