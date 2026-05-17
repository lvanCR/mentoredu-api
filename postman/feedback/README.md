# Feedback — Postman

Colección de casos de prueba para el Bounded Context **Feedback** (EP-11).

---

## Implementadas

_Ninguna aún. EP-11 está pendiente de implementación._

---

## Pendientes

| HU | Descripción | Endpoint previsto |
|---|---|---|
| HU34 | Registrar retroalimentación académica a un estudiante | `POST /api/v1/feedback` |
| HU35 | Consultar retroalimentación recibida | `GET /api/v1/feedback/me` |

---

## Reglas de negocio aplicables

- RN-36: Solo un usuario con rol `TEACHER` o `ADMIN` puede emitir retroalimentación.
- RN-37: Las entradas de retroalimentación son inmutables (no PUT/PATCH/DELETE).
- RN-38: El estudiante receptor puede consultar pero no modificar.

---

## Variables de entorno previstas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{teacher_token}}` | JWT de un usuario con rol TEACHER | HU34 |
| `{{student_token}}` | JWT del estudiante receptor | HU35 |
| `{{target_user_id}}` | UUID del estudiante que recibirá la retroalimentación | HU34 |
| `{{program_id}}` | UUID de un programa (opcional en HU34) | HU34 |
| `{{cycle_id}}` | UUID de un ciclo (opcional en HU34) | HU34 |
