# US14 — Reaccionar a contenido del foro

Endpoint de reacción con comportamiento **toggle** (reaccionar / quitar reacción) aplicable a hilos, respuestas y comentarios.

---

## Endpoints

| Método | Path | Target |
|---|---|---|
| `POST` | `/api/v1/threads/{threadId}/reactions` | Reaccionar a un hilo |
| `POST` | `/api/v1/answers/{answerId}/reactions` | Reaccionar a una respuesta |
| `POST` | `/api/v1/comments/{commentId}/reactions` | Reaccionar a un comentario |

---

## Headers requeridos

```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

## Body fields

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `reactionType` | `string` | Sí | Valor libre (p. ej. `LIKE`, `HELPFUL`, `DISLIKE`) |

## Reglas de negocio

- Si el usuario no ha reaccionado antes: se crea la reacción → 201 Created.
- Si el usuario ya reaccionó con el mismo `reactionType`: se elimina la reacción → 204 No Content (toggle off).
- Solo una reacción por usuario por tipo por contenido.
- El contenido referenciado debe existir; si no, devuelve 404.
- Requiere autenticación JWT.

---

## Casos de prueba

| Archivo | Escenario | Status esperado |
|---|---|---|
| `caso-01.json` | Primera reacción a un hilo | 201 Created |
| `caso-02.json` | Toggle off (misma reacción al mismo hilo) | 204 No Content |
| `caso-03.json` | Contenido (hilo) inexistente | 404 Not Found |
| `caso-04.json` | Sin autenticación | 401 Unauthorized |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT obtenido en HU02 login |
| `{{thread_id}}` | UUID de un hilo existente (de HU16) |
| `{{answer_id}}` | UUID de una respuesta existente (de HU17) |
| `{{comment_id}}` | UUID de un comentario existente (de HU28) |
| `{{nonexistent_id}}` | UUID inexistente en BD |

---

## Response body (201 — reacción creada)

```json
{
  "id": "uuid",
  "reactionType": "LIKE",
  "authorDisplay": "Juan Pérez",
  "createdAt": "2026-05-17T12:30:00"
}
```

Cuando es toggle off (204), el body está vacío.
