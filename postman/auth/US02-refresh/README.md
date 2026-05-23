# F0.5 — Renovar access token con refresh token

**Epic**: EP-01 Auth  
**Bounded Context**: `auth`  
**Estado**: Implementado — 2026-05-21  
**Rama**: `feat/HU38-submit-appeal` (fix de infraestructura)  
**Nombre en Postman**: `MentorEduAuthF05-RefreshTokenPOST`

---

## Endpoint

```
POST /api/v1/auth/refresh
```

No requiere `Authorization` header (el refresh token viaja en el body).

---

## Headers

| Header | Valor |
|---|---|
| `Content-Type` | `application/json` |

---

## Body — Campos requeridos

| Campo | Tipo | Validación |
|---|---|---|
| `refreshToken` | String | Obligatorio (`@NotBlank`). El token devuelto por `POST /auth/login` en el campo `refreshToken`. |

---

## Flujo de uso

1. El usuario hace login (`POST /auth/login`) y recibe `accessToken` (15 min) y `refreshToken` (7 días).
2. Cuando el `accessToken` expira, el frontend envía el `refreshToken` a este endpoint.
3. El sistema devuelve un nuevo `accessToken` sin necesidad de que el usuario ingrese credenciales otra vez.

---

## Reglas de negocio

| Condición | Respuesta |
|---|---|
| Refresh token válido y no revocado | `200 OK` con nuevo `accessToken` |
| Refresh token no encontrado | `401 Unauthorized` |
| Refresh token revocado (`revokedAt != null`) | `401 Unauthorized` |
| Refresh token expirado (`expiresAt < now`) | `401 Unauthorized` |
| Campo `refreshToken` ausente o vacío | `400 Bad Request` |

---

## Respuesta exitosa

**Status**: `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

| Campo | Descripción |
|---|---|
| `accessToken` | Nuevo JWT de acceso. Usar como `Authorization: Bearer <token>` en el resto de la API. |
| `expiresIn` | Tiempo de vida en segundos (3600 en local, 900 en producción). |

---

## Escenarios de aceptación

### Caso 1 — Renovación exitosa
**Archivo**: `caso-01-exitoso.json`  
**Prerequisito**: ejecutar `POST /auth/login` y guardar el `refreshToken`.  
**Status esperado**: `200 OK`

### Caso 2 — Refresh token revocado
**Archivo**: `caso-02-token-revocado.json`  
**Status esperado**: `401 Unauthorized`

### Caso 3 — Refresh token expirado
**Archivo**: `caso-03-token-expirado.json`  
**Status esperado**: `401 Unauthorized`

### Caso 4 — Campo obligatorio ausente
**Archivo**: `caso-04-campo-faltante.json`  
**Status esperado**: `400 Bad Request`

---

## Cómo probar en Postman

1. Ejecuta primero `HU02-login/caso-01-exitoso.json` y copia el `refreshToken` de la respuesta.
2. Crea un request `POST` con URL `{{base_url}}/{{api_v1}}/auth/refresh`.
3. En **Headers**, agrega `Content-Type: application/json`.
4. En **Body → raw → JSON**, pega el cuerpo del `caso-XX.json` correspondiente.
5. Verifica que el `status` coincida con `expected_response.status`.
