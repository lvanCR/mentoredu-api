# US28 — Administrar catálogo del sistema

Solo usuarios con rol ADMIN pueden crear y modificar entradas del catálogo (RN-20). Consultas `GET` son públicas (requieren autenticación pero no rol específico).

**Endpoints completos:**

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET`  | `/api/v1/catalog/universities` | cualquiera | Listar universidades |
| `POST` | `/api/v1/catalog/universities` | ADMIN | Crear universidad |
| `GET`  | `/api/v1/catalog/universities/{id}/areas` | cualquiera | Listar áreas de una universidad |
| `POST` | `/api/v1/catalog/universities/{id}/areas` | ADMIN | Crear área en una universidad |
| `GET`  | `/api/v1/catalog/courses` | cualquiera | Listar todos los cursos |
| `GET`  | `/api/v1/catalog/areas/{areaId}/courses` | cualquiera | Listar cursos de un área |
| `POST` | `/api/v1/catalog/courses` | ADMIN | Crear curso |
| `PUT`  | `/api/v1/catalog/areas/{areaId}/courses/{courseId}` | ADMIN | Asociar curso a área (idempotente) |
| `GET`  | `/api/v1/catalog/universities/{id}/careers` | cualquiera | Listar carreras de una universidad |
| `POST` | `/api/v1/catalog/universities/{id}/careers` | ADMIN | Crear carrera en una universidad |
| `PUT`  | `/api/v1/catalog/careers/{careerId}/courses/{courseId}` | ADMIN | Asociar curso a carrera (idempotente) |

> **Nota crítica:** Los endpoints de asociación usan `PUT` (no `POST`) — semántica idempotente. Una segunda llamada con el mismo par no genera error.

**Headers:** `Authorization: Bearer {{admin_token}}`, `Content-Type: application/json`

## Casos

| # | Archivo | Escenario | Status esperado |
|---|---|---|---|
| 01 | `caso-01-listar-universidades.json` | ADMIN lista universidades disponibles | 200 OK |
| 02 | `caso-02-crear-universidad.json` | ADMIN crea nueva universidad | 201 Created |
| 03 | `caso-03-forbidden.json` | No-ADMIN intenta crear universidad | 403 Forbidden |
| 04 | `caso-04-link-course-to-area.json` | ADMIN asocia curso a área | 204 No Content |
| 05 | `caso-05-link-course-to-career.json` | ADMIN asocia curso a carrera | 204 No Content |

## Nota

Los datos de V9 (7 universidades Lima, 18 cursos, 15 áreas) ya están en BD. Usar `GET /catalog/universities` para obtener los UUIDs de universidades antes de crear perfiles o recursos. Los UUIDs de areas/cursos también se consultan por GET.
