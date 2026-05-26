# US27 — Ver mis notificaciones

**Epic**: EP-05 Community
**Bounded Context**: `community`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoints

| Método  | Path | Descripción |
|---|---|---|
| `GET`   | `/api/v1/notifications/me` | Todas las notificaciones (paginado) |
| `GET`   | `/api/v1/notifications/me/pending` | Solo no leídas (paginado) |
| `PATCH` | `/api/v1/notifications/{id}/read` | Marcar una notificación como leída |

**Auth requerida:** `Authorization: Bearer {{access_token}}`

---

## Tipos de notificación válidos (`type`)

| Tipo | Disparado en |
|---|---|
| `new_follower` | Alguien te siguió (US21) |
| `answer_received` | Respondieron tu hilo (US13) |
| `comment_received` | Comentaron tu respuesta (US15) |
| `reaction_received` | Reaccionaron a tu contenido (US14) |
| `solution_submitted` | Enviaron resolución a tu ejercicio (US18) |
| `feedback_received` | El docente revisó tu resolución (US19) |
| `verification_processed` | Verificación resuelta (US23) |
| `association_resolved` | Solicitud de asociación resuelta (US24) |

---

## Respuesta exitosa — 200 OK (GET)

Devuelve `PagedResponse<NotificationResponse>`:

```json
{
  "content": [
    {
      "id": "uuid",
      "userId": "uuid",
      "type": "answer_received",
      "payload": { "threadId": "uuid", "threadTitle": "¿Cómo resolver integrales?" },
      "readAt": null,
      "createdAt": "2026-05-22T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

## Respuesta PATCH /read — 204 No Content

El body de respuesta está vacío. No devuelve objeto JSON.

---

## Casos de prueba

| # | Archivo | Endpoint | Escenario | HTTP esperado |
|---|---|---|---|---|
| 01 | `caso-01-exitoso.json` | `GET /me/pending` | Tiene notificaciones pendientes | 200 OK |
| 02 | `caso-02-sin-autenticacion.json` | `GET /me/pending` | Sin token | 401 Unauthorized |
| 03 | `caso-03-lista-vacia.json` | `GET /me/pending` | Sin notificaciones pendientes | 200 OK (content vacío) |
| 04 | `caso-04-marcar-leida.json` | `PATCH /{id}/read` | Marcar notificación como leída | **204 No Content** |
| 05 | `caso-05-todas-notificaciones.json` | `GET /me` | Todas las notificaciones (leídas + no leídas) | 200 OK |
