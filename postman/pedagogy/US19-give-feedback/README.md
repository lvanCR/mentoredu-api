# US19 — Dar feedback correctivo

**Epic**: EP-04 Pedagogy
**Bounded Context**: `pedagogy`
**Estado**: Implementada — 2026-05-22 `develop`

---

## Endpoint

```
POST /api/v1/solutions/{solutionId}/feedback
Authorization: Bearer {{teacher_token}}
Content-Type: application/json
```

Solo el autor del ejercicio puede dar feedback (RN-12).

---

## Body

| Campo   | Tipo       | Requerido | Validación |
|---|---|---|---|
| `body`  | String     | ✅ Sí     | No vacío (`@NotBlank`). Comentario correctivo del docente. |
| `score` | BigDecimal | No        | Entre 0.0 y 10.0 inclusive (RN-22). |

```json
{
  "body": "Excelente análisis en el punto 3. Revisa la integración del ejercicio 7.",
  "score": 8.5
}
```

---

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-11  | El feedback es inmutable: no se puede editar ni eliminar una vez creado. |
| RN-12  | Solo el autor del ejercicio puede dar feedback. Otro usuario → 403. |
| RN-22  | El score debe estar entre 0.0 y 10.0 (inclusive). Fuera de rango → 400. |

---

## Respuesta exitosa — 201 Created

```json
{
  "id": "uuid",
  "solutionId": "uuid",
  "body": "Excelente análisis en el punto 3. Revisa la integración del ejercicio 7.",
  "score": 8.5,
  "authorName": "Juan Quispe",
  "createdAt": "2026-05-22T14:00:00"
}
```

---

## Casos de prueba

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Feedback registrado correctamente | 201 Created |
| 02 | Feedback duplicado (ya existe para esta solución) | 409 Conflict |
| 03 | Usuario no es autor del ejercicio | 403 Forbidden |
| 04 | Score fuera de rango (< 0.0 o > 10.0) | 400 Bad Request |
| 05 | Solución no existe | 404 Not Found |

---

## Prerrequisitos

1. Usuario TEACHER/ACADEMY publica ejercicio con `acepta_resoluciones: true`
2. Usuario STUDENT envía resolución → obtener `{{solution_id}}`
3. Docente hace login → obtener `{{teacher_token}}`
