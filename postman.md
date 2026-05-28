# Postman y Swagger — MentorEdu API v2.0

Guía de referencia rápida para pruebas manuales de los endpoints REST.
Los casos de aceptación viven en `postman/` (un `README.md` y archivos `caso-NN.json` por Historia).

---

## Prerrequisitos

| Herramienta | Propósito |
|---|---|
| Docker Desktop | Levanta PostgreSQL vía `compose.yml` |
| Java 21 + Maven | Arranca el backend |
| Postman (desktop) | Ejecuta los casos de prueba |

```bash
mvn spring-boot:run          # Docker Compose levanta PostgreSQL automáticamente
```

Con el backend corriendo:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## Importar la colección en Postman

1. **Import → Link** → `http://localhost:8080/v3/api-docs`
2. **Environments → Import** → `postman/environments/local.postman_environment.json`
3. Activar entorno **MentorEdu — Local**.

---

## Variables de entorno

| Variable | Descripción | Valor de referencia (BD local) |
|---|---|---|
| `{{base_url}}` | Base sin prefijo | `http://localhost:8080` |
| `{{api_v1}}` | Prefijo completo | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario activo (obtener de US01/US02) | — |
| `{{refresh_token}}` | Refresh token activo (vigencia 7 días) | — |
| `{{teacher_token}}` | JWT de TEACHER — para US07, US08, US16, US17, US19 | — |
| `{{academy_token}}` | JWT de ACADEMY — para US06, US22, US24 (accept/reject) | — |
| `{{moderator_token}}` | JWT de moderator seed | — |
| `{{admin_token}}` | JWT de admin seed | — |
| `{{student_token}}` | JWT de STUDENT — usado en casos de error (403 esperado) | — |
| `{{university_id}}` | UUID de universidad del catálogo | `b1000000-0000-0000-0000-000000000001` (UNMSM) |
| `{{area_id}}` | UUID de área del catálogo | `b3000000-0000-0000-0000-000000000002` (Área B UNMSM) |
| `{{course_id}}` | UUID de curso del catálogo | `b2000000-0000-0000-0000-000000000001` (Matemática) |
| `{{career_id}}` | UUID de carrera del catálogo | seed V9 (ver `docs/seed-plan-admision-lima.md`) |
| `{{target_user_id}}` | UUID de otro usuario — para US21 (follow) | `23f48c4c-7c7b-4823-b535-b6105c551f92` (carlos) |
| `{{thread_id}}` | UUID de hilo OPEN — obtener de POST /threads (US12) | `42bccf1d-6598-44ec-be02-67fc74d62689` |
| `{{closed_thread_id}}` | UUID de hilo CLOSED — para US13 escenario de error (espera **409**) | `f772283d-20fa-43dd-aa67-efa2a9dfd3a6` |
| `{{answer_id}}` | UUID de respuesta — obtener de POST /threads/{id}/answers (US13) | `55225559-06fd-43c2-b476-9491474ae7b4` |
| `{{comment_id}}` | UUID de comentario — obtener de POST /answers/{id}/comments (US15) | — |
| `{{resource_id}}` | UUID de recurso — obtener de POST /resources (US08) | `a4ea3a21-2479-429b-b46c-8df18d81bf9f` |
| `{{practica_resource_id}}` | UUID de recurso PRACTICA con acepta_resoluciones=true | `7d88d5b5-1281-4937-bebd-f09e025135ac` |
| `{{solution_id}}` | UUID de solución — obtener de POST /resources/{id}/solutions (US18) | `b775dcce-2921-483b-8da5-bc39a7ff8be8` |
| `{{report_id}}` | UUID de reporte OPEN — obtener de POST /moderation/reports (US25) | `51244a37-2d26-4cfd-8780-f53aecaa92fa` |
| `{{resolved_report_id}}` | UUID de reporte ya RESOLVED — para US26 escenario de error (409) | `96beebe2-faa1-4fc8-8cbd-bf90aa886fca` |
| `{{verification_id}}` | UUID de solicitud de verificación — obtener de POST /verification/requests (US22) | `a664de24-ba3f-4138-ba98-157b04f18027` |
| `{{association_id}}` | UUID de asociación docente-academia (US24) | `116a75b8-399c-4f7f-88d7-9189ca37e2ab` |
| `{{academy_profile_id}}` | UUID del perfil de academia destino | `1970a0f7-cd22-48d1-aff3-40ba2bf21ea4` (mendel) |
| `{{notification_id}}` | UUID de notificación — obtener de GET /notifications/me (US27) | — |

