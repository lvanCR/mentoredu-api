# US06 — Editar perfil de academia

**Epic**: EP-01 Profile
**Bounded Context**: `profile`
**Estado**: Implementada — 2026-05-22 `develop`
**Nombre en Postman**: `MentorEduProfileUS06-CreateAcademyProfilePOST`

---

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| `POST`  | `/api/v1/profiles/academy`    | Crear perfil de academia (primera vez) |
| `PATCH` | `/api/v1/profiles/academy/me` | Actualizar perfil de academia existente |
| `GET`   | `/api/v1/profiles/{userId}`   | Ver perfil público de cualquier usuario |

**Auth requerida:** `Authorization: Bearer {{access_token}}` (rol `ACADEMY`)

---

## Body — POST y PATCH

| Campo        | Tipo   | Requerido | Validación                        |
|---|---|---|---|
| `academyName` | String | ✅ Sí     | No vacío (`@NotBlank`), máx. 120 chars, único en el sistema |
| `ruc`         | String | No        | Máx. 20 chars                     |
| `website`     | String | No        | Máx. 255 chars                    |
| `contactEmail`| String | No        | Formato email válido, máx. 120 chars |

---

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-01 | El rol de la cuenta debe ser `ACADEMY`. Cualquier otro rol → 403. |
| — | Solo puede existir un `academy_profile` por usuario. Segunda creación → 409. |
| — | `academyName` debe ser único en toda la plataforma. Duplicado → 409. |

---

## Respuesta exitosa — 201 Created (POST) / 200 OK (PATCH)

```json
{
  "profileId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "academyName": "Academia Preuniversitaria Lima",
  "ruc": "20123456789",
  "website": "https://academia-lima.pe",
  "contactEmail": "contacto@academia-lima.pe"
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso-campos-obligatorios.json` | Solo `academyName` | 201 Created |
| 02 | `caso-02-exitoso-todos-campos.json`         | Todos los campos completos | 201 Created |
| 03 | `caso-03-nombre-vacio.json`                 | `academyName` vacío | 400 Bad Request |
| 04 | `caso-04-nombre-duplicado.json`             | `academyName` ya existe en el sistema | 409 Conflict |
| 05 | `caso-05-tipo-incorrecto.json`              | Cuenta no es `ACADEMY` (ej. `TEACHER`) | 403 Forbidden |
| 06 | `caso-06-perfil-ya-existe.json`             | Segundo intento de creación | 409 Conflict |
| 07 | `caso-07-sin-autenticacion.json`            | Sin token | 401 Unauthorized |

---

## Flujo de uso

1. Registrar cuenta con `role: "ACADEMY"` → `POST /api/v1/auth/register`
2. Login → `POST /api/v1/auth/login` (obtener `{{access_token}}`)
3. Crear perfil de academia → `POST /api/v1/profiles/academy`
4. Actualizar datos → `PATCH /api/v1/profiles/academy/me`
5. Ver perfil público de otro usuario → `GET /api/v1/profiles/{userId}`
