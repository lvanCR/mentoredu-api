# Bounded Context: Academy (EP-03)

Gestiona las academias de preparación, sus sedes, programas y ciclos de estudio.

---

## Historias implementadas

| HU | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU33 | Crear academia | `POST /api/v1/academies` | `MentorEduAcademyHU33-CreateAcademyPOST` | 2026-05-16 |

## Historias pendientes

| HU | Descripción |
|---|---|
| HU11 | Registrar oferta académica |

---

## Estructura de carpetas

```
academy/
├── README.md           ← este archivo
└── HU33-create-academy/
    ├── README.md
    ├── caso-01-exitoso-campos-obligatorios.json
    ├── caso-02-exitoso-todos-campos.json
    ├── caso-03-name-vacio.json
    ├── caso-04-nombre-duplicado.json
    ├── caso-05-tipo-incorrecto.json
    ├── caso-06-sin-perfil-base.json
    ├── caso-07-sin-perfil-organizacion.json
    └── caso-08-sin-autenticacion.json
```

---

## Notas del bounded context

- Todos los endpoints de `/api/v1/academies/**` requieren `Authorization: Bearer <access_token>`.
- El token debe corresponder a una cuenta de tipo `ORGANIZATION` con perfil de organización creado (US10).
- Una organización puede tener múltiples academias con nombres distintos.
- El nombre de academia es único **por organización**, no globalmente.
