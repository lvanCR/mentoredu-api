# HU35 — View received academic feedback

**Endpoint**: `GET {{api_v1}}/feedback/me`  
**Auth**: `Bearer {{student_token}}`  

---

## Descripción

Devuelve el historial de retroalimentación académica recibida por el usuario autenticado,  
ordenado por fecha descendente (más reciente primero).  
Si no hay retroalimentación registrada, devuelve lista vacía sin error.

---

## Reglas de negocio

- **RN-37**: Las entradas son de solo lectura — no se expone PUT/PATCH/DELETE.
- **RN-38**: El estudiante receptor puede consultar pero no modificar.

---

## Escenarios Gherkin → casos

| Caso | Escenario | HTTP esperado |
|---|---|---|
| caso-01.json | Exitoso — tiene retroalimentación registrada → 200 con lista ordenada | 200 OK |
| caso-02.json | Error — no autenticado | 401 Unauthorized |
| caso-03.json | Alternativo exitoso — sin retroalimentación → 200 lista vacía | 200 OK |
| caso-04.json | Alternativo error — intento de modificar entrada (método no permitido) | 405 Method Not Allowed |
