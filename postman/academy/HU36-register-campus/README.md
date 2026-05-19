# HU36 — Register campus for academy

**Epic**: EP-03 · **BC**: academy · **Fecha**: 2026-05-19

---

## Endpoint

```
POST /api/v1/academies/{academyId}/campuses
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

---

## Request body

| Campo     | Tipo   | Requerido | Validaciones                        |
|-----------|--------|-----------|-------------------------------------|
| `name`    | String | ✅        | NotBlank · max 120 chars            |
| `address` | String | ✅        | NotBlank · max 180 chars            |
| `city`    | String | ✅        | NotBlank · max 80 chars             |

---

## Response 201 Created

```json
{
  "id": "uuid",
  "academyId": "uuid",
  "name": "Sede Miraflores",
  "address": "Av. Larco 1234",
  "city": "Lima",
  "createdAt": "2026-05-19T10:00:00",
  "updatedAt": "2026-05-19T10:00:00"
}
```

---

## Reglas de negocio aplicables

| RN | Descripción |
|----|-------------|
| RN-39 | El nombre de sede debe ser único por academia. |
| RN-40 | Solo la organización propietaria puede registrar sedes en la academia. |

---

## Escenarios Gherkin → casos de prueba

| Caso | Escenario | Status esperado |
|------|-----------|-----------------|
| caso-01 | Exitoso — sede válida registrada | 201 Created |
| caso-02 | Error — campo obligatorio faltante (address) | 400 Bad Request |
| caso-03 | Alternativo exitoso — segunda sede con nombre distinto | 201 Created |
| caso-04 | Alternativo error — nombre de sede duplicado en la misma academia | 409 Conflict |

---

## Variables de entorno requeridas

| Variable | Descripción |
|----------|-------------|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT de una cuenta tipo ORGANIZATION con perfil y academia creados |
| `{{academy_id}}` | UUID de una academia existente propiedad del usuario autenticado |
