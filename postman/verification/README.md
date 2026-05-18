# Verification — Postman

Colección de casos de prueba para el Bounded Context **Verification** (EP-07).

---

## Implementadas

| HU | Descripción | Casos | Endpoint |
|---|---|---|---|
| [HU21](./HU21-teacher-verification/) | Solicitar verificación de docente | 4 | `POST /api/v1/verification/requests` |
| [HU22](./HU22-organization-verification/) | Solicitar verificación de organización | 4 | `POST /api/v1/verification/requests` |

---

## Pendientes

*(EP-07 completo)*

---

## Reglas de negocio aplicables

- RN-23: Toda solicitud requiere documento válido adjunto (`documentType` + `fileUrl`).
- RN-24: Solo una solicitud activa (PENDING) por entidad.
- RN-25: Los flujos de verificación de docente (TEACHER) y organización (ACADEMY) son distintos. El rol del usuario debe coincidir con el `entityType` enviado.

---

## Variables de entorno requeridas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{teacher_token}}` | JWT de un usuario con rol TEACHER | HU21 |
| `{{academy_token}}` | JWT de un usuario con rol ACADEMY | HU22 |
