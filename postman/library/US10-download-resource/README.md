# US10 — Descargar un recurso

**Epic**: EP-02 Library
**Bounded Context**: `library`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
GET /api/v1/resources/{id}/download
Authorization: Bearer {{access_token}}
```

Requiere autenticación. Cualquier usuario autenticado puede descargar recursos con visibilidad `PUBLIC` o `PREMIUM`. Solo el autor puede descargar sus propios recursos `PRIVATE`.

---

## Respuesta exitosa — 200 OK

```json
{
  "resourceId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Examen UNMSM 2024-I",
  "fileUrl": "uploads/resources/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileName": "examen-unmsm-2024-i.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 204800
}
```

La descarga queda registrada en `download_logs`.

---

## Casos de prueba

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01.json` | Recurso PUBLIC, usuario autenticado | 200 OK |
| 02 | `caso-02.json` | Recurso no existe | 404 Not Found |
| 03 | `caso-03.json` | Sin autenticación | 401 Unauthorized |
| 04 | `caso-04.json` | Recurso PRIVATE y usuario no es el autor | 403 Forbidden |

---

## Prerrequisitos

1. Ejecutar US02 login → obtener `{{access_token}}`
2. Ejecutar US07 + US08 → obtener `{{resource_id}}` de un recurso PUBLIC
