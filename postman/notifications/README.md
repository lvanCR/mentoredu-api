# Notifications — Postman

Colección de casos de prueba para el Bounded Context **Notifications** (EP-09).

---

## Implementadas

_Ninguna aún. EP-09 está pendiente de implementación._

---

## Pendientes

| HU | Descripción | Endpoint previsto |
|---|---|---|
| HU25 | Consultar notificaciones pendientes | `GET /api/v1/notifications` |

---

## Reglas de negocio aplicables

- RN-29: Las notificaciones se generan únicamente por eventos definidos por el negocio.
- RN-30: Toda notificación tiene estado leído o no leído.

---

## Variables de entorno previstas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT del usuario autenticado | HU25 |
