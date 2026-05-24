# US09 — Buscar y filtrar recursos

**Epic**: EP-02 Library
**Bounded Context**: `library`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
GET /api/v1/resources
Authorization: Bearer {{access_token}}
```

Requiere autenticación. Todos los filtros son opcionales y combinables.

---

## Parámetros de query string

| Parámetro      | Tipo   | Descripción |
|---|---|---|
| `q`            | String | Texto libre en el título del recurso |
| `type`         | String | Tipo de recurso (ver valores válidos abajo) |
| `universityId` | UUID   | Filtrar por universidad |
| `areaId`       | UUID   | Filtrar por área (dentro de la universidad) |
| `careerId`     | UUID   | Filtrar por carrera |
| `courseId`     | UUID   | Filtrar por curso |
| `page`         | int    | Número de página (default: 0) |
| `size`         | int    | Elementos por página (default: 20) |

## Tipos de recurso válidos (`type`)

`EXAMEN_COMPLETO`, `EXAMEN_SECCION`, `GUIA`, `APUNTES`, `PRACTICA`, `OTRO`

> Recursos con visibilidad `PRIVATE` nunca aparecen en resultados (solo `PUBLIC` y `PREMIUM`).

---

## Respuesta exitosa — 200 OK

Devuelve `PagedResponse<ResourceResponse>`:

```json
{
  "content": [ { "id": "...", "title": "...", "resourceType": "EXAMEN_COMPLETO", ... } ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```

Lista vacía en `content` si no hay coincidencias — no es un error.

---

## Casos de prueba

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01.json` | Búsqueda sin filtros (todos los recursos) | 200 OK |
| 02 | `caso-02.json` | Filtro por `type` inválido | 400 Bad Request |
| 03 | `caso-03.json` | Filtro válido sin coincidencias (lista vacía) | 200 OK |
| 04 | `caso-04.json` | Filtro por `universityId` y `type` combinados | 200 OK |

---

## Variables requeridas

| Variable | Descripción |
|---|---|
| `{{access_token}}` | JWT obtenido en US02 login |
| `{{university_id}}` | UUID de universidad del seed V9 |
| `{{area_id}}` | UUID de área del seed V9 |