> **Flujo de autenticación:** ejecuta primero US01 o US02. El `access_token` y `refresh_token` del response se copian a variables de entorno y se reusan en las demás US.

---

## Estructura de carpetas

```
postman/
├── environments/
│   └── local.postman_environment.json
│
├── auth/                        → /api/v1/auth/**
│   ├── US01-registro/           → POST /auth/register            (8 casos)
│   ├── US02-login/              → POST /auth/login · POST /auth/logout (7 casos)
│   ├── US02-refresh/            → POST /auth/refresh             (4 casos)
│   ├── US03-password-recovery/  → POST /auth/forgot-password     (4 casos)
│   └── US03-reset-password/     → POST /auth/reset-password      (6 casos)
│
├── profile/                     → /api/v1/profiles/**
│   ├── US04-student-profile/    → POST /profiles/student         (crear perfil estudiante)
│   ├── US04-student-profile-update/ → PATCH /profiles/student/me
│   ├── US05-teacher-profile/    → POST /profiles/teacher
│   ├── US05-teacher-profile-update/ → PATCH /profiles/teacher/me
│   ├── US06-academy-profile/    → POST /profiles/academy
│   ├── US06-academy-profile-update/ → PATCH /profiles/academy/me
│   ├── profile-me/              → GET /profiles/me · PATCH /profiles/me  ← datos comunes del perfil propio
│   └── F04-get-my-profile/      → GET /profiles/{userId}         ← perfil público de cualquier usuario
│
├── library/                     → /api/v1/resources/**
│   ├── US07-upload-pdf/         → POST /resources/files          (multipart/form-data)
│   ├── US08-register-metadata/  → POST /resources · PATCH /resources/{id}/settings
│   ├── US09-search-resources/   → GET /resources?q=...&universityId=...&type=...
│   ├── US10-download-resource/  → GET /resources/{id}/download
│   ├── US11-my-resources/       → GET /resources/me
│   └── US16-publish-exercise/   → POST /resources (acepta_resoluciones=true) · PATCH /resources/{id}/settings
│
├── forum/                       → /api/v1/threads/** · /api/v1/answers/** · /api/v1/comments/**
│   ├── US12-create-thread/      → POST /threads · GET /threads · GET /threads/{id}
│   │                               PATCH /threads/{id}/close
│   ├── US13-reply-to-thread/    → POST /threads/{id}/answers · GET /threads/{id}/answers
│   ├── US14-react-to-content/   → POST /threads/{id}/reactions
│   │                               POST /answers/{id}/reactions
│   │                               POST /comments/{id}/reactions      ← tres targets válidos
│   └── US15-comment-on-answer/  → POST /answers/{id}/comments · GET /answers/{id}/comments
│
├── pedagogy/                    → /api/v1/resources/{id}/solutions/** · /api/v1/solutions/{id}/feedback
│   ├── US17-view-solutions/     → GET /resources/{id}/solutions · GET /resources/{id}/solutions/{solutionId}
│   ├── US18-submit-solution/    → POST /resources/{id}/solutions
│   ├── US19-give-feedback/      → POST /solutions/{id}/feedback
│   └── US20-view-my-solution/   → GET /resources/{id}/solutions/mine
│
├── community/                   → /api/v1/users/** · /api/v1/verification/** · /api/v1/associations/**
│   │                               /api/v1/moderation/** · /api/v1/notifications/**
│   ├── US21-follow-user/        → POST /users/{id}/follow            (toggle: 201 crear / 204 eliminar)
│   ├── US22-teacher-verification/ → POST /verification/requests (entityType=TEACHER)
│   │                                GET /verification/requests/me?page=0&size=20
│   ├── US22-academy-verification/ → POST /verification/requests (entityType=ACADEMY)
│   ├── US23-review-verification/ → GET /verification/requests?page=0&size=20
│   │                               PATCH /verification/requests/{id}/review
│   ├── US24-associate-teacher/  → POST /associations/teacher-academy
│   │                               GET /associations/teacher-academy/me
│   │                               GET /associations/teacher-academy/academy
│   │                               PATCH /associations/teacher-academy/{id}/accept
│   │                               PATCH /associations/teacher-academy/{id}/reject
│   ├── US25-report-content/     → POST /moderation/reports
│   ├── US26-resolve-report/     → GET /moderation/reports
│   │                               PATCH /moderation/reports/{id}/resolve
│   └── US27-notifications/      → GET /notifications/me
│                                   GET /notifications/me/pending
│                                   PATCH /notifications/{id}/read
│
└── catalog/                     → /api/v1/catalog/**  (solo ADMIN puede escribir)
    └── US28-manage-catalog/     → GET /catalog/universities          · POST /catalog/universities
                                    GET /catalog/universities/{id}/areas · POST /catalog/universities/{id}/areas
                                    GET /catalog/courses               · GET /catalog/areas/{id}/courses
                                    POST /catalog/courses
                                    PUT /catalog/areas/{areaId}/courses/{courseId}      ← PUT, sin body
                                    GET /catalog/universities/{id}/careers · POST /catalog/universities/{id}/careers
                                    PUT /catalog/careers/{careerId}/courses/{courseId}  ← PUT, sin body
```

