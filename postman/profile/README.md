# Bounded Context: Profile (EP-02)

Gestiona el perfil base y los perfiles específicos de cada tipo de usuario.

---

## Historias implementadas

| HU | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU04 | Seleccionar tipo de cuenta | `POST /api/v1/profiles/account-type` | `MentorEduProfileHU04-SelectAccountTypePOST` | 2026-05-16 |

## Historias pendientes

| HU | Descripción |
|---|---|
| HU05 | Actualizar datos comunes del perfil |
| HU06 | Crear perfil de estudiante |
| HU07 | Actualizar universidad objetivo del estudiante |
| HU08 | Crear perfil de docente |
| HU09 | Actualizar especialidad del docente |
| HU10 | Crear perfil de organización |

---

## Estructura de carpetas

```
profile/
├── README.md           ← este archivo
└── HU04-select-account-type/
    ├── README.md
    ├── caso-01-exitoso-student.json
    ├── caso-02-exitoso-teacher.json
    ├── caso-03-exitoso-organization.json
    ├── caso-04-perfil-ya-existe.json
    └── caso-05-tipo-invalido.json
```

---

## Notas del bounded context

- Todos los endpoints de `/api/v1/profiles/**` requieren `Authorization: Bearer <access_token>`.
- El token se obtiene del endpoint `POST /api/v1/auth/login` (HU02).
- `profileType` acepta exactamente: `STUDENT`, `TEACHER`, `ORGANIZATION`.
- Una vez seleccionado el tipo de cuenta, no puede cambiarse (RN-07).
