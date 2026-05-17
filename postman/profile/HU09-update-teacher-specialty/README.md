# HU09 — Actualizar especialidad del docente

**Epic**: EP-02 Profile  
**Endpoint**: `PATCH /api/v1/profiles/teacher/me`  
**Autenticación**: Bearer Token requerido  
**Nombre Postman**: `MentorEduProfileHU09-UpdateTeacherSpecialtyPATCH`

---

## Descripción

Permite al docente autenticado actualizar su especialidad y, opcionalmente, su institución y bio profesional, sin modificar el resto de campos del perfil.

**Flujo previo requerido**:
1. US01 — Registrar cuenta
2. US02 — Iniciar sesión (obtener token)
3. US04 — Seleccionar tipo de cuenta (TEACHER)
4. US08 — Crear perfil de docente

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
  "specialty": "string (obligatorio, no vacío)",
  "institutionName": "string (opcional)",
  "bioProfessional": "string (opcional)"
}
```

### Campos del body

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `specialty` | string | ✅ Sí | No puede estar vacío, máx. 120 caracteres |
| `institutionName` | string | No | Máx. 120 caracteres |
| `bioProfessional` | string | No | Máx. 2000 caracteres |

> Los campos opcionales se actualizan solo si se envían; los omitidos conservan su valor actual.

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-09 | El docente solo puede tener un perfil profesional |
| RN-11 | Solo se actualizan los campos enviados; los demás no se modifican |

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: perfil docente existe, specialty válida enviada | `caso-01-exitoso.json` | 200 OK |
| Alt exitoso: solo specialty, demás campos sin cambios | `caso-02-solo-specialty.json` | 200 OK |
| Error: specialty vacía | `caso-03-specialty-vacio.json` | 400 Bad Request |
| Alt error: perfil de docente no existe | `caso-04-perfil-no-existe.json` | 404 Not Found |
| Sin autenticación | `caso-05-sin-autenticacion.json` | 401 Unauthorized |

---

## Respuesta exitosa (200)

```json
{
  "profileId": "uuid",
  "specialty": "Química",
  "institutionName": "Instituto Preuniversitario El Triunfo",
  "bioProfessional": "Docente con 10 años de experiencia."
}
```
