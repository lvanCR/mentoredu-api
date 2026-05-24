# US05 — Crear perfil de docente

**Epic**: EP-01 Profile
**Bounded Context**: `profile`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `POST`  | `/api/v1/profiles/teacher`    | Crear perfil de docente (primera vez) |
| `PATCH` | `/api/v1/profiles/teacher/me` | Actualizar perfil de docente existente |

**Auth requerida:** `Authorization: Bearer {{access_token}}` (rol `TEACHER`)

---

## Body — POST y PATCH

| Campo             | Tipo   | Requerido | Validación |
|---|---|---|---|
| `bioProfessional` | String | No        | Máx. 2000 chars. Descripción profesional libre. |

> Solo existe el campo `bioProfessional`. No existen campos `specialty` ni `institutionName` en esta versión.

---

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-01 | El rol de la cuenta debe ser `TEACHER`. Cualquier otro rol → 403. |
| —      | Solo puede existir un `teacher_profile` por usuario. Segunda creación → 409. |

---

## Respuesta exitosa — 201 Created (POST) / 200 OK (PATCH)

```json
{
  "profileId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bioProfessional": "Docente con 10 años de experiencia en matemáticas preuniversitarias."
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso.json` | POST sin body (bioProfessional null) | 201 Created |
| 02 | `caso-02-exitoso-con-bio.json` | POST con bioProfessional completo | 201 Created |
| 03 | `caso-03-perfil-ya-existe.json` | Segundo intento de creación | 409 Conflict |
| 04 | `caso-04-tipo-incorrecto.json` | Cuenta no es `TEACHER` (ej. `STUDENT`) | 403 Forbidden |
| 05 | `caso-05-sin-autenticacion.json` | Sin token | 401 Unauthorized |

---

## Flujo de uso

1. Registrar cuenta con `role: "TEACHER"` → `POST /api/v1/auth/register`
2. Login → `POST /api/v1/auth/login` (obtener `{{access_token}}`)
3. Crear perfil de docente → `POST /api/v1/profiles/teacher`
4. Actualizar bio → `PATCH /api/v1/profiles/teacher/me`
