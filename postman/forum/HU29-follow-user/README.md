# HU29 — Follow a user

Endpoint de seguimiento entre usuarios con comportamiento **toggle** (seguir / dejar de seguir).

---

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/v1/users/{targetUserId}/follow` | Seguir / dejar de seguir (toggle) |
| `GET` | `/api/v1/users/{userId}/following` | Listar usuarios que sigue un usuario |
| `GET` | `/api/v1/users/{userId}/followers` | Listar seguidores de un usuario |

---

## Casos de prueba

| Archivo | Escenario | Método | Status esperado |
|---|---|---|---|
| `caso-01.json` | Seguir a un usuario (nuevo follow) | POST | 201 Created |
| `caso-02.json` | Dejar de seguir (toggle off) | POST | 204 No Content |
| `caso-03.json` | Intentar seguirse a sí mismo | POST | 400 Bad Request |
| `caso-04.json` | Usuario objetivo no existe | POST | 404 Not Found |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario autenticado (obtenido en HU02) |
| `{{target_user_id}}` | UUID del usuario a seguir |
| `{{self_user_id}}` | UUID del usuario autenticado (para self-follow) |
| `{{nonexistent_user_id}}` | UUID inexistente |
