# 💬 Forum — Postman

Colección de casos de prueba para el Bounded Context **Forum** (EP-05).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU16](./HU16-create-forum-thread/) | Crear hilo de foro | 4 | `POST /api/v1/threads` |
| [HU17](./HU17-reply-to-forum-thread/) | Responder a hilo de foro | 4 | `POST /api/v1/threads/{id}/answers` |
| [HU18](./HU18-close-forum-thread/) | Cerrar hilo de foro | 5 | `PATCH /api/v1/threads/{id}/close` |
| [HU27](./HU27-react-to-forum-content/) | Reaccionar a contenido del foro | 4 | `POST /api/v1/threads/{id}/reactions` |
| [HU28](./HU28-comment-on-answer/) | Comentar respuesta del foro | 4 | `POST /api/v1/answers/{id}/comments` |

---

## Pendientes

| HU | Descripción | Endpoint |
|---|---|---|
| HU29 | Seguir a un usuario | `POST /api/v1/users/{id}/follow` |

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT obtenido en HU02 login (usuario autor) | HU16, HU17, HU18, HU27 |
| `{{other_user_token}}` | JWT de un usuario distinto al autor del hilo | HU18 (caso-02) |
| `{{subject_id}}` | UUID de una materia existente en BD | HU16 |
| `{{thread_id}}` | UUID devuelto por HU16 | HU17, HU18, HU27 |
| `{{closed_thread_id}}` | UUID de un hilo en estado CLOSED | HU17 (caso-03), HU18 (caso-05) |
| `{{answer_id}}` | UUID devuelto por HU17 | HU27 (reacción a respuesta) |
| `{{comment_id}}` | UUID devuelto por HU28 | HU27 (reacción a comentario) |
