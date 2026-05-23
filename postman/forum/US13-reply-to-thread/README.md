# HU17 — Reply to forum thread

Endpoint para publicar una respuesta en un hilo de foro existente.

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/v1/threads/{threadId}/answers` | Publicar una respuesta en el hilo |

---

## Headers requeridos

```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

## Body fields

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `body` | `string` | Sí | No puede estar vacío (`@NotBlank`) |

## Reglas de negocio

- El hilo debe existir; si no, el sistema devuelve 404.
- Si el hilo está en estado `CLOSED`, el sistema rechaza la respuesta con 409 (`ThreadClosedException`).
- La respuesta se crea con `isAccepted = false` por defecto.
- Requiere autenticación JWT.

---

## Casos de prueba

| Archivo | Escenario | Status esperado |
|---|---|---|
| `caso-01.json` | Respuesta válida a hilo abierto | 201 Created |
| `caso-02.json` | Body vacío | 400 Bad Request |
| `caso-03.json` | Hilo en estado CLOSED | 409 Conflict |
| `caso-04.json` | Sin autenticación | 401 Unauthorized |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT obtenido en HU02 login |
| `{{thread_id}}` | UUID de un hilo en estado OPEN (creado en HU16) |
| `{{closed_thread_id}}` | UUID de un hilo en estado CLOSED (creado y cerrado en HU18) |

---

## Response body (201 — creado)

```json
{
  "id": "uuid",
  "threadId": "uuid",
  "body": "La integral por partes usa la fórmula ∫u dv = uv − ∫v du.",
  "isAccepted": false,
  "authorDisplay": "Prof. García",
  "createdAt": "2026-05-17T12:10:00",
  "updatedAt": "2026-05-17T12:10:00"
}
```
