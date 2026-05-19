# Bounded Context: Academy (EP-03)

Gestiona las academias de preparación, sus sedes, programas y ciclos de estudio.

---

## Historias implementadas

| HU | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU33 | Crear academia | `POST /api/v1/academies` | `MentorEduAcademyHU33-CreateAcademyPOST` | 2026-05-16 |
| HU11 | Registrar programa académico | `POST /api/v1/academies/{academyId}/programs` | `MentorEduAcademyHU11-CreateProgramPOST` | 2026-05-16 |
| HU11 | Registrar ciclo académico | `POST /api/v1/academies/{academyId}/cycles` | `MentorEduAcademyHU11-CreateCyclePOST` | 2026-05-16 |
| HU36 | Registrar sede de academia | `POST /api/v1/academies/{academyId}/campuses` | `MentorEduAcademyHU36-CreateCampusPOST` | 2026-05-19 |
| HU37 | Asociar docente a academia | `POST /api/v1/academies/{academyId}/teachers` | `MentorEduAcademyHU37-AssociateTeacherPOST` | 2026-05-19 |

## Historias pendientes

*(EP-03 completo)*

---

## Estructura de carpetas

```
academy/
├── README.md           ← este archivo
├── HU33-create-academy/
│   ├── README.md
│   └── caso-01 al caso-08
├── HU11-register-academic-offering/
│   ├── README.md
│   ├── caso-01-programa-exitoso.json
│   ├── caso-02-programa-duplicado.json
│   ├── caso-03-programa-campos-faltantes.json
│   ├── caso-04-ciclo-exitoso.json
│   ├── caso-05-ciclo-fecha-invalida.json
│   ├── caso-06-ciclo-duplicado.json
│   ├── caso-07-sin-autenticacion.json
│   └── caso-08-academia-no-encontrada.json
├── HU36-register-campus/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-campo-faltante.json
│   ├── caso-03-segunda-sede.json
│   └── caso-04-sede-duplicada.json
└── HU37-associate-teacher/
    ├── README.md
    ├── caso-01-exitoso.json
    ├── caso-02-docente-adicional.json
    ├── caso-03-campo-faltante.json
    ├── caso-04-perfil-no-encontrado.json
    ├── caso-05-docente-duplicado.json
    └── caso-06-sin-autenticacion.json
```

---

## Notas del bounded context

- Todos los endpoints de `/api/v1/academies/**` requieren `Authorization: Bearer <access_token>`.
- El token debe corresponder a una cuenta de tipo `ORGANIZATION` con perfil de organización creado (US10).
- Una organización puede tener múltiples academias con nombres distintos.
- El nombre de academia es único **por organización**, no globalmente.
- El nombre de programa es único **por academia**.
- El nombre de ciclo es único **por academia**.
- Las fechas de ciclo deben estar en formato `yyyy-MM-dd`. `endDate` debe ser posterior a `startDate`.
- Valores sugeridos: `modality` = PRESENCIAL/VIRTUAL/HIBRIDO · `intensity` = NORMAL/INTENSIVO · `cycleType` = REGULAR/INTENSIVO/VACACIONAL.
