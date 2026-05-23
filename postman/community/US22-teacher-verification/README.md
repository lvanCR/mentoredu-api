# HU21 — Request teacher verification

**Endpoint principal:** `POST /api/v1/verification/requests`  
**Endpoint consulta:** `GET /api/v1/verification/requests/me`  
**Auth requerida:** JWT con rol `TEACHER`

---

## Casos de prueba

### caso-01.json — Exitoso: credencial válida → 201 Created

```json
POST /api/v1/verification/requests
Authorization: Bearer {{teacher_token}}

{
  "entityType": "TEACHER",
  "documentType": "DNI",
  "fileUrl": "https://docs.example.com/docente-dni.pdf"
}
```

**Respuesta esperada:** `201 Created`
```json
{
  "id": "{{uuid}}",
  "userId": "{{uuid}}",
  "entityType": "TEACHER",
  "status": "PENDING",
  "documentType": "DNI",
  "fileUrl": "https://docs.example.com/docente-dni.pdf",
  "submittedAt": "{{timestamp}}",
  "reviewedAt": null
}
```

---

### caso-02.json — Error: campos vacíos → 400 Bad Request

```json
POST /api/v1/verification/requests
Authorization: Bearer {{teacher_token}}

{}
```

**Respuesta esperada:** `400 Bad Request`
```json
{
  "error": "Validation failed",
  "details": {
    "entityType": "entityType is required",
    "documentType": "documentType is required",
    "fileUrl": "fileUrl is required"
  }
}
```

---

### caso-03.json — Alternativo exitoso: documento profesional → 201 Created, estado PENDING

```json
POST /api/v1/verification/requests
Authorization: Bearer {{teacher_token}}

{
  "entityType": "TEACHER",
  "documentType": "TITULO_UNIVERSITARIO",
  "fileUrl": "https://docs.example.com/titulo-universitario.pdf"
}
```

**Respuesta esperada:** `201 Created` con `"status": "PENDING"`

---

### caso-04.json — Alternativo error: solicitud duplicada → 409 Conflict (RN-24)

```json
POST /api/v1/verification/requests
Authorization: Bearer {{teacher_token}}

{
  "entityType": "TEACHER",
  "documentType": "DNI",
  "fileUrl": "https://docs.example.com/docente-dni.pdf"
}
```
*(ejecutar después de caso-01 sin resolver la solicitud anterior)*

**Respuesta esperada:** `409 Conflict`
```json
{
  "error": "Conflict",
  "message": "Ya tienes una solicitud de verificación pendiente para TEACHER"
}
```

---

## Consulta de estado (GET)

```
GET /api/v1/verification/requests/me
Authorization: Bearer {{teacher_token}}
```

**Respuesta esperada:** `200 OK` con lista de solicitudes del docente autenticado.
