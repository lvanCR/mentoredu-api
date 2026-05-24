# US04 — Editar perfil de estudiante

**Epic**: EP-01 Profile
**Bounded Context**: `profile`
**Estado**: Implementada — 2026-05-22 `develop`
**Nombre en Postman**: `MentorEduProfileUS04-CreateStudentProfilePOST`

---

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| `POST`  | `/api/v1/profiles/student`    | Crear perfil de estudiante (primera vez) |
| `PATCH` | `/api/v1/profiles/student/me` | Actualizar perfil de estudiante existente |
| `GET`   | `/api/v1/profiles/student/{userId}` | Obtener perfil de estudiante por userId |

**Auth requerida:** `Authorization: Bearer {{access_token}}` (rol `STUDENT`)

---

## Body — POST y PATCH

| Campo               | Tipo   | Requerido | Validación |
|---|---|---|---|
| `gradeLevel`        | String | ✅ Sí     | No vacío (`@NotBlank`), máx. 20 chars. Ej: `5TO_SECUNDARIA`, `4TO_SECUNDARIA`, `EGRESADO` |
| `schoolName`        | String | No        | Máx. 120 chars |
| `studyShift`        | String | No        | Máx. 30 chars. Ej: `MAÑANA`, `TARDE` |
| `targetUniversityId`| UUID   | No        | ID de universidad del catálogo (V9). Inexistente → 400 (RN-03) |
| `targetAreaId`      | UUID   | No        | ID de área del catálogo (V9). Inexistente → 400 (RN-03) |
| `targetCareerId`    | UUID   | No        | ID de carrera del catálogo (V9). Inexistente → 400 (RN-03) |

> Los IDs de catálogo se obtienen con `GET /api/v1/catalog/universities`, `/api/v1/catalog/areas`, etc.

---

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-01  | El rol de la cuenta debe ser `STUDENT`. Cualquier otro rol → 403. |
| RN-03  | `targetUniversityId`, `targetAreaId` y `targetCareerId` deben existir en el catálogo. |
| —      | Solo puede existir un `student_profile` por usuario. Segunda creación → 409. |

---

## Respuesta exitosa — 201 Created (POST) / 200 OK (PATCH)

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
| 01 | `caso-01-exitoso-campos-obligatorios.json` | Solo `gradeLevel` | 201 Created |
| 02 | `caso-02-exitoso-todos-campos.json`         | Todos los campos completos con IDs del catálogo | 201 Created |
| 03 | `caso-03-gradelevel-vacio.json`             | `gradeLevel` vacío | 400 Bad Request |
| 04 | `caso-04-university-no-existe.json`         | `targetUniversityId` no existe en el catálogo | 400 Bad Request |
| 05 | `caso-05-perfil-ya-existe.json`             | Segundo intento de creación | 409 Conflict |
| 06 | `caso-06-tipo-incorrecto.json`              | Cuenta no es `STUDENT` (ej. `TEACHER`) | 403 Forbidden |
| 07 | `caso-07-sin-autenticacion.json`            | Sin token | 401 Unauthorized |

---

## Flujo de uso

1. Registrar cuenta con `role: "STUDENT"` → `POST /api/v1/auth/register`
2. Login → `POST /api/v1/auth/login` (obtener `{{access_token}}`)
3. Consultar catálogo para obtener UUIDs válidos → `GET /api/v1/catalog/universities`
4. Crear perfil → `POST /api/v1/profiles/student`
5. Actualizar datos → `PATCH /api/v1/profiles/student/me`
