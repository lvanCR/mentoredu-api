# Billing — Postman

Colección de casos de prueba para el Bounded Context **Billing** (EP-08).

---

## Implementadas

_Ninguna aún. EP-08 está pendiente de implementación._

---

## Pendientes

| HU | Descripción | Endpoint previsto |
|---|---|---|
| HU23 | Activar suscripción premium | `POST /api/v1/billing/subscriptions` |
| HU24 | Comprar paquete de monedas | `POST /api/v1/billing/coin-purchases` |

---

## Reglas de negocio aplicables

- RN-26: Un usuario solo puede tener una suscripción premium activa a la vez.
- RN-27: El acceso premium depende de suscripción vigente o compra validada.
- RN-28: Las compras fallidas no modifican el saldo ni el estado del plan.

---

## Variables de entorno previstas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT del usuario comprador | HU23, HU24 |
| `{{plan_id}}` | UUID del plan premium elegido | HU23 |
| `{{coin_package_id}}` | UUID del paquete de monedas elegido | HU24 |
