# HU19 — Report content

Endpoint para reportar contenido inapropiado (hilos, respuestas, comentarios o recursos).

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/v1/moderation/reports` | Crear reporte sobre un contenido |

---

## Casos de prueba

| Archivo | Escenario | Método | Status esperado |
|---|---|---|---|
| `caso-01.json` | Reporte exitoso de un hilo | POST | 201 Created |
| `caso-02.json` | Reporte sin motivo (campo vacío) | POST | 400 Bad Request |
| `caso-03.json` | Reporte de contenido propio | POST | 400 Bad Request |
| `caso-04.json` | Contenido inexistente | POST | 404 Not Found |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario reportante (obtenido en HU02) |
| `{{own_content_token}}` | JWT del autor del contenido (para self-report) |
| `{{thread_id}}` | UUID de un hilo existente creado por otro usuario |
| `{{own_thread_id}}` | UUID de un hilo propiedad del usuario autenticado |
| `{{nonexistent_id}}` | UUID inexistente en BD |

---

## Body esperado

```json
{
  "targetType": "THREAD",
  "targetId": "{{thread_id}}",
  "reason": "Contenido inapropiado o spam"
}
```

Valores válidos para `targetType`: `THREAD`, `ANSWER`, `COMMENT`, `RESOURCE`.