---

## Convención de archivos de caso

```
caso-{NN}-{descripcion-corta}.json
```

```json
{
  "name": "MentorEdu{BC}HU{NN}-{Accion}{MétodoHTTP}",
  "description": "Descripción del escenario",
  "request": {
    "method": "POST",
    "url": "{{api_v1}}/auth/register",
    "header": [
      { "key": "Content-Type", "value": "application/json" },
      { "key": "Authorization", "value": "Bearer {{access_token}}" }
    ],
    "body": { "mode": "raw", "raw": "{ ... }" }
  },
  "response": {
    "status": 201,
    "description": "Descripción de la respuesta esperada"
  }
}
```

> Usar `{{variable}}` para tokens, UUIDs y valores dinámicos.

---

## Headers requeridos

| Tipo de request | Headers obligatorios |
|---|---|
| Rutas públicas (`/auth/register`, `/auth/login`) | `Content-Type: application/json` |
| Rutas protegidas | `Content-Type: application/json` + `Authorization: Bearer {{access_token}}` |
| Upload de archivo (`US07`) | `Authorization: Bearer {{access_token}}` (sin `Content-Type` — Postman lo pone automático en `form-data`) |
| Rutas de MODERATOR/ADMIN | `Authorization: Bearer {{moderator_token}}` o `{{admin_token}}` |

---

## Comportamientos verificados

### Sesiones (US02)
- Login crea una sesión nueva; el `refresh_token` no expira hasta 7 días o logout explícito.
- Logout revoca la sesión — el `refresh_token` queda inutilizable.
- `/auth/refresh` con token revocado → **401**.
- Si el mismo usuario abre más de 5 sesiones simultáneas, las más antiguas se revocan automáticamente.
- Los refresh tokens se almacenan hasheados (SHA-256) en BD; el valor en la respuesta es el token en claro.

### Tokens JWT
- Access token válido 15 minutos. Expirado → **401** (no 403).
- Token inválido/expirado → respuesta JSON:
  ```json
  { "timestamp": "...", "status": 401, "error": "Unauthorized", "message": "Autenticación requerida." }
  ```
- El rol se extrae del claim `role` del JWT — no se consulta la BD en cada request.

