# Feedback — Postman

Colección de casos de prueba para el Bounded Context **Feedback** (EP-11).

---

## Implementadas

| HU | Descripción | Endpoint | Casos |
|---|---|---|---|
| HU34 | Registrar retroalimentación académica a un estudiante | `POST /api/v1/feedback` | 4 |
| HU35 | Consultar retroalimentación recibida | `GET /api/v1/feedback/me` | 4 |

---

## Pendientes

_EP-11 completo._

---

## Reglas de negocio aplicables

- **RN-36**: Solo un usuario con rol `TEACHER` o `ADMIN` puede emitir retroalimentación.
- **RN-37**: Las entradas de retroalimentación son inmutables (no PUT/PATCH/DELETE).
- **RN-38**: El estudiante receptor puede consultar pero no modificar.

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{teacher_token}}` | JWT de un usuario con rol TEACHER | HU34 |
| `{{student_token}}` | JWT del estudiante receptor | HU35 |
| `{{target_user_id}}` | UUID del estudiante receptor | HU34 |
| `{{program_id}}` | UUID de un programa (opcional) | HU34 |
| `{{cycle_id}}` | UUID de un ciclo (opcional) | HU34 |
