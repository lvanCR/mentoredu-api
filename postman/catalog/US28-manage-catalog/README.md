# US28 — Administrar catálogo del sistema

Solo usuarios con rol ADMIN pueden crear, modificar y consultar universidades, áreas, cursos y carreras (RN-20).

**Endpoints:**
- `POST   /api/v1/catalog/universities` — crear universidad
- `GET    /api/v1/catalog/universities` — listar universidades
- `POST   /api/v1/catalog/areas` — crear área (pertenece a una universidad)
- `GET    /api/v1/catalog/areas` — listar áreas
- `POST   /api/v1/catalog/courses` — crear curso
- `GET    /api/v1/catalog/courses` — listar cursos
- `POST   /api/v1/catalog/careers` — crear carrera
- `GET    /api/v1/catalog/careers` — listar carreras

**Headers:** `Authorization: Bearer {{admin_token}}`, `Content-Type: application/json`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | ADMIN lista universidades disponibles | 200 OK |
| 02 | ADMIN crea nueva universidad | 201 Created |
| 03 | ADMIN crea área dentro de una universidad | 201 Created |
| 04 | No-ADMIN intenta crear una universidad | 403 Forbidden |
| 05 | Nombre de universidad duplicado en la misma universidad | 409 Conflict |

## Nota

Los datos de V9 (7 universidades Lima, 18 cursos, 15 áreas) ya están en BD. Usar `GET` para obtener los UUIDs antes de crear perfiles o recursos.
