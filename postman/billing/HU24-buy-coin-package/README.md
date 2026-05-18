# HU24 — Buy coin package

**Endpoint**: `POST /api/v1/billing/coin-purchases`
**Autenticación**: Bearer JWT (`{{access_token}}`)

---

## Casos de prueba

### Caso 01 — Exitoso: pago confirmado → monedas acreditadas (201 Created)

**Request name**: `MentorEduBillingHU24-ComprarMonedasPOST`

```json
POST {{api_v1}}/billing/coin-purchases
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "coinPackageId": "{{coin_package_id}}",
  "quantity": 1,
  "paymentMethod": "CARD"
}
```

**Respuesta esperada (201)**:
```json
{
  "id": "<uuid>",
  "userId": "<uuid>",
  "coinPackageId": "{{coin_package_id}}",
  "coinPackageName": "STARTER",
  "quantity": 1,
  "totalCoins": 100,
  "totalAmount": 5.00,
  "status": "COMPLETED",
  "newWalletBalance": 100,
  "createdAt": "<timestamp>"
}
```

---

### Caso 02 — Error: pago falla → sistema no acredita saldo (400 Bad Request)

```json
POST {{api_v1}}/billing/coin-purchases
Authorization: Bearer {{access_token}}

{
  "coinPackageId": "{{coin_package_id}}",
  "quantity": 1,
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

### Caso 03 — Alternativo exitoso: múltiples paquetes → saldo incrementado (201 Created)

```json
{
  "coinPackageId": "{{coin_package_id_pro}}",
  "quantity": 2,
  "paymentMethod": "YAPE"
}
```

**Respuesta esperada**: `totalCoins: 600`, `newWalletBalance` acumulado.

---

### Caso 04 — Alternativo error: paquete no existe → 404 Not Found

```json
{
  "coinPackageId": "00000000-0000-0000-0000-000000000000",
  "quantity": 1,
  "paymentMethod": "CARD"
}
```

**Respuesta esperada (404)**:
```json
{
  "error": "Not Found",
  "message": "Paquete de monedas no encontrado o inactivo: 00000000-0000-0000-0000-000000000000"
}
```

---

### Caso 05 — Sin autenticación → 401 Unauthorized

Petición sin cabecera `Authorization`.

---

### Endpoint: Listar paquetes disponibles

```
GET {{api_v1}}/billing/coin-packages
Authorization: Bearer {{access_token}}
```

**Respuesta (200)**:
```json
[
  { "id": "<uuid>", "name": "STARTER", "coins": 100, "price": 5.00, "active": true },
  { "id": "<uuid>", "name": "PRO",     "coins": 300, "price": 12.00, "active": true },
  { "id": "<uuid>", "name": "ELITE",   "coins": 700, "price": 25.00, "active": true }
]
```

---

## Variables de entorno necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario autenticado |
| `{{coin_package_id}}` | UUID de un paquete STARTER (obtener de `GET /api/v1/billing/coin-packages`) |
| `{{coin_package_id_pro}}` | UUID de un paquete PRO |
