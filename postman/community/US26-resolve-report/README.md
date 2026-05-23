# HU20 — Resolver un reporte

**Endpoint**: `PATCH /api/v1/moderation/reports/{id}/resolve`  
**Autenticación**: Bearer Token (rol MODERATOR o ADMIN requerido)  
**Nombre Postman**: `MentorEduModerationHU20-ResolveReportPATCH`

---

## Campos del body

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `resolution` | String (enum) | Sí | Estado final: `RESOLVED` o `REJECTED` |
| `actionType` | String (enum) | Sí | Acción tomada: `HIDE`, `DELETE`, `WARN`, `SUSPEND`, `RESTORE` |
| `notes` | String | No | Notas adicionales del moderador |

---

## Casos de prueba

| Archivo | Escenario Gherkin | HTTP | Descripción |
|---|---|---|---|
| `caso-01.json` | Exitoso | 200 | Moderador resuelve reporte con acción HIDE |
| `caso-02.json` | Error — sin permisos | 403 | Usuario sin rol de moderador intenta resolver |
| `caso-03.json` | Alternativo exitoso | 200 | Admin rechaza reporte (REJECTED) con acción RESTORE |
| `caso-04.json` | Alternativo error | 409 | Reporte ya fue resuelto anteriormente |

---

## Variables requeridas

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{moderator_token}}` | JWT de un usuario con rol MODERATOR |
| `{{admin_token}}` | JWT de un usuario con rol ADMIN |
| `{{student_token}}` | JWT de un usuario con rol STUDENT (para caso-02) |
| `{{report_id}}` | UUID de un reporte en estado OPEN |
| `{{resolved_report_id}}` | UUID de un reporte ya resuelto (para caso-04) |
