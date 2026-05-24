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

| # | Archivo | Escenario | Status esperado |
|---|---|---|---|
| 01 | `caso-01-listar-universidades.json` | ADMIN lista universidades disponibles | 200 OK |
| 02 | `caso-02-crear-universidad.json` | ADMIN crea nueva universidad | 201 Created |
| 03 | `caso-03-forbidden.json` | No-ADMIN intenta crear universidad | 403 Forbidden |

> **Faltan** casos para: crear área (POST /catalog/areas), crear carrera (POST /catalog/careers), nombre duplicado (409). Agregar `caso-04` a `caso-06` cuando se implementen.

## Nota

Los datos de V9 (7 universidades Lima, 18 cursos, 15 áreas) ya están en BD. Usar `GET` para obtener los UUIDs antes de crear perfiles o recursos.
