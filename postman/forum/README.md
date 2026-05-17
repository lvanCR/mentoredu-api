# 💬 Forum — Postman

Colección de casos de prueba para el Bounded Context **Forum** (EP-05).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU16](./HU16-create-forum-thread/) | Crear hilo de foro | 4 | `POST /api/v1/threads` |

---

## Pendientes

| HU | Descripción | Endpoint |
|---|---|---|
| HU17 | Responder a hilo de foro | `POST /api/v1/threads/{id}/answers` |
| HU18 | Cerrar hilo de foro | `PATCH /api/v1/threads/{id}/close` |
| HU27 | Reaccionar a contenido del foro | `POST /api/v1/threads/{id}/reactions` |
| HU28 | Comentar respuesta del foro | `POST /api/v1/answers/{id}/comments` |
| HU29 | Seguir a un usuario | `POST /api/v1/users/{id}/follow` |

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT obtenido en HU02 login | HU16 |
| `{{subject_id}}` | UUID de una materia existente en BD | HU16 |
| `{{thread_id}}` | UUID devuelto por HU16 | HU17, HU18, HU27 |
