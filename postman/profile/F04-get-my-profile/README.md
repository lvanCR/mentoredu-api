# F0.4 — Obtener mi perfil completo

**Epic**: EP-02 Profile (fix de infraestructura)  
**Bounded Context**: `profile`  
**Estado**: Implementado — 2026-05-21  
**Rama**: `feat/HU38-submit-appeal`  
**Nombre en Postman**: `MentorEduProfileF04-GetMyProfileGET`

---

## Endpoint

```
GET /api/v1/profiles/me
```

**Requiere autenticación**: `Authorization: Bearer <access_token>`

---

## Headers

| Header | Valor |
|---|---|
| `Authorization` | `Bearer {{token}}` |

Sin body (es un GET).

---

## Propósito

El frontend llama este endpoint inmediatamente después del login para decidir a qué dashboard redirigir al usuario:

| Condición | Acción del frontend |
|---|---|
| `profileType` es null / perfil no existe | Redirigir a selección de tipo de cuenta (US04) |
| `isProfileComplete = false` | Redirigir a completar perfil específico (US06/08/10) |
| `isProfileComplete = true` | Redirigir al dashboard principal |

---

## Respuesta exitosa

**Status**: `200 OK`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "displayName": "Ana García",
  "avatarUrl": null,
  "city": null,
  "bio": null,
  "profileType": "STUDENT",
  "isProfileComplete": true,
  "createdAt": "2026-05-21T10:30:00"
}
```

| Campo | Descripción |
|---|---|
| `id` | UUID del perfil base en `profiles` |
| `userId` | UUID del usuario en `users` |
| `displayName` | Nombre mostrado públicamente |
| `avatarUrl` | URL de foto de perfil (null si no se configuró) |
| `city` | Ciudad del usuario (null si no se configuró) |
| `bio` | Biografía del usuario (null si no se configuró) |
| `profileType` | `STUDENT`, `TEACHER` u `ORGANIZATION` |
| `isProfileComplete` | `true` si el subtipo específico existe (`student_profiles`, `teacher_profiles` u `organization_profiles`) |
| `createdAt` | Fecha de creación del perfil base |

---

## Escenarios de aceptación

### Caso 1 — Perfil completo (STUDENT)
**Archivo**: `caso-01-perfil-completo-student.json`  
**Prerequisito**: el usuario completó US04 y US06.  
**Status esperado**: `200 OK` con `isProfileComplete: true`

### Caso 2 — Perfil base sin completar
**Archivo**: `caso-02-perfil-sin-completar.json`  
**Prerequisito**: el usuario completó US04 pero NO US06/08/10.  
**Status esperado**: `200 OK` con `isProfileComplete: false`

### Caso 3 — Sin autenticación
**Archivo**: `caso-03-sin-autenticacion.json`  
**Status esperado**: `401 Unauthorized`

### Caso 4 — Sin perfil base
**Archivo**: `caso-04-sin-perfil-base.json`  
**Prerequisito**: el usuario se registró pero NO ejecutó US04.  
**Status esperado**: `404 Not Found`

---

## Cómo probar en Postman

1. Ejecuta primero `HU02-login/caso-01-exitoso.json` y guarda el `accessToken`.
2. Crea un request `GET` con URL `{{base_url}}/{{api_v1}}/profiles/me`.
3. En **Headers**, agrega `Authorization: Bearer {{token}}`.
4. Ejecuta y verifica el `status` y el campo `isProfileComplete`.
