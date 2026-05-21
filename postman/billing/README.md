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
