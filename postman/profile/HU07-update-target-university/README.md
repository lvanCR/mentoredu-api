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
| `targetUniversity` | string | ✅ Sí | No puede estar vacío |
| `schoolName` | string | No | — |
| `gradeLevel` | string | No | — |
| `targetCareer` | string | No | — |
| `studyShift` | string | No | — |

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-08 | El estudiante solo puede tener un perfil académico activo |
| RN-11 | Solo se actualizan los campos enviados; los omitidos no se modifican |

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: universidad válida enviada, perfil existe | `caso-01-exitoso.json` | 200 OK |
| Alt exitoso: solo targetUniversity, otros campos no se tocan | `caso-02-solo-target-university.json` | 200 OK |
| Error: targetUniversity vacío | `caso-03-targetuniversity-vacio.json` | 400 Bad Request |
| Alt error: perfil de estudiante no existe | `caso-04-perfil-no-existe.json` | 404 Not Found |

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
