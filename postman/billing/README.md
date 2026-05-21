<<<<<<< HEAD
# Bounded Context: Billing (EP-08)

Gestiona suscripciones premium, paquetes de monedas y compras de los usuarios.

---

## Implementados

| ID | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU23 | Activar suscripción premium | `POST /api/v1/billing/subscriptions` | `MentorEduBillingHU23-ActivateSubscriptionPOST` | 2026-05-18 |
| HU24 | Comprar paquete de monedas | `POST /api/v1/billing/coin-purchases` | `MentorEduBillingHU24-BuyCoinPackagePOST` | 2026-05-18 |
| F1.3 | Listar planes (público) | `GET /api/v1/billing/plans` | `MentorEduBillingF13-GetPlansGET` | 2026-05-21 |
| F1.3 | Listar paquetes de monedas (público) | `GET /api/v1/billing/coin-packages` | `MentorEduBillingF13-GetCoinPackagesGET` | 2026-05-21 |

## Historias pendientes

Todas las HUs del bounded context **billing** han sido implementadas.

---

## Estructura de carpetas

```
billing/
├── README.md           ← este archivo
└── F13-catalog/
    ├── README.md
    ├── caso-01-planes-sin-auth.json
    └── caso-02-paquetes-sin-auth.json
```

---

## Notas del bounded context

- `GET /billing/plans` y `GET /billing/coin-packages` son **públicos** (no requieren auth).
- `POST /billing/subscriptions` y `POST /billing/coin-purchases` requieren `Authorization: Bearer <token>`.
- Las monedas (`coin_wallet`) son distintas de los puntos de experiencia (`point_transactions`).
- Los datos de planes y paquetes provienen del seed de la migración V5.
=======
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
>>>>>>> 24b4d986245a45255516a1701b1bff348ed88e8e
