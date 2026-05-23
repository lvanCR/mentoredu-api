# Library (US07–US11, US16)

Ciclo de vida de recursos académicos: subir, registrar, buscar, descargar y publicar ejercicios.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US07 | Subir archivo PDF | `POST /api/v1/resources/files` | `US07-upload-pdf/` |
| US08 | Registrar metadatos del recurso | `POST /api/v1/resources` | `US08-register-metadata/` |
| US09 | Buscar y filtrar recursos | `GET /api/v1/resources/search` | `US09-search-resources/` |
| US10 | Descargar un recurso | `GET /api/v1/resources/{id}/download` | `US10-download-resource/` |
| US11 | Ver mis recursos publicados | `GET /api/v1/resources/me` | `US11-my-resources/` |
| US16 | Publicar ejercicio sin solución | `POST /api/v1/resources` con `acepta_resoluciones=true` | `US16-publish-exercise/` |

## Flujo US07 → US08

1. `POST /api/v1/resources/files` (multipart/form-data) → devuelve `fileId`
2. `POST /api/v1/resources` con el `fileId` del paso anterior → devuelve el recurso creado

## Variables de entorno

`api_v1`, `access_token`, `teacher_token`, `file_id`, `resource_id`, `university_id`, `area_id`, `course_id`

## Notas

- Solo TEACHER, ACADEMY o ADMIN pueden subir recursos (RN-05).
- `acepta_resoluciones=true` solo es válido para `resource_type=PRACTICA` (RN-08).
- `course_id` es opcional para tipos `EXAMEN_COMPLETO`, `GUIA` y `APUNTES`.
- Si `career_id` está presente, debe pertenecer al mismo área que el recurso (RN-23).
