# HU07 — Actualizar universidad objetivo del estudiante

**Epic**: EP-02 Profile  
**Endpoint**: `PATCH /api/v1/profiles/student/me`  
**Autenticación**: Bearer Token requerido  
**Nombre Postman**: `MentorEduProfileHU07-UpdateTargetUniversityPATCH`

---

## Descripción

Permite al estudiante autenticado actualizar su universidad objetivo sin modificar el resto de campos de su perfil académico.

**Flujo previo requerido**:
1. US01 — Registrar cuenta
2. US02 — Iniciar sesión (obtener token)
3. US04 — Seleccionar tipo de cuenta (STUDENT)
4. US06 — Crear perfil de estudiante

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
  "targetUniversity": "string (obligatorio, no vacío)"
}
```

### Campos del body

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `targetUniversity` | string | ✅ Sí | No puede estar vacío, máx. 120 caracteres |
| `schoolName` | string | No | Máx. 120 caracteres |
| `gradeLevel` | string | No | Máx. 20 caracteres |
| `targetCareer` | string | No | Máx. 120 caracteres |
| `studyShift` | string | No | Máx. 30 caracteres |

> Los campos opcionales se actualizan solo si se envían; los omitidos conservan su valor actual.

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-08 | El estudiante solo puede tener un perfil académico principal |
| RN-11 | Solo se actualizan los campos enviados; los demás no se modifican |

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: perfil existe, targetUniversity válida enviada | `caso-01-exitoso.json` | 200 OK |
| Alt exitoso: solo targetUniversity, demás campos sin cambios | `caso-02-solo-target-university.json` | 200 OK |
| Error: targetUniversity vacío | `caso-03-targetuniversity-vacio.json` | 400 Bad Request |
| Alt error: perfil de estudiante no existe | `caso-04-perfil-no-existe.json` | 404 Not Found |
| Sin autenticación | `caso-05-sin-autenticacion.json` | 401 Unauthorized |

---

## Respuesta exitosa (200)

```json
{
  "profileId": "uuid",
  "schoolName": "Colegio Nacional",
  "gradeLevel": "5TO_SECUNDARIA",
  "targetUniversity": "Universidad Nacional Mayor de San Marcos",
  "targetCareer": "Ingeniería de Sistemas",
  "studyShift": "MAÑANA"
}
```
