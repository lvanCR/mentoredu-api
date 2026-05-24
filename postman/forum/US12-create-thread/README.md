# US12 — Crear hilo en el foro

**Epic**: EP-03 Forum
**Bounded Context**: `forum`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/v1/threads` | Crear un nuevo hilo de discusión |
| `GET`  | `/api/v1/threads` | Listar hilos recientes (paginado) |
| `GET`  | `/api/v1/threads/{id}` | Obtener hilo por ID |
| `PATCH`| `/api/v1/threads/{id}/close` | Cerrar hilo (solo autor o MODERATOR/ADMIN) |

**Auth requerida:** `Authorization: Bearer {{access_token}}`

---

## Body — POST (clasificación multi-modal, RN-12)

| Campo         | Tipo    | Requerido | Notas |
|---|---|---|---|
| `title`       | String  | ✅ Sí     | No vacío, máx. 160 chars |
| `body`        | String  | ✅ Sí     | No vacío |
| `anonymous`   | Boolean | No        | Default `false`. Si `true`, el autor aparece como "Anónimo" (se guarda internamente, RN-13) |
| `universityId`| UUID    | Condicional | Requerido si se envía `areaId`. |
| `areaId`      | UUID    | No        | Solo válido si `universityId` está presente. |
| `courseId`    | UUID    | Condicional | No puede coexistir con `careerId`. |
| `careerId`    | UUID    | Condicional | No puede coexistir con `courseId`. |

> **Regla RN-12**: Al menos uno de `universityId`, `courseId` o `careerId` debe estar presente. No se puede enviar `areaId` sin `universityId`. No se puede enviar `careerId` + `courseId` simultáneamente.

---

## Respuesta exitosa — 201 Created

```json
{
  "id": "uuid",
  "title": "¿Cómo resolver integrales por partes?",
  "body": "Tengo problemas con este tipo de integrales...",
  "anonymous": false,
  "authorDisplay": "Juan Pérez",
  "status": "OPEN",
  "universityId": "b1000000-0000-0000-0000-000000000001",
  "areaId": null,
  "courseId": "b2000000-0000-0000-0000-000000000005",
  "careerId": null,
  "createdAt": "2026-05-22T12:00:00"
}
```

---

## Casos de prueba

| # | Archivo | Escenario | HTTP esperado |
|---|---|---|---|
| 01 | `caso-01.json` | Hilo válido con courseId (no anónimo) | 201 Created |
| 02 | `caso-02.json` | Title o body vacío | 400 Bad Request |
| 03 | `caso-03.json` | Hilo anónimo válido con universityId | 201 Created |
| 04 | `caso-04.json` | Sin autenticación | 401 Unauthorized |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{access_token}}` | JWT obtenido en US02 login |
| `{{university_id}}` | UUID de universidad del seed V9 (ej. `b1000000-0000-0000-0000-000000000001`) |
| `{{course_id}}` | UUID de curso del seed V9 (ej. `b2000000-0000-0000-0000-000000000005`) |
| `{{career_id}}` | UUID de carrera del seed V9 |
