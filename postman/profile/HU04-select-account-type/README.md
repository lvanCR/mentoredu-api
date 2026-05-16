# HU04 — Seleccionar tipo de cuenta

**Epic**: EP-02 Profile  
**Bounded Context**: `profile`  
**Estado**: Implementada — 2026-05-16  
**Rama**: `feat/HU04-select-account-type`  
**Nombre en Postman**: `MentorEduProfileHU04-SelectAccountTypePOST`

---

## Endpoint

```
POST /api/v1/profiles/account-type
```

Requiere autenticación (`Authorization: Bearer <access_token>`).

---

## Headers

| Header | Valor |
|---|---|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer {{access_token}}` |

---

## Body — Campos requeridos

| Campo | Tipo | Validación |
|---|---|---|
| `profileType` | String (enum) | Obligatorio. Valores: `STUDENT`, `TEACHER`, `ORGANIZATION` |

---

## Reglas de negocio aplicables

| Código | Regla |
|---|---|
| RN-05 | Un usuario solo puede tener un perfil base. |
| RN-07 | Un cambio de tipo de cuenta debe ser consistente con el perfil creado. Una vez definido, no puede cambiarse. |

---

## Respuesta exitosa

**Status**: `201 Created`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "Juan Pérez",
  "profileType": "STUDENT",
  "createdAt": "2026-05-16T10:00:00"
}
```

---

## Escenarios de aceptación

### Caso 1 — Selección exitosa como STUDENT

**Archivo**: `caso-01-exitoso-student.json`  
**Gherkin (Escenarios 1 y 3)**:
> Dado que mi cuenta aún no tiene tipo asignado, cuando selecciono STUDENT, entonces el sistema guarda la elección y devuelve el perfil creado.

**Status esperado**: `201 Created`

---

### Caso 2 — Selección exitosa como TEACHER

**Archivo**: `caso-02-exitoso-teacher.json`  
**Status esperado**: `201 Created`

---

### Caso 3 — Selección exitosa como ORGANIZATION

**Archivo**: `caso-03-exitoso-organization.json`  
**Status esperado**: `201 Created`

---

### Caso 4 — Perfil ya existe

**Archivo**: `caso-04-perfil-ya-existe.json`  
**Gherkin (Escenario 2)**:
> Dado que la cuenta ya tiene perfil definido, cuando intento cambiarlo, entonces el sistema rechaza la operación.

**Prerequisito**: ejecutar cualquier Caso 1–3 primero con el mismo usuario.  
**Status esperado**: `409 Conflict`

---

### Caso 5 — Tipo de cuenta no permitido

**Archivo**: `caso-05-tipo-invalido.json`  
**Gherkin (Escenario alternativo error)**:
> Dado que intento seleccionar un tipo no permitido por el sistema, cuando envío la solicitud, entonces el sistema devuelve un error de validación.

**Status esperado**: `400 Bad Request`

---

## Cómo probar en Postman

1. Activa el ambiente **MentorEdu — Local**.
2. Ejecuta HU02 (login) para obtener `access_token` y guárdalo en la variable de entorno.
3. Crea un request `POST` con URL `{{base_url}}/{{api_v1}}/profiles/account-type`.
4. En **Headers**: `Content-Type: application/json` y `Authorization: Bearer {{access_token}}`.
5. En **Body → raw → JSON**, pega el contenido del `caso-XX.json` correspondiente.
6. Para el Caso 4, ejecuta primero el Caso 1 con el mismo usuario.
