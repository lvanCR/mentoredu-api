# Profile (US04–US06)

Perfiles específicos por rol: estudiante, docente y academia.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US04 | Crear perfil de estudiante | `POST /api/v1/profiles/student` | `US04-student-profile/` |
| US04 | Actualizar perfil de estudiante | `PATCH /api/v1/profiles/student/me` | `US04-student-profile-update/` |
| US05 | Crear perfil de docente | `POST /api/v1/profiles/teacher` | `US05-teacher-profile/` |
| US05 | Actualizar perfil de docente | `PATCH /api/v1/profiles/teacher/me` | `US05-teacher-profile-update/` |
| US06 | Crear perfil de academia | `POST /api/v1/profiles/academy` | `US06-academy-profile/` |
| — | Ver mi perfil completo | `GET /api/v1/profiles/me` | `F04-get-my-profile/` |
| — | Actualizar datos comunes | `PATCH /api/v1/profiles/me` | `profile-me/` |

## Notas

- Todos los endpoints requieren `Authorization: Bearer {{access_token}}`.
- El rol del usuario determina qué perfil específico puede crear (STUDENT → student, TEACHER → teacher, ACADEMY → academy).
- Los campos de catálogo (`targetUniversityId`, `targetAreaId`, `targetCareerId`) referencian UUIDs de `catalog/`.
- Una vez creado, el perfil específico no puede cambiar de tipo (RN-01).
