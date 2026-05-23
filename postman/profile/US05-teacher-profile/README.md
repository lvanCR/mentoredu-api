# HU08 — Crear perfil de docente

**Epic**: EP-02 Profile  
**Endpoint**: `POST /api/v1/profiles/teacher`  
**Autenticación**: Bearer Token requerido  
**Nombre Postman**: `MentorEduProfileHU08-CreateTeacherProfilePOST`

---

## Descripción

Permite al usuario autenticado con tipo de cuenta TEACHER crear su perfil profesional. Solo puede ejecutarse una vez por usuario (RN-09).

**Flujo previo requerido**:
1. US01 — Registrar cuenta
2. US02 — Iniciar sesión (obtener token)
3. US04 — Seleccionar tipo de cuenta (TEACHER)

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
  "specialty": "string (obligatorio)",
  "institutionName": "string (obligatorio)",
  "bioProfessional": "string (opcional)"
}
```

### Campos del body

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `specialty` | string | ✅ Sí | No vacío, máx. 120 caracteres |
| `institutionName` | string | ✅ Sí | No vacío, máx. 120 caracteres |
| `bioProfessional` | string | No | Máx. 2000 caracteres |

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-09 | Un docente solo puede tener un perfil profesional |
| RN-11 | Solo se registran los campos correspondientes al tipo de perfil |

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: cuenta TEACHER, especialidad e institución válidos | `caso-01-exitoso-campos-obligatorios.json` | 201 Created |
| Alt exitoso: todos los campos válidos | `caso-02-exitoso-todos-campos.json` | 201 Created |
| Error: specialty vacío | `caso-03-specialty-vacio.json` | 400 Bad Request |
| Error: institutionName vacío | `caso-04-institution-vacio.json` | 400 Bad Request |
| Alt error: specialty no presente | `caso-05-specialty-faltante.json` | 400 Bad Request |
| Error: tipo de cuenta incorrecto (no TEACHER) | `caso-06-tipo-incorrecto.json` | 409 Conflict |
| Error: perfil de docente ya existe | `caso-07-perfil-ya-existe.json` | 409 Conflict |
| Error: perfil base no existe (US04 no ejecutada) | `caso-08-sin-perfil-base.json` | 404 Not Found |

---

## Respuesta exitosa (201)

```json
{
  "profileId": "uuid",
  "specialty": "Matemáticas",
  "institutionName": "Instituto Preuniversitario El Triunfo",
  "bioProfessional": null
}
```
