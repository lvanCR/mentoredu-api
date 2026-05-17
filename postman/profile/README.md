# Bounded Context: Profile (EP-02)

Gestiona el perfil base y los perfiles específicos de cada tipo de usuario.

---

## Historias implementadas

| HU | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU04 | Seleccionar tipo de cuenta | `POST /api/v1/profiles/account-type` | `MentorEduProfileHU04-SelectAccountTypePOST` | 2026-05-16 |
| HU05 | Actualizar datos comunes del perfil | `PATCH /api/v1/profiles/me` | `MentorEduProfileHU05-UpdateProfilePATCH` | 2026-05-16 |
| HU06 | Crear perfil de estudiante | `POST /api/v1/profiles/student` | `MentorEduProfileHU06-CreateStudentProfilePOST` | 2026-05-16 |
| HU07 | Actualizar universidad objetivo del estudiante | `PATCH /api/v1/profiles/student/me` | `MentorEduProfileHU07-UpdateTargetUniversityPATCH` | 2026-05-16 |
| HU08 | Crear perfil de docente | `POST /api/v1/profiles/teacher` | `MentorEduProfileHU08-CreateTeacherProfilePOST` | 2026-05-16 |
| HU09 | Actualizar especialidad del docente | `PATCH /api/v1/profiles/teacher/me` | `MentorEduProfileHU09-UpdateTeacherSpecialtyPATCH` | 2026-05-16 |
| HU10 | Crear perfil de organización | `POST /api/v1/profiles/organization` | `MentorEduProfileHU10-CreateOrganizationProfilePOST` | 2026-05-16 |

## Historias pendientes

_EP-02 Profile completado. Continúa en EP-03 Academy (HU33, HU11)._

---

## Estructura de carpetas

```
profile/
├── README.md           ← este archivo
├── HU04-select-account-type/
│   ├── README.md
│   ├── caso-01-exitoso-student.json
│   ├── caso-02-exitoso-teacher.json
│   ├── caso-03-exitoso-organization.json
│   ├── caso-04-perfil-ya-existe.json
│   └── caso-05-tipo-invalido.json
├── HU05-update-profile/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-solo-nombre-ciudad.json
│   ├── caso-03-displayname-vacio.json
│   ├── caso-04-sin-displayname.json
│   └── caso-05-sin-autenticacion.json
├── HU06-create-student-profile/
│   ├── README.md
│   ├── caso-01-exitoso-campos-obligatorios.json
│   ├── caso-02-exitoso-todos-campos.json
│   ├── caso-03-gradelevel-vacio.json
│   ├── caso-04-targetuniversity-vacio.json
│   ├── caso-05-perfil-ya-existe.json
│   ├── caso-06-tipo-incorrecto.json
│   ├── caso-07-sin-perfil-base.json
│   └── caso-08-sin-autenticacion.json
├── HU07-update-target-university/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-solo-target-university.json
│   ├── caso-03-targetuniversity-vacio.json
│   ├── caso-04-perfil-no-existe.json
│   └── caso-05-sin-autenticacion.json
├── HU08-create-teacher-profile/
│   ├── README.md
│   ├── caso-01-exitoso-campos-obligatorios.json
│   ├── caso-02-exitoso-todos-campos.json
│   ├── caso-03-specialty-vacio.json
│   ├── caso-04-institution-vacio.json
│   ├── caso-05-specialty-faltante.json
│   ├── caso-06-tipo-incorrecto.json
│   ├── caso-07-perfil-ya-existe.json
│   └── caso-08-sin-perfil-base.json
├── HU09-update-teacher-specialty/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-solo-specialty.json
│   ├── caso-03-specialty-vacio.json
│   ├── caso-04-perfil-no-existe.json
│   └── caso-05-sin-autenticacion.json
└── HU10-create-organization-profile/
    ├── README.md
    ├── caso-01-exitoso-campos-obligatorios.json
    ├── caso-02-exitoso-todos-campos.json
    ├── caso-03-nombre-vacio.json
    ├── caso-04-nombre-duplicado.json
    ├── caso-05-tipo-incorrecto.json
    ├── caso-06-perfil-ya-existe.json
    ├── caso-07-sin-perfil-base.json
    └── caso-08-sin-autenticacion.json
```

---

## Notas del bounded context

- Todos los endpoints de `/api/v1/profiles/**` requieren `Authorization: Bearer <access_token>`.
- El token se obtiene del endpoint `POST /api/v1/auth/login` (HU02).
- `profileType` acepta exactamente: `STUDENT`, `TEACHER`, `ORGANIZATION`.
- Una vez seleccionado el tipo de cuenta, no puede cambiarse (RN-07).
