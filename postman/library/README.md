# 📚 Library — Postman

Colección de casos de prueba para el Bounded Context **Library** (EP-04).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU12](./HU12-upload-pdf-resource/) | Subir archivo PDF académico | 4 | `POST /api/v1/resources/files` |
| [HU13](./HU13-register-resource-metadata/) | Registrar metadatos del recurso | 4 | `POST /api/v1/resources` |
| [HU14](./HU14-search-resources/) | Buscar recursos por filtros | 4 | `GET /api/v1/resources/search` |
| [HU15](./HU15-download-resource/) | Descargar recurso académico | 4 | `GET /api/v1/resources/{id}/download` |

---

## Flujo completo US12 → US13 → US14 → US15

1. **US12** `POST /api/v1/resources/files` — sube el PDF, devuelve `{ id, fileUrl, ... }`
2. **US13** `POST /api/v1/resources` — registra los metadatos, referencia el archivo con `fileId`
3. **US14** `GET /api/v1/resources/search` — busca recursos públicos por filtros opcionales
4. **US15** `GET /api/v1/resources/{id}/download` — descarga el PDF (requiere JWT + permisos según visibilidad)

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT obtenido en HU02 login | HU12, HU13, HU15 |
| `{{file_id}}` | UUID devuelto por HU12 | HU13 |
| `{{resource_id}}` | UUID devuelto por HU13 | HU15 |
| `{{institution_id}}` | UUID de una institución existente en BD | HU13, HU14 |
| `{{subject_id}}` | UUID de una materia existente en BD | HU13, HU14 |
