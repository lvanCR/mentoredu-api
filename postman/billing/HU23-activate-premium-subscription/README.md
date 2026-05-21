# HU23 — Activate premium subscription

**Endpoint**: `POST /api/v1/billing/subscriptions`
**Autenticación**: Bearer JWT (`{{access_token}}`)

---

## Casos de prueba

### Caso 01 — Exitoso: pago válido → suscripción activada (201 Created)

**Request name**: `MentorEduBillingHU23-ActivarSuscripcionPOST`

```json
POST {{api_v1}}/billing/subscriptions
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "planId": "{{plan_id}}",
  "paymentMethod": "CARD"
}
```

**Respuesta esperada (201)**:
```json
{
  "id": "<uuid>",
  "userId": "<uuid>",
  "planId": "{{plan_id}}",
  "planName": "MENSUAL",
  "planPrice": 19.90,
  "status": "ACTIVE",
  "startsAt": "<timestamp>",
  "endsAt": "<timestamp>",
  "createdAt": "<timestamp>"
}
```

---

### Caso 02 — Error: pago falla → plan no habilitado (400 Bad Request)

**Escenario**: El servicio lanza `PaymentFailedException` (simulado en tests).

```json
POST {{api_v1}}/billing/subscriptions
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "planId": "{{plan_id}}",
  "paymentMethod": "SIMULATED_FAIL"
}
```

**Respuesta esperada (400)**:
```json
{
  "error": "Bad Request",
  "message": "El pago fue rechazado"
}
```

---

### Caso 03 — Alternativo exitoso: plan premium válido → acceso premium activo (201 Created)

Igual al Caso 01 con `planId` de plan TRIMESTRAL o ANUAL.

---

### Caso 04 — Alternativo error: suscripción activa existente → 409 Conflict (RN-26)

```json
POST {{api_v1}}/billing/subscriptions
Authorization: Bearer {{access_token}}

{
  "planId": "{{plan_id}}",
  "paymentMethod": "CARD"
}
```

**Respuesta esperada (409)**:
```json
{
  "error": "Conflict",
  "message": "Ya tienes una suscripción activa hasta: <fecha>"
}
```

---

### Caso 05 — Plan no encontrado → 404 Not Found

```json
{
  "planId": "00000000-0000-0000-0000-000000000000",
  "paymentMethod": "CARD"
}
```

**Respuesta esperada (404)**:
```json
{
  "error": "Not Found",
  "message": "Plan no encontrado: 00000000-0000-0000-0000-000000000000"
}
```

---

### Caso 06 — Sin autenticación → 401 Unauthorized

Petición sin cabecera `Authorization`.

---

### Endpoints adicionales

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/billing/subscriptions/me` | Lista todas las suscripciones del usuario |
| `GET` | `/api/v1/billing/subscriptions/me/active` | Devuelve la suscripción activa o 404 |

---

## Variables de entorno necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario autenticado |
| `{{plan_id}}` | UUID de un plan (ver `GET /api/v1/swagger-ui.html` para listar planes con Swagger) |
