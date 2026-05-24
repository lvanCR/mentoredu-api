# US04 — Actualizar perfil de estudiante

**Epic**: EP-01 Profile
**Bounded Context**: `profile`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
PATCH /api/v1/profiles/student/me
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

---

## Body — Todos los campos son opcionales

| Campo                | Tipo   | Validación |
|---|---|---|
| `schoolName`         | String | Máx. 120 chars |
| `gradeLevel`         | String | Máx. 20 chars. Ej: `5TO_SECUNDARIA`, `EGRESADO` |
| `studyShift`         | String | Máx. 30 chars. Ej: `MAÑANA`, `TARDE` |
| `targetUniversityId` | UUID   | ID de universidad del catálogo (V9). Inexistente → 400 |
| `targetAreaId`       | UUID   | ID de área del catálogo (V9). Inexistente → 400 |
| `targetCareerId`     | UUID   | ID de carrera del catálogo (V9). Inexistente → 400 |

> Los campos omitidos conservan su valor actual. Ningún campo es obligatorio en el PATCH.
>
> Los IDs de catálogo se obtienen con `GET /api/v1/catalog/universities`, `/api/v1/catalog/areas`, etc.

---

## Respuesta exitosa — 200 OK

```json
{
  "profileId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "gradeLevel": "5TO_SECUNDARIA",
  "schoolName": "Colegio Nacional San Marcos",
  "studyShift": "MAÑANA",
  "targetUniversityId": "b1000000-0000-0000-0000-000000000001",
  "targetAreaId": "b3000000-0000-0000-0000-000000000002",
  "targetCareerId": "b4000000-0000-0000-0000-000000000009"
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso.json` | Actualizar gradeLevel y schoolName | 200 OK |
| 02 | `caso-02-solo-universidad.json` | Solo targetUniversityId con UUID válido | 200 OK |
| 03 | `caso-03-university-no-existe.json` | targetUniversityId no existe en el catálogo | 400 Bad Request |
| 04 | `caso-04-perfil-no-existe.json` | Perfil estudiante no existe aún | 404 Not Found |
| 05 | `caso-05-sin-autenticacion.json` | Sin token | 401 Unauthorized |

---

## Flujo previo requerido

1. `POST /api/v1/auth/login` → obtener `{{access_token}}`
2. `POST /api/v1/profiles/student` → crear perfil (US04)
3. Consultar catálogo para obtener UUIDs → `GET /api/v1/catalog/universities`
4. Este PATCH → actualizar datos
