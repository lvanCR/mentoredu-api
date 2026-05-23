# HU41 — View my solution and received feedback

## Endpoint

```
GET /api/v1/resources/{resourceId}/solutions/mine
Authorization: Bearer <jwt_token>
```

## Descripción

Permite al estudiante autenticado consultar la resolución que envió para un recurso académico,
junto con el feedback del docente si ya fue evaluada. Solo el propio estudiante puede llamar
este endpoint (RN-46).

## Respuesta exitosa — con feedback (status 200)

```json
{
  "solutionId": "uuid",
  "resourceId": "uuid",
  "resourceTitle": "Simulacro de Matemáticas UNI 2024",
  "fileId": "uuid",
  "fileUrl": "http://localhost:8080/uploads/resources/archivo.pdf",
  "status": "REVIEWED",
  "submittedAt": "2026-05-21T10:30:00",
  "feedback": {
    "id": "uuid",
    "body": "Excelente análisis en el punto 3. Revisa la integración del ejercicio 7.",
    "score": 8.5,
    "authorName": "Juan Quispe",
    "createdAt": "2026-05-21T14:00:00"
  }
}
```

## Respuesta exitosa — sin feedback aún (status 200)

```json
{
  "solutionId": "uuid",
  "resourceId": "uuid",
  "resourceTitle": "Simulacro de Matemáticas UNI 2024",
  "fileId": "uuid",
  "fileUrl": "http://localhost:8080/uploads/resources/archivo.pdf",
  "status": "SUBMITTED",
  "submittedAt": "2026-05-21T10:30:00",
  "feedback": null
}
```

## Casos de prueba

| Archivo       | Escenario                          | HTTP |
|---------------|------------------------------------|------|
| caso-01.json  | Éxito con feedback registrado      | 200  |
| caso-02.json  | Éxito sin feedback aún             | 200  |
| caso-03.json  | Estudiante no envió resolución     | 404  |
| caso-04.json  | No autenticado                     | 401  |

## Reglas de Negocio aplicadas

- **RN-46**: Solo el autor de la solución puede ver su propia resolución vía este endpoint.
- **RN-49**: Si el docente registró feedback (`solutionId` en FeedbackEntry), el status de la solución es `REVIEWED` automáticamente.
- **RN-51**: Las soluciones son privadas y no aparecen en búsquedas públicas.

## Flujo previo requerido

1. `POST /api/v1/resources/files` (HU12) → obtener `fileId`
2. `POST /api/v1/resources` (HU13) con `allowsSolutions=true` → obtener `resourceId`
3. `POST /api/v1/resources/{resourceId}/solutions` (HU39) → enviar resolución
4. *(Opcional)* `POST /api/v1/feedback` (HU40) con `solutionId` → docente da feedback
5. `GET /api/v1/resources/{resourceId}/solutions/mine` (HU41) → este endpoint
