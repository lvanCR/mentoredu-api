# Feedback — Postman

Colección de casos de prueba para el Bounded Context **Feedback** (EP-11).

---

## Implementadas

| HU | Descripción | Endpoint | Casos |
|---|---|---|---|
| HU34 | Registrar retroalimentación académica a un estudiante | `POST /api/v1/feedback` | 4 |
| HU35 | Consultar retroalimentación recibida | `GET /api/v1/feedback/me` | 4 |
| HU40 | Dar feedback a una resolución de estudiante | `POST /api/v1/feedback` (con `solutionId`) | 6 |
| F1.2 | Consultar feedback emitido (docente) | `GET /api/v1/feedback/given` | incluido en HU40 caso-06 |

---

## Reglas de negocio aplicables

- **RN-36**: Solo `TEACHER`, `ACADEMY` o `ADMIN` pueden emitir retroalimentación.
- **RN-37**: Las entradas de retroalimentación son inmutables (no PUT/PATCH/DELETE).
- **RN-38**: El estudiante receptor puede consultar pero no modificar.
- **RN-48**: Solo el autor del recurso puede dar feedback a sus soluciones.
- **RN-49**: `solution.status` cambia automáticamente de `SUBMITTED` a `REVIEWED` al registrar feedback con `solutionId`.
- **RN-50**: Feedback de solución es inmutable (igual que RN-37).

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{teacher_token}}` | JWT de un usuario con rol TEACHER (autor del recurso) | HU34, HU40 |
| `{{other_teacher_token}}` | JWT de un docente que NO es autor del recurso | HU40 caso-03 |
| `{{student_token}}` | JWT del estudiante receptor | HU35 |
| `{{student_id}}` | UUID del estudiante receptor | HU34, HU40 |
| `{{solution_id}}` | UUID de una resolución existente | HU40 |
| `{{target_user_id}}` | UUID del estudiante receptor | HU34 |
| `{{program_id}}` | UUID de un programa (opcional) | HU34 |
| `{{cycle_id}}` | UUID de un ciclo (opcional) | HU34 |