### Perfiles (US04–US06)
- `GET /profiles/me` → datos completos del usuario autenticado (`ProfileMeResponse`).
- `PATCH /profiles/me` → actualiza campos comunes (displayName, bio, city, avatarUrl).
- `GET /profiles/{userId}` → perfil público de cualquier usuario (requiere autenticación).
- `POST /profiles/student` → crea student_profile; solo rol STUDENT, una vez.
- `POST /profiles/teacher` → crea teacher_profile; solo rol TEACHER, una vez.
- `POST /profiles/academy` → crea academy_profile; solo rol ACADEMY, una vez.

### Recursos (US07–US11, US16)
- `POST /resources/files` acepta cualquier usuario autenticado (incluso STUDENT). La restricción de rol aplica en US08.
- `POST /resources` y `PATCH /resources/{id}/settings` requieren TEACHER/ACADEMY/ADMIN.
- `acepta_resoluciones=true` solo válido cuando `resource_type=PRACTICA`. Otro tipo → **400**.
- `courseId` obligatorio para EXAMEN_SECCION, PRACTICA y OTRO. Opcional para EXAMEN_COMPLETO, GUIA, APUNTES.
- `careerId` siempre opcional; si presente, debe pertenecer al `areaId` enviado → caso contrario **400** (RN-23).

### Foro (US12–US15)
- Un hilo requiere al menos uno de: `universityId`, `courseId`, `careerId`.
- `areaId` solo válido acompañado de `universityId`. Solo → **400**.
- `careerId` + `courseId` simultáneos → **400**.
- Hilo CLOSED: responder → **409 Conflict**.
- Reacciones: toggle. Misma reacción → **204** (elimina). Reacción distinta → **201** (reemplaza). Nueva → **201**.
- `reactionType`: String libre (sin validación enum en servidor). Solo `LIKE` y `DISLIKE` tienen semántica definida. Cualquier otro string es aceptado técnicamente pero no dispara notificaciones.
- Reacciones disponibles en tres targets: THREAD (`/threads/{id}/reactions`), ANSWER (`/answers/{id}/reactions`), COMMENT (`/comments/{id}/reactions`).

### Ciclo pedagógico (US16–US20)
- Solo STUDENT puede enviar resoluciones. TEACHER/ACADEMY → **403**.
- Un STUDENT puede enviar exactamente una resolución por ejercicio. Segunda → **409**.
- Feedback es inmutable. Segundo feedback en misma solución → **409**.
- `score` es opcional (null válido). Si presente: 0.0–10.0 inclusive. Fuera de rango → **400**.
- Al dar feedback, la solución pasa automáticamente a status=REVIEWED.
- `GET /resources/{id}/solutions/mine` → **404** si el estudiante no envió resolución.

### Verificación y asociaciones (US22–US24)
- `PATCH /verification/requests/{id}/review` — único endpoint para aprobar y rechazar. Campo `action: APPROVED|REJECTED`.
- Rechazar sin `notes` → **400** (RN-17).
- Solicitud ya procesada → **409**.
- Endpoint base de asociaciones: `/api/v1/associations/teacher-academy`.
- Accept y reject son endpoints separados: `.../accept` y `.../reject`.

### Moderación (US25–US26)
- Request de reporte: `{targetType, targetId, reason}`.
- `targetType` acepta: `THREAD`, `ANSWER`, `COMMENT`, `RESOURCE`. Otro valor → **400**.
- Request de resolución: `{resolutionNote}` (único campo, obligatorio).
- Reporte ya resuelto → **409**.
- Al resolver, se crea fila en `moderation_audit_log` (RN-19). No hay endpoint público para consultarlo.

### Notificaciones (US27)
- `GET /notifications/me` → todas (leídas y no leídas), paginado.
- `GET /notifications/me/pending` → solo no leídas (`read_at IS NULL`), paginado.
- Filtro opcional `?type=new_follower|answer_received|...` disponible en ambos GET.
- `PATCH /notifications/{id}/read` → **204** No Content. Body vacío.

