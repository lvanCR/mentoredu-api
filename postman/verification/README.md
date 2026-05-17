# Verification — Postman

Colección de casos de prueba para el Bounded Context **Verification** (EP-07).

---

## Implementadas

_Ninguna aún. EP-07 está pendiente de implementación._

---

## Pendientes

| HU | Descripción | Endpoint previsto |
|---|---|---|
| HU21 | Solicitar verificación de docente | `POST /api/v1/verification/teacher` |
| HU22 | Solicitar verificación de organización | `POST /api/v1/verification/organization` |

---

## Reglas de negocio aplicables

- RN-23: Toda solicitud requiere documento válido adjunto.
- RN-24: Solo una solicitud activa por entidad.
- RN-25: Los flujos de verificación de docente y organización son distintos.

---

## Variables de entorno previstas

| Variable | Descripción | Requerida en |
|---|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` | Todas |
| `{{teacher_token}}` | JWT de un usuario con perfil TEACHER | HU21 |
| `{{org_token}}` | JWT de un usuario con perfil ORGANIZATION | HU22 |
