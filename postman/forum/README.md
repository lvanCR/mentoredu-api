# Forum (US12–US15)

Comunidad Q&A: hilos por curso, respuestas, comentarios y reacciones.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US12 | Crear hilo en el foro | `POST /api/v1/threads` | `US12-create-thread/` |
| US13 | Responder a un hilo | `POST /api/v1/threads/{id}/answers` | `US13-reply-to-thread/` |
| US14 | Reaccionar a contenido | `POST /api/v1/threads/{id}/reactions` · `POST /api/v1/answers/{id}/reactions` · `POST /api/v1/comments/{id}/reactions` | `US14-react-to-content/` |
| US15 | Comentar en una respuesta | `POST /api/v1/answers/{id}/comments` | `US15-comment-on-answer/` |

## Variables de entorno

`api_v1`, `access_token`, `university_id`, `course_id`, `thread_id`, `answer_id`, `comment_id`

## Notas

- Un hilo requiere al menos uno de `university_id`, `course_id`, `career_id` (RN-12).
- No se puede combinar `career_id` + `course_id` en el mismo hilo.
- Las reacciones son toggle: mismo tipo → elimina; tipo distinto → reemplaza (RN-15).
- Solo el autor del hilo o un MODERATOR/ADMIN puede cerrarlo (RN-14). El cierre se realiza con `PATCH /api/v1/threads/{id}/close`.
- Todos los endpoints requieren `Authorization: Bearer {{access_token}}`.
