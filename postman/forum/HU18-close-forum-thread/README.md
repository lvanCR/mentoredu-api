# HU18 — Close forum thread

Endpoint para cerrar un hilo de foro, bloqueando nuevas respuestas.

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `PATCH` | `/api/v1/threads/{threadId}/close` | Cerrar el hilo (solo el autor puede hacerlo) |

---

## Headers requeridos

```
Authorization: Bearer {{access_token}}
```

No requiere `Content-Type` ni body.

## Reglas de negocio

- Solo el autor del hilo puede cerrarlo (RN-18). Si otro usuario lo intenta, el sistema devuelve 403.
- El hilo debe existir; si no, devuelve 404.
- Si el hilo ya está `CLOSED`, el sistema devuelve 409.
- Tras el cierre, el estado pasa a `CLOSED` y no se permiten nuevas respuestas.

---

## Casos de prueba

| Archivo | Escenario | Status esperado |
|---|---|---|
| `caso-01.json` | Cierre exitoso por el autor | 200 OK |
| `caso-02.json` | Intento de cierre por usuario no autor | 403 Forbidden |
| `caso-03.json` | Verificación: hilo cerrado bloquea nuevas respuestas | 409 Conflict |
| `caso-04.json` | Hilo no existe | 404 Not Found |
| `caso-05.json` | Hilo ya estaba cerrado | 409 Conflict |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del autor del hilo (obtenido en HU02) |
| `{{other_user_token}}` | JWT de un usuario distinto al autor |
| `{{thread_id}}` | UUID de un hilo OPEN propiedad del `access_token` |
| `{{closed_thread_id}}` | UUID de un hilo ya en estado CLOSED (devuelto tras caso-01) |
| `{{nonexistent_id}}` | UUID inexistente en BD |

---

## Response body (200 — exitoso)

```json
{
  "id": "uuid",
  "title": "¿Cómo resolver integrales por partes?",
  "status": "CLOSED",
  "updatedAt": "2026-05-17T12:20:00"
}
```
