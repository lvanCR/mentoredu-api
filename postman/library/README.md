# 📚 Library — Postman

Colección de casos de prueba para el Bounded Context **Library** (EP-04).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU12](./HU12-upload-pdf-resource/) | Subir archivo PDF académico | 4 | `POST /api/v1/resources/files` |
| [HU13](./HU13-register-resource-metadata/) | Registrar metadatos del recurso | 4 | `POST /api/v1/resources` |
| [HU14](./HU14-search-resources/) | Buscar recursos por filtros | 4 | `GET /api/v1/resources/search` |

## Pendientes

| HU | Descripción |
|---|---|
| HU15 | Descargar recurso académico |

---

## Flujo de US12 → US13 → US14

1. **US12** `POST /api/v1/resources/files` — sube el PDF, devuelve `{ id, fileUrl, ... }`
2. **US13** `POST /api/v1/resources` — registra los metadatos, referencia el archivo con `fileId`
3. **US14** `GET /api/v1/resources/search` — busca recursos públicos por filtros opcionales

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT obtenido en HU02 login | HU12, HU13 |
| `{{file_id}}` | UUID devuelto por HU12 | HU13 |
| `{{institution_id}}` | UUID de una institución existente en BD | HU13, HU14 |
| `{{subject_id}}` | UUID de una materia existente en BD | HU13, HU14 |
