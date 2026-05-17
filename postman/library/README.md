# 📚 Library — Postman

Colección de casos de prueba para el Bounded Context **Library** (EP-04).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU12](./HU12-upload-pdf-resource/) | Subir archivo PDF académico | 4 | `POST /api/v1/resources/files` |
| [HU13](./HU13-register-resource-metadata/) | Registrar metadatos del recurso | 4 | `POST /api/v1/resources` |

## Pendientes

| HU | Descripción |
|---|---|
| HU14 | Buscar recursos por filtros |
| HU15 | Descargar recurso académico |

---

## Flujo de US12 → US13

US12 y US13 son pasos consecutivos del mismo flujo de publicación:

1. **US12** `POST /api/v1/resources/files` — sube el PDF, devuelve `{ id, fileUrl, ... }`
2. **US13** `POST /api/v1/resources` — registra los metadatos, referencia el archivo con `fileId`

El campo `id` devuelto en US12 debe usarse como `fileId` en el body de US13.

---

## Variables de entorno requeridas

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT obtenido en HU02 login |
| `{{file_id}}` | UUID devuelto por HU12 (POST /resources/files) |
| `{{institution_id}}` | UUID de una institución existente en BD |
| `{{subject_id}}` | UUID de una materia existente en BD |
