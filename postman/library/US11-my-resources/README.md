# US11 — Ver mis recursos publicados

Permite a un docente o academia listar todos los recursos que ha publicado.

**Endpoint:** `GET /api/v1/resources/me`

**Headers:** `Authorization: Bearer {{access_token}}`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Lista de recursos del usuario autenticado | 200 OK |
| 02 | Lista vacía (usuario sin recursos) | 200 OK `[]` |
| 03 | Sin autenticación | 401 Unauthorized |
