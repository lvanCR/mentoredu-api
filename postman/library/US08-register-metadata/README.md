# HU13 — Register resource metadata

**Epic**: EP-04 Library  
**Endpoint**: `POST /api/v1/resources`  
**Auth**: Bearer token requerido  
**Content-Type**: `application/json`

---

## Descripción

Registra los metadatos de un recurso académico. Debe ejecutarse después de US12 (subir el PDF).  
El campo `fileId` debe ser el `id` devuelto por `POST /api/v1/resources/files`.

## Campos obligatorios (RN-12)

| Campo | Tipo | Descripción |
|---|---|---|
| `title` | String | Título del recurso |
| `fileId` | UUID | ID del archivo subido en US12 |
| `institutionId` | UUID | ID de la institución |
| `subjectId` | UUID | ID del curso/materia |
| `year` | Integer | Año del examen (1900–2099) |
| `type` | String | Categoría: `EXAM`, `SOLUTION`, `NOTES`, `PRACTICE`, `VIDEO`, `OTHER` |

## Campos opcionales

| Campo | Tipo | Descripción |
|---|---|---|
| `visibility` | String | `PUBLIC` (default), `PREMIUM`, `PRIVATE` |
| `description` | String | Descripción del recurso |
| `examCycle` | String | Ciclo del examen (ej: `2024-I`) |

---

## Casos de prueba

| Caso | Archivo | Descripción | HTTP |
|---|---|---|---|
| 01 | `caso-01.json` | Exitoso — campos mínimos requeridos | 201 |
| 02 | `caso-02.json` | Error — falta campo obligatorio (`title`) | 400 |
| 03 | `caso-03.json` | Alternativo exitoso — todos los campos incluidos | 201 |
| 04 | `caso-04.json` | Alternativo error — año fuera de rango | 400 |

---

## Nombre de requests en Postman

```
MentorEduLibraryHU13-RegisterResourcePOST
```

---

## Variables requeridas

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT obtenido en HU02 login |
| `{{file_id}}` | UUID devuelto por HU12 (POST /resources/files) |
| `{{institution_id}}` | UUID de una institución existente |
| `{{subject_id}}` | UUID de una materia existente |
