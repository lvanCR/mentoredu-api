# US22 — Solicitar verificación de academia

**Endpoint principal:** `POST /api/v1/verification/requests`  
**Endpoint consulta:** `GET /api/v1/verification/requests/me`  
**Auth requerida:** JWT con rol `ACADEMY`

---

## Casos de prueba

### caso-01.json — Exitoso: documentación válida → 201 Created

```json
POST /api/v1/verification/requests
Authorization: Bearer {{academy_token}}

{
  "entityType": "ACADEMY",
  "documentType": "RUC",
  "fileUrl": "https://docs.example.com/academia-ruc.pdf"
}
```

**Respuesta esperada:** `201 Created`
```json
{
  "id": "{{uuid}}",
  "userId": "{{uuid}}",
  "entityType": "ACADEMY",
  "status": "PENDING",
  "documentType": "RUC",
  "fileUrl": "https://docs.example.com/academia-ruc.pdf",
  "submittedAt": "{{timestamp}}",
  "reviewedAt": null
}
```

---

### caso-02.json — Error: documentación incompleta → 400 Bad Request

```json
POST /api/v1/verification/requests
Authorization: Bearer {{academy_token}}

{
  "entityType": "ACADEMY"
}
```

**Respuesta esperada:** `400 Bad Request`
```json
{
  "error": "Validation failed",
  "details": {
    "documentType": "documentType is required",
    "fileUrl": "fileUrl is required"
  }
}
```

---

### caso-03.json — Alternativo exitoso: documentos completos → 201 Created

```json
POST /api/v1/verification/requests
Authorization: Bearer {{academy_token}}

{
  "entityType": "ACADEMY",
  "documentType": "LICENCIA_FUNCIONAMIENTO",
  "fileUrl": "https://docs.example.com/licencia-funcionamiento.pdf"
}
```

**Respuesta esperada:** `201 Created` con `"status": "PENDING"`

---

### caso-04.json — Alternativo error: solicitud activa ya existente → 409 Conflict (RN-24)

```json
POST /api/v1/verification/requests
Authorization: Bearer {{academy_token}}

{
  "entityType": "ACADEMY",
  "documentType": "RUC",
  "fileUrl": "https://docs.example.com/academia-ruc.pdf"
}
```
*(ejecutar después de caso-01 sin resolver la solicitud anterior)*

**Respuesta esperada:** `409 Conflict`
```json
{
  "error": "Conflict",
  "message": "Ya tienes una solicitud de verificación pendiente para ACADEMY"
}
```

---

## Nota sobre flujos distintos (RN-25)

La diferencia entre US21 y US22 reside únicamente en el `entityType`:
- `TEACHER` → requiere rol `TEACHER` en el JWT
- `ACADEMY` → requiere rol `ACADEMY` en el JWT

Si el `entityType` no coincide con el rol del usuario autenticado, el sistema responde `403 Forbidden`.
