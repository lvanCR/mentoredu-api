# HU38 — Submit appeal for moderation decision

**Endpoint**: `POST /api/v1/moderation/appeals`
**Auth**: Bearer Token (JWT)

## Escenarios

| Caso | Descripción | Status esperado |
|---|---|---|
| caso-01 | Apelación exitosa con motivo válido | 201 Created |
| caso-02 | Motivo omitido | 400 Bad Request |
| caso-03 | Apelación duplicada para el mismo reporte | 409 Conflict |
| caso-04 | Reporte inexistente | 404 Not Found |

## Variables requeridas

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario que apela |
| `{{report_id}}` | UUID de un reporte existente en BD |
| `{{nonexistent_id}}` | UUID inexistente en BD |
