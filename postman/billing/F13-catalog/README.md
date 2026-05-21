# F1.3 — Catálogo público de planes y paquetes de monedas

**Epic**: EP-08 Billing (fix de infraestructura)  
**Bounded Context**: `billing`  
**Estado**: Implementado — 2026-05-21  
**Rama**: `feat/HU38-submit-appeal`  
**Nombres en Postman**:  
- `MentorEduBillingF13-GetPlansPOST`  
- `MentorEduBillingF13-GetCoinPackagesGET`

---

## Endpoints

```
GET /api/v1/billing/plans
GET /api/v1/billing/coin-packages
```

**No requieren autenticación** (endpoints públicos, expuestos como `permitAll` en `SecurityConfig`).

---

## GET /api/v1/billing/plans

### Headers

| Header | Valor |
|---|---|
| *(ninguno requerido)* | — |

Sin body (es un GET).

### Respuesta exitosa

**Status**: `200 OK`

```json
[
  {
    "id": "uuid-plan-basic",
    "name": "Plan Básico",
    "price": 9.99,
    "durationDays": 30,
    "description": "Acceso básico a recursos premium"
  },
  {
    "id": "uuid-plan-premium",
    "name": "Plan Premium",
    "price": 19.99,
    "durationDays": 30,
    "description": "Acceso completo a todos los recursos"
  }
]
```

Los datos provienen del seed de la migración V5.

---

## GET /api/v1/billing/coin-packages

### Respuesta exitosa

**Status**: `200 OK`

```json
[
  {
    "id": "uuid-package-1",
    "name": "Paquete Básico",
    "coins": 100,
    "price": 4.99,
    "description": "100 monedas para descargar recursos premium"
  },
  {
    "id": "uuid-package-2",
    "name": "Paquete Estándar",
    "coins": 300,
    "price": 12.99,
    "description": "300 monedas con descuento"
  }
]
```

| Campo | Descripción |
|---|---|
| `id` | UUID del paquete |
| `name` | Nombre del paquete |
| `coins` | Cantidad de monedas que se añaden al wallet |
| `price` | Precio en USD |
| `description` | Descripción del paquete |

---

## Escenarios de aceptación

### Caso 1 — Listar planes (sin auth)
**Archivo**: `caso-01-planes-sin-auth.json`  
**Status esperado**: `200 OK` — lista de planes del seed V5

### Caso 2 — Listar paquetes de monedas (sin auth)
**Archivo**: `caso-02-paquetes-sin-auth.json`  
**Status esperado**: `200 OK` — lista de paquetes del seed V5

---

## Cómo probar en Postman

1. No es necesario estar autenticado.
2. Crea un request `GET` con URL `{{base_url}}/{{api_v1}}/billing/plans`.
3. Ejecuta directamente — sin headers de auth.
4. Verifica que la respuesta sea `200 OK` con la lista de planes.
5. Repite con `{{base_url}}/{{api_v1}}/billing/coin-packages`.

---

## Notas

- Estos endpoints son de solo lectura. Para activar una suscripción usar `POST /billing/subscriptions` (HU23, requiere auth).
- Para comprar monedas usar `POST /billing/coin-purchases` (HU24, requiere auth).
