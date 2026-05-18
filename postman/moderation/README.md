# 🛡️ Moderation — Postman

Colección de casos de prueba para el Bounded Context **Moderation** (EP-06).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU19](./HU19-report-content/) | Reportar contenido | 4 | `POST /api/v1/moderation/reports` |
| [HU20](./HU20-resolve-report/) | Resolver un reporte | 4 | `PATCH /api/v1/moderation/reports/{id}/resolve` |

---

## Pendientes

*(EP-06 completo)*

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT del usuario reportante (obtenido en HU02) | HU19 |
| `{{own_content_token}}` | JWT del autor del contenido a reportar | HU19 (caso-03) |
| `{{thread_id}}` | UUID de un hilo existente | HU19 |
| `{{nonexistent_id}}` | UUID inexistente en BD | HU19 (caso-04) |
