# US18 — Enviar resolución a un ejercicio

**Epic**: EP-04 Pedagogy
**Bounded Context**: `pedagogy`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
POST /api/v1/resources/{resourceId}/solutions
Authorization: Bearer {{student_token}}
Content-Type: application/json
```

---

## Body

| Campo     | Tipo   | Requerido | Descripción |
|---|---|---|---|
| `fileUrl` | String | No        | URL del PDF de la resolución (máx. 500 chars). Obtenido de `POST /resources/files` (US07). |
| `content` | String | No        | Texto de la resolución (máx. 10000 chars). Alternativa o complemento a `fileUrl`. |

> Al menos uno de los dos (`fileUrl` o `content`) debe estar presente para que la resolución tenga sentido, aunque técnicamente ambos son opcionales en la validación.

---

## Reglas de negocio

| RN | Descripción |
|----|-------------|
| RN-09 | Solo puede existir una resolución por par `(resource_id, student_id)`. Segunda → 409. |
| RN-10 | El recurso debe tener `acepta_resoluciones = true`. Recurso sin esta flag → 403. |
| RN-01 | Solo STUDENT puede enviar resoluciones. TEACHER/ACADEMY/ADMIN → 403. |

---

## Respuesta exitosa — 201 Created

```json
{
  "id": "uuid",
  "resourceId": "uuid",
  "studentId": "uuid",
  "fileUrl": "uploads/resources/mi-resolucion.pdf",
  "content": null,
  "status": "SUBMITTED",
  "submittedAt": "2026-05-22T15:00:00"
}
```

---

## Escenarios de aceptación

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01.json` | Resolución enviada correctamente (con fileUrl) | 201 Created |
| 02 | `caso-02.json` | Recurso con `acepta_resoluciones=false` | 403 Forbidden |
| 03 | `caso-03.json` | Solución duplicada para el mismo recurso | 409 Conflict |
| 04 | `caso-04.json` | Recurso no existe | 404 Not Found |
| 05 | `caso-05.json` | No autenticado | 401 Unauthorized |

---

## Prerrequisitos

1. Usuario TEACHER/ACADEMY publica un recurso con `acepta_resoluciones: true` → obtener `{{resource_id}}`
2. Usuario STUDENT sube un PDF con US07 → obtener `fileUrl`
3. Usuario STUDENT hace login → obtener `{{student_token}}`
4. Ejecutar este endpoint
