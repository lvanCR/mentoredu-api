# US19 — Dar feedback correctivo

Permite al autor del ejercicio dar feedback a una resolución de un estudiante.

**Endpoint:** `POST /api/v1/solutions/{id}/feedback`

**Headers:** `Authorization: Bearer {{teacher_token}}`, `Content-Type: application/json`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Feedback registrado correctamente | 201 Created |
| 02 | Feedback duplicado (ya existe para esta solución) | 409 Conflict |
| 03 | Usuario no es autor del ejercicio | 403 Forbidden |
| 04 | Score fuera de rango (< 0.0 o > 10.0) | 400 Bad Request |
| 05 | Solución no existe | 404 Not Found |
