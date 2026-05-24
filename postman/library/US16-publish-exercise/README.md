# US16 — Publicar un ejercicio sin solución

Permite a un docente o academia publicar un recurso de tipo `PRACTICA` con `acepta_resoluciones=true`.

**Endpoint:** `POST /api/v1/resources`

**Headers:** `Authorization: Bearer {{teacher_token}}`, `Content-Type: application/json`

## Casos

| # | Escenario | Status esperado |
|---|---|---|
| 01 | Ejercicio publicado correctamente (PRACTICA + acepta_resoluciones=true) | 201 Created |
| 02 | acepta_resoluciones=true en tipo no PRACTICA | 400 Bad Request |
| 03 | Estudiante intenta activar acepta_resoluciones | 403 Forbidden |
| 04 | Sin fileUrl (metadatos de archivo ausentes) | 400 Bad Request |
