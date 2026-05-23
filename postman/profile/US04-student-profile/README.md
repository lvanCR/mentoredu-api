# HU06 — Crear perfil de estudiante

**Endpoint:** `POST /api/v1/profiles/student`  
**Auth requerida:** `Authorization: Bearer {{access_token}}`  
**Nombre Postman:** `MentorEduProfileHU06-CreateStudentProfilePOST`

## Prerrequisito

El usuario debe haber ejecutado **HU04** con `profileType: STUDENT`. Sin perfil base, devuelve 404.

## Campos del body

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `gradeLevel` | String | ✅ Sí | Grado académico. Ej.: `5TO_SECUNDARIA`, `4TO_SECUNDARIA`, `EGRESADO`. Máx. 20 chars. |
| `targetUniversity` | String | ✅ Sí | Universidad objetivo. Máx. 120 chars. |
| `schoolName` | String | No | Colegio de procedencia. Máx. 120 chars. |
| `targetCareer` | String | No | Área/carrera objetivo. Máx. 120 chars. |
| `studyShift` | String | No | Turno de estudio. Ej.: `MAÑANA`, `TARDE`. Máx. 30 chars. |

## Restricciones de negocio

- **RN-08**: Solo un perfil académico por estudiante. Segundo intento → 409.
- La cuenta debe ser de tipo `STUDENT`. Tipo distinto → 409.
- El criterio Gherkin menciona "área de preparación" — corresponde al campo `targetCareer` del DTO (opcional, nullable en BD según diagrama-er.puml).

## Casos

| Archivo | Escenario | HTTP esperado |
|---|---|---|
| `caso-01-exitoso-campos-obligatorios.json` | Solo gradeLevel + targetUniversity | 201 Created |
| `caso-02-exitoso-todos-campos.json` | Todos los campos completos | 201 Created |
| `caso-03-gradelevel-vacio.json` | gradeLevel vacío | 400 Bad Request |
| `caso-04-targetuniversity-vacio.json` | targetUniversity vacío | 400 Bad Request |
| `caso-05-perfil-ya-existe.json` | Segundo intento de creación | 409 Conflict |
| `caso-06-tipo-incorrecto.json` | Cuenta no es STUDENT (ej. TEACHER) | 409 Conflict |
| `caso-07-sin-perfil-base.json` | US04 no ejecutada | 404 Not Found |
| `caso-08-sin-autenticacion.json` | Sin token | 401 Unauthorized |
