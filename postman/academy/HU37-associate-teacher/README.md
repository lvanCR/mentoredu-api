# HU37 — Associate Teacher to Academy

**Endpoint:** `POST /api/v1/academies/{academyId}/teachers`  
**Auth:** `Authorization: Bearer <access_token>` (cuenta de tipo ORGANIZATION, propietaria de la academia)

---

## Casos de prueba

| # | Escenario | Resultado esperado |
|---|---|---|
| 01 | Asociación exitosa con docente válido | 201 Created |
| 02 | Docente adicional (academia con docentes previos) | 201 Created |
| 03 | teacherProfileId nulo o ausente | 400 Bad Request |
| 04 | Perfil no existe o no es de tipo TEACHER | 404 Not Found |
| 05 | Docente ya asociado a la misma academia | 409 Conflict |
| 06 | Sin autenticación | 401 Unauthorized |

---

## Reglas de negocio aplicadas

- **RN-41**: Un docente puede asociarse a múltiples academias; una academia puede tener múltiples docentes.
- **RN-42**: Una asociación docente-academia no puede registrarse más de una vez.
- **RN-40**: Solo la organización propietaria puede gestionar los docentes de la academia.
