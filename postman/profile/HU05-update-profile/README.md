# HU05 — Actualizar datos comunes del perfil

**Endpoint:** `PATCH /api/v1/profiles/me`  
**Auth requerida:** `Authorization: Bearer {{access_token}}`  
**Nombre Postman:** `MentorEduProfileHU05-UpdateProfilePATCH`

## Campos del body

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `displayName` | String | ✅ Sí | Nombre visible. 1–120 caracteres. |
| `avatarUrl` | String | No | URL del avatar. Puede ser `null`. |
| `city` | String | No | Ciudad de residencia. Máx. 80 caracteres. |
| `bio` | String | No | Descripción breve. Sin límite. |

> `profileType` NO puede modificarse mediante este endpoint.

## Casos

| Archivo | Escenario | HTTP esperado |
|---|---|---|
| `caso-01-exitoso.json` | Datos comunes válidos → perfil actualizado | 200 OK |
| `caso-02-solo-nombre-ciudad.json` | Solo displayName y city → actualización parcial | 200 OK |
| `caso-03-displayname-vacio.json` | displayName vacío → error de validación | 400 Bad Request |
| `caso-04-sin-displayname.json` | displayName ausente → error de validación | 400 Bad Request |
| `caso-05-sin-autenticacion.json` | Sin token → acceso denegado | 401 Unauthorized |
