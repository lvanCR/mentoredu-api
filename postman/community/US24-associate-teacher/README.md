# US24 — Asociar docente a academia

Flujo de asociación: el docente solicita, la academia aprueba o rechaza.

**Endpoints:**
- `POST /api/v1/associations` — docente solicita asociación
- `GET /api/v1/associations/me` — docente lista sus asociaciones
- `GET /api/v1/associations/requests` — academia lista solicitudes recibidas
- `PATCH /api/v1/associations/{id}/accept` — academia acepta
- `PATCH /api/v1/associations/{id}/reject` — academia rechaza

**Headers:** `Authorization: Bearer {{teacher_token}}` o `Bearer {{academy_token}}`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Docente solicita asociación con academia | 201 Created `status: PENDING` |
| 02 | Solicitud duplicada (ya existe con esa academia) | 409 Conflict |
| 03 | Docente lista sus asociaciones | 200 OK |
| 04 | Academia lista solicitudes recibidas | 200 OK |
| 05 | Academia acepta la solicitud | 200 OK `status: ACCEPTED` |
| 06 | Academia rechaza la solicitud | 200 OK `status: REJECTED` |
| 07 | Solicitud ya resuelta (aceptar/rechazar nuevamente) | 409 Conflict |
| 08 | Usuario no docente intenta solicitar | 403 Forbidden |
