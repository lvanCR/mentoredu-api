# HU14 — Search resources by filters

**Epic**: EP-04 Library  
**Endpoint**: `GET /api/v1/resources/search`  
**Auth**: No requerida (endpoint público)  
**Content-Type**: ninguno — parámetros por query string

---

## Descripción

Busca recursos académicos aplicando filtros opcionales combinables.  
Los recursos PRIVATE nunca aparecen en resultados (solo PUBLIC y PREMIUM).  
Devuelve lista vacía `[]` si no hay coincidencias (sin error).

## Filtros disponibles

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `q` | String | No | Texto libre en el título |
| `type` | String | No | `EXAM`, `SOLUTION`, `NOTES`, `PRACTICE`, `VIDEO`, `OTHER` |
| `visibility` | String | No | `PUBLIC` o `PREMIUM` (PRIVATE no permitido) |
| `institutionId` | UUID | No | ID de la institución |
| `subjectId` | UUID | No | ID de la materia/curso |
| `year` | Integer | No | Año del examen (1900–2099) |

---

## Casos de prueba

| Caso | Archivo | Descripción | HTTP |
|---|---|---|---|
| 01 | `caso-01.json` | Exitoso — búsqueda sin filtros | 200 |
| 02 | `caso-02.json` | Error — tipo de recurso inválido | 400 |
| 03 | `caso-03.json` | Alternativo exitoso — filtros válidos sin coincidencias (lista vacía) | 200 |
| 04 | `caso-04.json` | Alternativo error — filtros inconsistentes (year fuera de rango) | 400 |

---

## Nombre de requests en Postman

```
MentorEduLibraryHU14-SearchResourcesGET
```

---

## Variables requeridas

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |

> No requiere `{{access_token}}` — el endpoint es público.
