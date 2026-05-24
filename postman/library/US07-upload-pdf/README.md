# US07 — Subir archivo PDF académico

**Epic**: EP-02 Library
**Bounded Context**: `library`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
POST /api/v1/resources/files
Authorization: Bearer {{teacher_token}}
Content-Type: multipart/form-data
```

Solo TEACHER, ACADEMY o ADMIN pueden usar este endpoint (RN-05).

---

## Request

| Campo (form-data) | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `file` | File | ✅ | Archivo PDF. Solo `application/pdf`. Máx 20 MB. |

---

## Respuesta exitosa — 201 Created

```json
{
  "fileUrl": "uploads/resources/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileName": "examen-uni-2024.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 204800
}
```

> **No existe campo `id` ni `fileId` en la respuesta.** Los 4 campos (`fileUrl`, `fileName`, `mimeType`, `sizeBytes`) deben copiarse directamente en el body de `POST /api/v1/resources` (US08).

---

## Respuesta 400 Bad Request

```json
{
  "timestamp": "2026-05-22T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Only PDF files are accepted. Received content type: image/png"
}
```

---

## Casos de prueba

| # | Archivo | Escenario | Status esperado |
|---|---|---|---|
| 01 | `caso-01-exitoso.json` | PDF válido, tamaño permitido | 201 Created |
| 02 | `caso-02-tipo-invalido.json` | Archivo no es PDF (Word/imagen) | 400 Bad Request |
| 03 | `caso-03-excede-tamano.json` | PDF mayor a 20 MB | 400 Bad Request |
| 04 | `caso-04-archivo-corrupto.json` | Archivo con header inválido | 400 Bad Request |

---

## Flujo obligatorio

1. Ejecutar este endpoint → copiar los 4 campos de la respuesta.
2. Pegar esos campos en `POST /api/v1/resources` (US08) para registrar los metadatos.
