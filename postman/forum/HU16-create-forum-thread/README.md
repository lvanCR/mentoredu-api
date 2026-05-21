# HU16 — Create forum thread

Endpoint para crear un nuevo hilo de discusión en el foro.

---

## Endpoint

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/v1/threads` | Crear un nuevo hilo de foro |

---

## Headers requeridos

```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

## Body fields

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `subjectId` | `UUID` | Sí | Debe corresponder a una materia existente (RN-16) |
| `title` | `string` | Sí | No puede estar vacío (`@NotBlank`) |
| `body` | `string` | Sí | No puede estar vacío (`@NotBlank`) |
| `isAnonymous` | `boolean` | Sí | `true` oculta el nombre público; `author_user_id` se guarda internamente |

## Reglas de negocio

- El hilo debe estar asociado a un `subjectId` válido (RN-16).
- Si `isAnonymous = true`, el nombre del autor no se expone en la respuesta, pero se conserva internamente para moderación (RN-04).
- El hilo se crea con estado `OPEN`.
- Requiere autenticación JWT.

---

## Casos de prueba

| Archivo | Escenario | Status esperado |
|---|---|---|
| `caso-01.json` | Hilo válido no anónimo | 201 Created |
| `caso-02.json` | Body vacío / título vacío | 400 Bad Request |
| `caso-03.json` | Hilo anónimo válido | 201 Created |
| `caso-04.json` | Sin autenticación (token ausente) | 401 Unauthorized |

---

## Variables necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT obtenido en HU02 login |
| `{{subject_id}}` | UUID de una materia existente en BD |

---

## Response body (201 — creado)

```json
{
  "id": "uuid",
  "subjectId": "uuid",
  "title": "¿Cómo resolver integrales por partes?",
  "body": "Tengo problemas con este tipo de integrales...",
  "isAnonymous": false,
  "authorDisplay": "Juan Pérez",
  "status": "OPEN",
  "createdAt": "2026-05-17T12:00:00",
  "updatedAt": "2026-05-17T12:00:00"
}
```
