# US23 — Aprobar o rechazar verificación

Solo MODERATOR o ADMIN puede listar y revisar solicitudes de verificación pendientes.

**Endpoints:**
- `GET /api/v1/verification/requests` — listar todas las solicitudes
- `PATCH /api/v1/verification/requests/{id}/review` — aprobar o rechazar

**Headers:** `Authorization: Bearer {{moderator_token}}`, `Content-Type: application/json`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Moderador lista solicitudes pendientes | 200 OK |
| 02 | Moderador aprueba una verificación | 200 OK `status: APPROVED` |
| 03 | Moderador rechaza con razón obligatoria | 200 OK `status: REJECTED` |
| 04 | Rechazo sin campo `notes` | 400 Bad Request (RN-17) |
| 05 | Usuario sin rol MODERATOR/ADMIN | 403 Forbidden |
