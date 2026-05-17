# HU33 — Crear academia

**Epic**: EP-03 Academy  
**Endpoint**: `POST /api/v1/academies`  
**Autenticación**: Bearer Token requerido  
**Nombre Postman**: `MentorEduAcademyHU33-CreateAcademyPOST`

---

## Descripción

Permite a la organización autenticada registrar una nueva academia en la plataforma. El nombre de la academia debe ser único por organización. Una misma organización puede registrar múltiples academias con nombres distintos.

**Flujo previo requerido**:
1. US01 — Registrar cuenta
2. US02 — Iniciar sesión (obtener token)
3. US04 — Seleccionar tipo de cuenta (ORGANIZATION)
4. US10 — Crear perfil de organización

**Flujo siguiente**:
5. US11 — Registrar oferta académica

---

## Headers requeridos

| Header | Valor |
|---|---|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer {{access_token}}` |

---

## Body (JSON)

```json
{
  "name": "string (obligatorio, no vacío)",
  "description": "string (opcional)",
  "website": "string (opcional)",
  "email": "email válido (opcional)"
}
```

### Campos del body

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `name` | string | ✅ Sí | No puede estar vacío, máx. 120 caracteres, único por organización |
| `description` | string | No | Sin restricción de longitud |
| `website` | string | No | Máx. 255 caracteres |
| `email` | string | No | Formato email válido, máx. 120 caracteres |

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-10 | Una organización solo puede tener un perfil institucional activo (prerequisito US10) |

> No existe límite en la cantidad de academias por organización. El nombre debe ser único **dentro de la misma organización**, no globalmente.

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: nombre válido (campos obligatorios) | `caso-01-exitoso-campos-obligatorios.json` | 201 Created |
| Alt exitoso: todos los campos | `caso-02-exitoso-todos-campos.json` | 201 Created |
| Error: name vacío | `caso-03-name-vacio.json` | 400 Bad Request |
| Alt error: academia con mismo nombre ya existe en esta org | `caso-04-nombre-duplicado.json` | 409 Conflict |
| Error: tipo de cuenta incorrecto (no ORGANIZATION) | `caso-05-tipo-incorrecto.json` | 409 Conflict |
| Error: sin perfil base (US04 no ejecutada) | `caso-06-sin-perfil-base.json` | 404 Not Found |
| Error: sin perfil de organización (US10 no ejecutada) | `caso-07-sin-perfil-organizacion.json` | 404 Not Found |
| Sin autenticación | `caso-08-sin-autenticacion.json` | 401 Unauthorized |

---

## Respuesta exitosa (201)

```json
{
  "id": "uuid",
  "ownerProfileId": "uuid",
  "name": "Academia Preuniversitaria Norte",
  "description": "Centro de preparación universitaria en Lima Norte.",
  "website": "https://academia-norte.pe",
  "email": "info@academia-norte.pe",
  "active": true,
  "createdAt": "2026-05-16T20:00:00",
  "updatedAt": "2026-05-16T20:00:00"
}
```

---

## Respuestas de error

### 400 Bad Request — name vacío o faltante
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation failed",
  "details": {
    "name": "name is required"
  }
}
```

### 401 Unauthorized — sin token
```json
{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized"
}
```

### 404 Not Found — perfil no encontrado
```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Profile not found for user: org@example.com"
}
```

### 409 Conflict — nombre duplicado por la misma org
```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "An academy with this name already exists for this organization: Academia Preuniversitaria Norte"
}
```

### 409 Conflict — tipo de cuenta incorrecto
```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "Account type is not ORGANIZATION. Current type: STUDENT"
}
```
