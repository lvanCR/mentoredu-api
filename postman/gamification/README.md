# Gamification — Postman

Colección de casos de prueba para el Bounded Context **Gamification** (EP-10).

---

## Implementadas

| HU | Descripción | Endpoint |
|---|---|---|
| HU30 | Acumular puntos de experiencia | Disparado internamente por eventos (sin endpoint directo) |
| HU31 | Consultar nivel y progreso personal | `GET /api/v1/gamification/users/{userId}/level` |

---

## Pendientes

| HU | Descripción | Endpoint previsto |
|---|---|---|
| HU32 | Ganar y ver insignias | `GET /api/v1/gamification/users/{userId}/badges` |

---

## Reglas de negocio aplicables

- RN-31: Los puntos se generan únicamente por acciones definidas por el sistema.
- RN-32: El nivel se recalcula automáticamente al acumular experiencia suficiente.
- RN-33: Una insignia no puede asignarse más de una vez al mismo usuario.
- RN-34: El historial de puntos es inmutable.
- RN-35: Puntos de experiencia y monedas virtuales son sistemas independientes.

---

## Variables de entorno previstas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT del usuario autenticado | HU31, HU32 |