### Catálogo (US28)
- `PUT /catalog/areas/{areaId}/courses/{courseId}` → **204** (sin body). Asocia curso a área.
- `PUT /catalog/careers/{careerId}/courses/{courseId}` → **204** (sin body). Asocia curso a carrera.
- Solo ADMIN puede crear/modificar catálogo. Otro rol → **403**.
- GET de catálogo requiere autenticación.

---

## Códigos de respuesta HTTP de referencia

| Situación | Código |
|---|---|
| Creación exitosa | 201 Created |
| Actualización exitosa | 200 OK |
| Toggle elimina (follow/react) | 204 No Content |
| Mark as read (notificación) | 204 No Content |
| Link curso-área/carrera (PUT) | 204 No Content |
| Sin autenticación | 401 Unauthorized |
| Rol incorrecto / no autorizado | 403 Forbidden |
| Recurso no encontrado (path param) | 404 Not Found |
| Referencia catálogo no existe (body) | 400 Bad Request |
| Hilo cerrado al responder | 409 Conflict |
| Duplicado (email, perfil, solución, feedback, asociación) | 409 Conflict |
| Solicitud ya procesada (verificación, reporte) | 409 Conflict |
| Validación de campos | 400 Bad Request |
| Archivo > 20 MB | 413 Payload Too Large |
| Tipo de archivo no PDF | 415 Unsupported Media Type |

---

## Flujo de prueba recomendado

### Setup inicial (una vez)

1. `mvn spring-boot:run`
2. Importar colección desde `http://localhost:8080/v3/api-docs`
3. Importar entorno `postman/environments/local.postman_environment.json`
4. Activar entorno **MentorEdu — Local**

### Por cada Historia de Usuario

1. Leer `postman/{bc}/{US}-{nombre}/README.md`.
2. Ejecutar el `caso-01` (happy path) primero.
3. Ejecutar los casos de error (4xx): validación, auth, duplicados, not found.
4. Copiar tokens/IDs del response a variables de entorno para encadenar US dependientes.

### Orden sugerido para validación integral

```
Auth        →  US01 → US02 → US03
Profile     →  US04 → US05 → US06
Library     →  US07 → US08 → US09 → US10 → US11
Forum       →  US12 → US13 → US14 → US15
Pedagogy    →  US16 → US18 → US17 → US19 → US20
Community   →  US21 → US22 → US23 → US24 → US25 → US26 → US27
Catalog     →  US28
```

---

## Usuarios seed disponibles en local

| Email | Contraseña | Rol | userId |
|---|---|---|---|
| `admin@mentoredu.com` | `AdminMentor2026!` | ADMIN | `00000000-0000-0000-0000-000000000001` |
| `moderator@mentoredu.com` | `ModMentor2026!` | MODERATOR | `00000000-0000-0000-0000-000000000002` |

Registrar usuarios STUDENT/TEACHER/ACADEMY via `POST /api/v1/auth/register` con el campo `role`.

---

## Features de Seguridad Activas

| Feature | Descripción | Estado |
|---|---|---|
| **Rate Limiting (429)** | Protege `/api/v1/**`. Auth: 20 req/min. API: 60 req/min. | ✅ Implementado |
| **Sesiones Concurrentes** | Límite de 5 sesiones activas por usuario con auto-revocación. | ✅ Implementado |
| **Hash de Tokens** | Refresh tokens y Reset tokens almacenados mediante SHA-256. | ✅ Implementado |
| **Auditoría de Moderación** | Registro obligatorio de acciones tomadas por Moderadores/Admin. | ✅ Implementado |

---

## Agregar casos a una Historia existente

1. Crea `caso-{NN}-{descripcion}.json` en la carpeta de la US correspondiente.
2. Actualiza el `README.md` de la US con el nuevo escenario.
3. Si cambió la respuesta de un endpoint existente, actualiza los `caso-XX.json` afectados.
