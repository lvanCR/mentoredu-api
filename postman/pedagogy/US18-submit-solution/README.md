# HU39 — Submit solution to academic resource

## Endpoint

```
POST /api/v1/resources/{resourceId}/solutions
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

## Body

| Campo  | Tipo | Obligatorio | Descripción |
|--------|------|-------------|-------------|
| fileId | UUID | ✅ | ID del archivo PDF previamente subido con US12 |

## Reglas de negocio

| RN | Descripción |
|----|-------------|
| RN-45 | Solo puede existir una resolución por par `(resource_id, student_id)`. UNIQUE en BD. |
| RN-46 | La solución es privada: visible solo para su autor, el autor del recurso, moderadores y admins. |
| RN-47 | El recurso debe tener `allows_solutions=true`. Solo TEACHER/ACADEMY/ADMIN puede activarlo al publicar. |
| RN-31 | Se otorgan 3 XP al estudiante por enviar su primera y única resolución (`SOLUTION_SUBMITTED`). |

## Respuesta exitosa — 201 Created

```json
{
  "solutionId": "uuid",
  "resourceId": "uuid",
  "studentId": "uuid",
  "fileId": "uuid",
  "status": "SUBMITTED",
  "submittedAt": "2026-05-21T15:00:00"
}
```

## Escenarios de aceptación

| # | Archivo | Status | Descripción |
|---|---------|--------|-------------|
| 1 | caso-01.json | 201 | Resolución enviada correctamente |
| 2 | caso-02.json | 403 | Recurso con `allows_solutions=false` (RN-47) |
| 3 | caso-03.json | 409 | Solución duplicada para el mismo recurso (RN-45) |
| 4 | caso-04.json | 404 | Recurso no existe |
| 5 | caso-05.json | 401 | No autenticado |

## Prerrequisitos

1. Usuario con rol TEACHER/ACADEMY publica recurso con `allowsSolutions: true` → obtener `resourceId`.
2. El mismo usuario o cualquier TEACHER/ACADEMY sube un PDF con US12 → obtener `fileId`.
3. Usuario con rol STUDENT hace login → obtener JWT token.
4. Ejecutar `POST /api/v1/resources/{resourceId}/solutions` con el token del estudiante.
