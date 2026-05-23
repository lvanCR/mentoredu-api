# US17 — Ver resoluciones de mis ejercicios

Permite a un docente (o academia) ver todas las resoluciones enviadas a un ejercicio que publicó.

**Endpoint:** `GET /api/v1/resources/{id}/solutions`

**Headers:** `Authorization: Bearer {{teacher_token}}`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Docente lista resoluciones de su ejercicio | 200 OK |
| 02 | Ejercicio sin resoluciones | 200 OK `[]` |
| 03 | Usuario no es el autor del ejercicio | 403 Forbidden |
| 04 | Recurso no existe | 404 Not Found |
