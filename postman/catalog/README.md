# Catalog (US28)

Administración del catálogo del sistema: universidades, áreas, cursos y carreras.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US28 | Administrar catálogo (solo ADMIN) | `POST/GET /api/v1/catalog/universities` · `POST/GET /api/v1/catalog/areas` · `POST/GET /api/v1/catalog/courses` · `POST/GET /api/v1/catalog/careers` | `US28-manage-catalog/` |

## Variables de entorno

`api_v1`, `admin_token`, `university_id`, `area_id`, `course_id`, `career_id`

## Notas

- Solo usuarios con rol ADMIN pueden crear o modificar entidades del catálogo (RN-20).
- Los datos maestros pre-cargados en V9 (7 universidades Lima, 18 cursos, 15 áreas) están disponibles sin necesidad de crearlos.
- Usar `GET /api/v1/catalog/universities` para obtener los UUIDs necesarios en los flujos de profile y library.
