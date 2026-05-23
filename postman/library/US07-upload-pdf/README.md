# HU12 — Subir archivo PDF académico

**Épica:** EP-04 Library  
**Endpoint:** `POST /api/v1/resources/files`  
**Auth:** Bearer JWT (requerido)  
**Content-Type:** `multipart/form-data`

---

## Descripción

Sube un archivo PDF al servidor y genera su referencia interna.  
El `id` devuelto debe usarse como `fileId` en HU13 al registrar los metadatos del recurso.

---

## Request

| Campo (form-data) | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `file` | File | ✅ | Archivo PDF. Solo `application/pdf`. Máx 20 MB. |

## Response 201 Created

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fileUrl": "uploads/resources/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileName": "examen-uni-2024.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 204800,
  "createdAt": "2026-05-16T21:00:00"
}
```

## Response 400 Bad Request

```json
{
  "timestamp": "2026-05-16T21:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Only PDF files are accepted. Received content type: image/png"
}
```

---

## Casos de prueba

| Archivo | Escenario | Status esperado |
|---|---|---|
| [caso-01-exitoso.json](./caso-01-exitoso.json) | PDF válido, tamaño permitido | 201 Created |
| [caso-02-tipo-invalido.json](./caso-02-tipo-invalido.json) | Archivo no es PDF (Word/imagen) | 400 Bad Request |
| [caso-03-excede-tamano.json](./caso-03-excede-tamano.json) | PDF mayor a 20 MB | 400 Bad Request |
| [caso-04-archivo-corrupto.json](./caso-04-archivo-corrupto.json) | Archivo con header inválido (corrupto) | 400 Bad Request |

---

## Nombre del request en colección

`MentorEduLibraryHU12-SubirPdfPOST`
