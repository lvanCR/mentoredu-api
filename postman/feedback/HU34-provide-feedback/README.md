# HU34 — Provide academic feedback to student

**Endpoint**: `POST {{api_v1}}/feedback`  
**Auth**: `Bearer {{teacher_token}}`  
**Content-Type**: `application/json`

---

## Body fields

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `targetUserId` | UUID | Sí | ID del estudiante receptor |
| `body` | String | Sí | Cuerpo de la retroalimentación |
| `score` | Decimal (0.0–20.0) | No | Puntuación opcional |
| `programId` | UUID | No | Programa relacionado |
| `cycleId` | UUID | No | Ciclo relacionado |

---

## Reglas de negocio

- **RN-36**: Solo `TEACHER` o `ADMIN` puede emitir retroalimentación.
- **RN-37**: Las entradas son inmutables una vez registradas.

---

## Escenarios Gherkin → casos

| Caso | Escenario | HTTP esperado |
|---|---|---|
| caso-01.json | Exitoso — docente válido + estudiante existe + body válido | 201 Created |
| caso-02.json | Error — body vacío | 400 Bad Request |
| caso-03.json | Alternativo exitoso — incluye puntuación | 201 Created |
| caso-04.json | Alternativo error — estudiante no existe | 404 Not Found |
