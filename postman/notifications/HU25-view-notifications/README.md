# US25 — View pending notifications

**Endpoint**: `GET /api/v1/notifications/me/pending`  
**Auth**: Bearer JWT requerido  
**Descripción**: Devuelve las notificaciones no leídas del usuario autenticado, ordenadas por fecha descendente.

## Casos

| Caso | Escenario Gherkin | HTTP esperado |
|---|---|---|
| caso-01 | Exitoso — tiene notificaciones pendientes | 200 OK con lista |
| caso-02 | Error — sin autenticación | 401 Unauthorized |
| caso-03 | Alternativo exitoso — sin notificaciones pendientes | 200 OK lista vacía |

## Respuesta exitosa (200)

```json
[
  {
    "id": "uuid",
    "userId": "uuid",
    "type": "FORUM_REPLY",
    "title": "Alguien respondió tu hilo",
    "message": "Tu hilo 'Cómo resolver integrales...' recibió una nueva respuesta.",
    "read": false,
    "createdAt": "2026-05-18T10:30:00"
  }
]
```

## Notas

- Una lista vacía (`[]`) no es un error; indica que no hay notificaciones pendientes.
- `read: false` siempre en este endpoint (solo devuelve `read_at IS NULL`).
- Las notificaciones son generadas por eventos del sistema (RN-29), no por operaciones directas del usuario.
