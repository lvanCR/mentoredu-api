# Pedagogy (US17–US20)

Ciclo pedagógico: ejercicios sin solución → resoluciones de estudiantes → feedback correctivo.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US17 | Ver resoluciones de mis ejercicios (docente) | `GET /api/v1/resources/{id}/solutions` | `US17-view-solutions/` |
| US18 | Enviar mi resolución a un ejercicio (estudiante) | `POST /api/v1/resources/{id}/solutions` | `US18-submit-solution/` |
| US19 | Dar feedback correctivo (docente) | `POST /api/v1/solutions/{id}/feedback` | `US19-give-feedback/` |
| US20 | Ver mi resolución y feedback recibido (estudiante) | `GET /api/v1/resources/{id}/solutions/mine` | `US20-view-my-solution/` |

## Variables de entorno

`api_v1`, `teacher_token`, `student_token`, `resource_id`, `solution_id`, `file_id`

## Notas

- US16 (publicar ejercicio) está en `library/US16-publish-exercise/` — requiere `acepta_resoluciones=true`.
- Un estudiante puede enviar exactamente una resolución por ejercicio. Segunda → 409 (RN-09).
- Solo el autor del ejercicio (o TEACHER con TeacherAcademyLink ACCEPTED si el autor es ACADEMY) puede ver resoluciones y dar feedback (RN-10).
- El feedback es inmutable una vez enviado — no se edita ni elimina (RN-11).
- El score de feedback va de 0.0 a 10.0 (RN-22).
