# US08 — Registrar metadatos de un recurso

**Epic**: EP-02 Library
**Bounded Context**: `library`
**Estado**: Implementada — 2026-05-22 `develop`
**Nombre en Postman**: `MentorEduLibraryUS08-RegisterResourcePOST`

---

## Endpoint

```
POST /api/v1/resources
Authorization: Bearer {{teacher_token}}
Content-Type: application/json
```

Solo TEACHER, ACADEMY o ADMIN pueden usar este endpoint (RN-05).

---

## Flujo obligatorio: US07 → US08

1. `POST /api/v1/resources/files` (multipart/form-data) → respuesta con `fileUrl`, `fileName`, `mimeType`, `sizeBytes`
2. Copiar esos 4 campos en el body de `POST /api/v1/resources`

---

## Body — Campos obligatorios

| Campo          | Tipo         | Descripción |
|---|---|---|
| `title`        | String       | Título del recurso. No vacío. |
| `universityId` | UUID         | ID de la universidad del catálogo (V9). |
| `areaId`       | UUID         | ID del área del catálogo. Debe pertenecer a `universityId`. |
| `resourceType` | String (enum)| Tipo de recurso. Ver valores válidos abajo. |
| `fileUrl`      | String       | URL del archivo devuelto por `POST /resources/files`. |
| `fileName`     | String       | Nombre del archivo devuelto por `POST /resources/files`. |
| `mimeType`     | String       | MIME type devuelto por `POST /resources/files` (`application/pdf`). |
| `sizeBytes`    | Long         | Tamaño en bytes devuelto por `POST /resources/files`. |

## Body — Campos opcionales

| Campo               | Tipo         | Descripción |
|---|---|---|
| `courseId`          | UUID         | Obligatorio para `EXAMEN_SECCION`, `PRACTICA` y `OTRO`. Opcional para `EXAMEN_COMPLETO`, `GUIA` y `APUNTES` (RN-07). |
| `careerId`          | UUID         | Siempre opcional. Si presente, debe pertenecer al `areaId` enviado (RN-23). |
| `visibility`        | String       | `PUBLIC` (default), `PREMIUM` o `PRIVATE`. |
| `description`       | String       | Descripción libre del recurso. |
| `aceptaResoluciones`| Boolean      | `true` solo válido para `resourceType = PRACTICA` (RN-08). Solo TEACHER/ACADEMY/ADMIN pueden activarlo (RN-05). |

---

## Tipos de recurso válidos (`resourceType`)

| Valor            | `courseId` requerido |
|---|---|
| `EXAMEN_COMPLETO` | No (abarca el área completa) |
| `EXAMEN_SECCION`  | Sí |
| `GUIA`            | No |
| `APUNTES`         | No |
| `PRACTICA`        | Sí |
| `OTRO`            | Sí |

---

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-05  | Solo TEACHER, ACADEMY o ADMIN pueden registrar recursos. STUDENT → 403. |
| RN-06  | Solo los 6 tipos de recurso listados son válidos. Otro valor → 400. |
| RN-07  | `courseId` obligatorio para `EXAMEN_SECCION`, `PRACTICA` y `OTRO`. |
| RN-08  | `aceptaResoluciones=true` solo válido para `PRACTICA`. Otro tipo → 400. |
| RN-23  | Si `careerId` presente, la carrera debe pertenecer al `areaId` enviado. Inconsistencia → 400. |

---

## Respuesta exitosa — 201 Created

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Examen UNMSM 2024-I — Área A",
  "resourceType": "EXAMEN_COMPLETO",
  "visibility": "PUBLIC",
  "description": null,
  "universityId": "b1000000-0000-0000-0000-000000000001",
  "areaId": "b3000000-0000-0000-0000-000000000001",
  "careerId": null,
  "courseId": null,
  "fileUrl": "uploads/resources/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileName": "examen-unmsm-2024-i.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 204800,
  "aceptaResoluciones": false,
  "createdAt": "2026-05-22T10:00:00",
  "authorId": "uuid-del-docente",
  "authorName": "Juan Pérez"
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso-examen-completo.json`     | `EXAMEN_COMPLETO` sin `courseId` | 201 Created |
| 02 | `caso-02-exitoso-practica-con-course.json` | `PRACTICA` con `courseId` | 201 Created |
| 03 | `caso-03-sin-title.json`                   | `title` faltante | 400 Bad Request |
| 04 | `caso-04-sin-university.json`              | `universityId` faltante | 400 Bad Request |
| 05 | `caso-05-course-requerido.json`            | `EXAMEN_SECCION` sin `courseId` | 400 Bad Request |
| 06 | `caso-06-career-inconsistente.json`        | `careerId` no pertenece al `areaId` enviado | 400 Bad Request |
| 07 | `caso-07-acepta-resoluciones-tipo-incorrecto.json` | `aceptaResoluciones=true` en `GUIA` | 400 Bad Request |
| 08 | `caso-08-student-intenta-publicar.json`    | Usuario con rol STUDENT | 403 Forbidden |

---

## Variables necesarias

| Variable        | Descripción |
|---|---|
| `{{teacher_token}}`  | JWT de un usuario TEACHER o ACADEMY |
| `{{university_id}}`  | UUID de universidad (del seed V9) |
| `{{area_id}}`        | UUID de área (del seed V9, debe pertenecer a `university_id`) |
| `{{course_id}}`      | UUID de curso (del seed V9, requerido según tipo) |
| `{{file_url}}`       | `fileUrl` obtenido de `POST /resources/files` (US07) |
| `{{file_name}}`      | `fileName` obtenido de `POST /resources/files` |
| `{{mime_type}}`      | `mimeType` obtenido de `POST /resources/files` |
| `{{size_bytes}}`     | `sizeBytes` obtenido de `POST /resources/files` |
