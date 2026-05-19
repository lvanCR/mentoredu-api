# HU31 — View personal level and progress

Endpoint: `GET /api/v1/gamification/users/{userId}/level`

Autenticación: Bearer JWT requerido.

---

## Escenarios

| # | Caso | Esperado |
|---|---|---|
| 01 | Usuario con progreso activo | 200 — nivel, XP y porcentaje |
| 02 | Sin autenticación | 401 Unauthorized |
| 03 | Usuario nuevo sin experiencia | 200 — nivel 1, XP 0, porcentaje 0 |
| 04 | Registro de progreso inexistente | 200 — sistema inicializa y devuelve estado base |

---

## Respuesta exitosa (ejemplo)

```json
{
  "userId": "{{user_id}}",
  "currentLevel": 2,
  "experience": 105,
  "progressPercentage": 5.00,
  "updatedAt": "2026-05-18T12:00:00"
}
```
