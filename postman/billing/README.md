# Billing — Postman

Colección de casos de prueba para el Bounded Context **Billing** (EP-08).

---

## Implementadas

| HU | Descripción | Endpoint |
|---|---|---|
| HU23 | Activar suscripción premium | `POST /api/v1/billing/subscriptions` |
| HU24 | Comprar paquete de monedas | `POST /api/v1/billing/coin-purchases` |

---

## Pendientes

_EP-08 completo._

---

## Reglas de negocio aplicadas

- RN-26: Un usuario solo puede tener una suscripción premium activa a la vez.
- RN-27: El acceso premium depende de suscripción vigente o compra validada.
- RN-28: Las compras fallidas no modifican el saldo ni el estado del plan.

---

## Endpoints disponibles

| Método | Ruta | US | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/billing/subscriptions` | US23 | Activar suscripción premium |
| `GET`  | `/api/v1/billing/subscriptions/me` | US23 | Listar mis suscripciones |
| `GET`  | `/api/v1/billing/subscriptions/me/active` | US23 | Suscripción activa |
| `GET`  | `/api/v1/billing/coin-packages` | US24 | Listar paquetes de monedas activos |
| `POST` | `/api/v1/billing/coin-purchases` | US24 | Comprar paquete de monedas |

---

## Variables de entorno

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{access_token}}` | JWT del usuario comprador | HU23, HU24 |
| `{{plan_id}}` | UUID del plan premium elegido (seed: V5) | HU23 |
| `{{coin_package_id}}` | UUID del paquete de monedas elegido (seed: V5) | HU24 |
