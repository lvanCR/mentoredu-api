# US05 — Actualizar perfil de docente

**Epic**: EP-01 Profile
**Bounded Context**: `profile`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
PATCH /api/v1/profiles/teacher/me
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

---

## Body

| Campo             | Tipo   | Requerido | Validación |
|---|---|---|---|
| `bioProfessional` | String | No        | Máx. 2000 chars. Si se omite, conserva el valor actual. |

> Solo existe el campo `bioProfessional`. No existen campos `specialty` ni `institutionName`.

---

## Respuesta exitosa — 200 OK

```json
{
  "profileId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bioProfessional": "Docente con 10 años de experiencia en química."
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso.json` | Actualizar bioProfessional válida | 200 OK |
| 02 | `caso-02-bio-larga.json` | bioProfessional excede 2000 chars | 400 Bad Request |
| 03 | `caso-03-perfil-no-existe.json` | Perfil docente no existe aún | 404 Not Found |
| 04 | `caso-04-sin-autenticacion.json` | Sin token | 401 Unauthorized |

---

## Flujo previo requerido

1. `POST /api/v1/auth/login` → obtener `{{access_token}}`
2. `POST /api/v1/profiles/teacher` → crear perfil (US05)
3. Este PATCH → actualizar datos
