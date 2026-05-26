# Postman y Swagger — MentorEdu API v2.0

Guía de referencia rápida para pruebas manuales de los endpoints REST.  
Los casos de aceptación detallados viven en `postman/` (un `README.md` y archivos `caso-NN.json` por Historia).

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

Con el backend corriendo, los recursos Swagger están en:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## Importar la colección en Postman

1. **Import → Link** → `http://localhost:8080/v3/api-docs`  
   Postman genera los requests automáticamente desde la definición OpenAPI.

2. **Environments → Import** → `postman/environments/local.postman_environment.json`  
   Activa el entorno **MentorEdu — Local**.

### Variables de entorno disponibles

| Variable | Descripción |
|---|---|
| `{{base_url}}` | `http://localhost:8080` |
| `{{api_v1}}` | `http://localhost:8080/api/v1` (URL completa del prefijo) |
| `{{access_token}}` | JWT del usuario activo (STUDENT por defecto) — obtener de US01/US02 |
| `{{refresh_token}}` | Refresh token de la sesión activa — vigencia 7 días |
| `{{teacher_token}}` | JWT de usuario TEACHER — necesario para US07, US08, US16, US17, US19 |
| `{{academy_token}}` | JWT de usuario ACADEMY — necesario para US06, US22 (academy), US24 (aceptar/rechazar) |
| `{{moderator_token}}` | JWT del moderator seed (`moderator@mentoredu.com`) — para US23, US26 |
| `{{admin_token}}` | JWT del admin seed (`admin@mentoredu.com`) — para US28, casos alternativos US26 |
| `{{student_token}}` | JWT de un usuario STUDENT — usado en casos de error (esperan 403) |
| `{{university_id}}` | UUID de UNMSM del seed V9: `b1000000-0000-0000-0000-000000000001` |
| `{{area_id}}` | UUID de Área A (UNMSM) del seed V9: `b3000000-0000-0000-0000-000000000001` |
| `{{course_id}}` | UUID de curso del seed V9: `b2000000-0000-0000-0000-000000000001` |
| `{{career_id}}` | UUID de carrera del seed V9: `b4000000-0000-0000-0000-000000000001` |
| `{{target_user_id}}` | UUID de otro usuario — para US21 (follow) |
| `{{thread_id}}` | UUID de un hilo existente — obtener de POST /threads (US12) |
| `{{closed_thread_id}}` | UUID de un hilo con status=CLOSED — para US13 caso-03 (espera 422) |
| `{{answer_id}}` | UUID de una respuesta existente — obtener de POST /threads/{id}/answers (US13) |
| `{{comment_id}}` | UUID de un comentario existente — obtener de POST /answers/{id}/comments (US15) |
| `{{resource_id}}` | UUID de un recurso académico — obtener de POST /resources (US08) |
| `{{solution_id}}` | UUID de una solución — obtener de POST /resources/{id}/solutions (US18) |
| `{{report_id}}` | UUID de un reporte abierto — obtener de POST /moderation/reports (US25) |
| `{{resolved_report_id}}` | UUID de un reporte ya resuelto — para US26 caso-04 (espera 409) |
| `{{verification_id}}` | UUID de solicitud de verificación — obtener de POST /verification/requests (US22) |
| `{{association_id}}` | UUID de asociación docente-academia — obtener de POST /associations/teacher-academy (US24) |
| `{{academy_profile_id}}` | UUID del perfil de academia destino — obtener de POST /profiles/academy (US06) |
| `{{notification_id}}` | UUID de una notificación — obtener de GET /notifications/me (US27) |

> **Flujo de autenticación:** ejecuta primero `US01 — Registro` o `US02 — Login`. El `access_token` y `refresh_token` del response se copian a las variables de entorno y se reusan en las demás US.

---

## Estructura de carpetas

```
postman/
├── environments/
│   └── local.postman_environment.json
│
├── auth/                     → /api/v1/auth/**
│   ├── US01-registro/        → POST /auth/register            (8 casos)
│   ├── US02-login/           → POST /auth/login · POST /auth/logout (7 casos)
│   ├── US02-refresh/         → POST /auth/refresh             (4 casos)
│   ├── US03-password-recovery/ → POST /auth/forgot-password   (4 casos)
│   └── US03-reset-password/  → POST /auth/reset-password      (6 casos)
│
├── profile/                  → /api/v1/profiles/**
│   ├── US04-student-profile/ → POST /profiles/student         (crear perfil estudiante)
│   ├── US04-student-profile-update/ → PATCH /profiles/student/me
│   ├── US05-teacher-profile/ → POST /profiles/teacher
│   ├── US05-teacher-profile-update/ → PATCH /profiles/teacher/me
│   ├── US06-academy-profile/ → POST /profiles/academy
│   ├── profile-me/           → GET /profiles/me
│   └── F04-get-my-profile/   → GET /profiles/me (flujo completo)
│
├── library/                  → /api/v1/resources/**
│   ├── US07-upload-pdf/      → POST /resources/files          (4 casos)
│   ├── US08-register-metadata/ → POST /resources              (metadatos)
│   ├── US09-search-resources/ → GET /resources?query=...
│   ├── US10-download-resource/ → GET /resources/{id}/download (4 casos)
│   ├── US11-my-resources/    → GET /resources/me
│   └── US16-publish-exercise/ → POST /resources (acepta_resoluciones=true)
│
├── forum/                    → /api/v1/threads/**
│   ├── US12-create-thread/   → POST /threads · GET /threads · GET /threads/{id}
│   │                            PATCH /threads/{id}/close
│   ├── US13-reply-to-thread/ → POST /threads/{id}/answers · GET /threads/{id}/answers
│   ├── US14-react-to-content/ → POST /threads/{id}/reactions
│   │                            POST /answers/{id}/reactions
│   │                            POST /comments/{id}/reactions
│   └── US15-comment-on-answer/ → POST /answers/{id}/comments · GET /answers/{id}/comments
│
├── pedagogy/                 → /api/v1/resources/{id}/solutions/** · /api/v1/solutions/{id}/feedback
│   ├── US17-view-solutions/  → GET /resources/{id}/solutions
│   ├── US18-submit-solution/ → POST /resources/{id}/solutions
│   ├── US19-give-feedback/   → POST /solutions/{id}/feedback
│   └── US20-view-my-solution/ → GET /resources/{id}/solutions/mine
│
├── community/                → /api/v1/users/** · /api/v1/verification/** · /api/v1/moderation/** · /api/v1/notifications/**
│   ├── US21-follow-user/     → POST /users/{id}/follow        (4 casos, toggle)
│   ├── US22-teacher-verification/ → POST /verification/requests (entityType=TEACHER)
│   │                               GET /verification/requests/me?page=0&size=20  (PagedResponse — paginado)
│   ├── US22-academy-verification/ → POST /verification/requests (entityType=ACADEMY)
│   ├── US23-review-verification/ → GET /verification/requests?page=0&size=20 (PagedResponse) · PATCH /verification/requests/{id}/review
│   ├── US24-associate-teacher/ → POST /associations/teacher-academy · GET /associations/teacher-academy/me · GET /associations/teacher-academy/academy
│   │                            PATCH /associations/teacher-academy/{id}/accept · PATCH /associations/teacher-academy/{id}/reject
│   ├── US25-report-content/  → POST /moderation/reports       (4 casos)
│   ├── US26-resolve-report/  → GET /moderation/reports · PATCH /moderation/reports/{id}/resolve
│   └── US27-notifications/   → GET /notifications/me?type=<tipo> · GET /notifications/me/pending?type=<tipo> · PATCH /notifications/{id}/read
│
└── catalog/                  → /api/v1/catalog/**  (solo ADMIN)
    └── US28-manage-catalog/  → GET /catalog/universities · POST /catalog/universities
                                 GET /catalog/universities/{id}/areas · POST /catalog/universities/{id}/areas
                                 GET /catalog/courses · GET /catalog/areas/{id}/courses · POST /catalog/courses
                                 PUT /catalog/areas/{areaId}/courses/{courseId}       ← PUT (no POST)
                                 GET /catalog/universities/{id}/careers · POST /catalog/universities/{id}/careers
                                 PUT /catalog/careers/{careerId}/courses/{courseId}   ← PUT (no POST)
```

---

## Convención de archivos de caso

```
caso-{NN}-{descripcion-corta}.json
```

Cada archivo tiene la estructura:

```json
{
  "description": "Descripción del escenario",
  "method": "POST",
  "path": "/api/v1/auth/register",
  "headers": { "Content-Type": "application/json" },
  "request": { "body": { ... } },
  "expected_response": {
    "status": 201,
    "body_contains": ["accessToken", "refreshToken"]
  }
}
```

- Usar `{{variable}}` para UUIDs, tokens y valores dinámicos.
- Los campos `<uuid-generado-por-el-sistema>` son placeholders — reemplazar con variables de entorno reales.

---

## Convención de nombre de request en Postman

```
MentorEdu{BC}{US}-{Accion}{MétodoHTTP}
```

| Historia | Nombre del request |
|---|---|
| US01 Registro | `MentorEduAuthUS01-RegistroPOST` |
| US02 Login | `MentorEduAuthUS02-LoginPOST` |
| US02 Logout | `MentorEduAuthUS02-LogoutPOST` |
| US02 Refresh | `MentorEduAuthUS02-RefreshPOST` |
| US03 Forgot password | `MentorEduAuthUS03-ForgotPasswordPOST` |
| US03 Reset password | `MentorEduAuthUS03-ResetPasswordPOST` |
| US04 Crear perfil estudiante | `MentorEduProfileUS04-StudentProfilePOST` |
| US07 Subir PDF | `MentorEduLibraryUS07-UploadPdfPOST` |
| US08 Registrar metadatos | `MentorEduLibraryUS08-RegisterMetadataPOST` |
| US09 Buscar recursos | `MentorEduLibraryUS09-SearchGET` |
| US12 Crear hilo | `MentorEduForumUS12-CreateThreadPOST` |
| US18 Enviar resolución | `MentorEduPedagogyUS18-SubmitSolutionPOST` |
| US19 Dar feedback | `MentorEduPedagogyUS19-GiveFeedbackPOST` |
| US25 Reportar contenido | `MentorEduCommunityUS25-ReportContentPOST` |
| US26 Resolver reporte | `MentorEduCommunityUS26-ResolveReportPATCH` |
| US28 Administrar catálogo | `MentorEduCatalogUS28-CreateUniversityPOST` |

---

## Flujo de prueba recomendado

### Setup inicial (una vez)

1. `mvn spring-boot:run`
2. Importar colección desde `http://localhost:8080/v3/api-docs`
3. Importar entorno `postman/environments/local.postman_environment.json`
4. Activar entorno **MentorEdu — Local**

### Por cada Historia de Usuario

1. Leer `postman/{bc}/{US}-{nombre}/README.md` — endpoint, reglas de negocio, escenarios.
2. Ejecutar el `caso-01` (caso exitoso) primero; verificar status code y body.
3. Ejecutar los casos de error (4xx): validación, auth, duplicados, not found.
4. Copiar tokens/IDs del response a las variables de entorno para encadenar US dependientes.

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

## Headers requeridos

| Tipo de request | Headers obligatorios |
|---|---|
| Rutas públicas (`/auth/register`, `/auth/login`) | `Content-Type: application/json` |
| Rutas protegidas | `Content-Type: application/json` + `Authorization: Bearer {{access_token}}` |
| Upload de archivo (`US07`) | `Authorization: Bearer {{access_token}}` (sin `Content-Type` — Postman lo pone automático en `form-data`) |
| Rutas de MODERATOR/ADMIN | `Authorization: Bearer {{moderator_token}}` |

---

## Comportamientos a verificar específicamente

### Sesiones (US02)
- Login crea una sesión nueva; el `refresh_token` no expira hasta 7 días o logout explícito.
- Logout revoca la sesión — el `refresh_token` queda inutilizable.
- `/auth/refresh` con token revocado → 401.
- Si el mismo usuario abre más de 5 sesiones simultáneas, las más antiguas se revocan automáticamente.
- Los refresh tokens se almacenan hasheados (SHA-256) en BD — el valor en la respuesta es el token en claro (no el hash).

### Rate limiting en `/auth/**`
- Más de 20 requests por minuto desde la misma IP → 429 Too Many Requests.
- La respuesta incluye el header `Retry-After: 60` (segundos).
- Solo activo en perfil `prod`; en local no aplica.

### Tokens JWT
- Access token válido 15 minutos. Expirado → 401 (no 403).
- El token incluye claims `iss=mentoredu-api` y `aud=mentoredu-frontend`.
- Token inválido/expirado → `JwtAuthFilter` retorna **401 con JSON body** (ya no es body vacío):
  ```json
  { "timestamp": "<ISO>", "status": 401, "error": "Unauthorized", "message": "Token inválido o expirado." }
  ```
- El rol del usuario se extrae del JWT (claim `role`) — ya no se consulta la BD en cada request.

### Reacciones (US14)
- Misma reacción dos veces → toggle (elimina, 204 No Content).
- Reacción diferente sobre el mismo target → actualiza, 201 Created.
- `reactionType` es String abierto (LIKE, HELPFUL, DISLIKE, etc.).

### Audit log de moderación (US26)
- Al resolver un reporte (`PATCH /moderation/reports/{id}/resolve`), se crea automáticamente una fila en `moderation_audit_log`.
- No hay endpoint público para consultarlo (solo acceso directo a BD en auditorías).
- **Body del request (campo único):** `{ "resolutionNote": "texto obligatorio" }` — los campos anteriores `resolution`, `actionType` y `notes` ya no existen.
- Solo MODERATOR/ADMIN pueden resolver reportes (`@PreAuthorize`). Un STUDENT recibe 403 con mensaje estandarizado.

### Validaciones de enum en requests
- `Report.targetType` acepta solo: `THREAD`, `ANSWER`, `COMMENT`, `RESOURCE` → otro valor: 400.
- `VerificationRequest.entityType` acepta solo: `TEACHER`, `ACADEMY` → otro valor: 400.
- `ReviewVerificationRequest.action` acepta solo: `APPROVED`, `REJECTED` → otro valor: 400.
- `PublishResourceRequest.visibility` acepta solo: `PUBLIC`, `PREMIUM`, `PRIVATE` → otro valor: 400 (antes era validación por regex, ahora es deserialización del enum Jackson).

### Códigos de error actualizados
| Excepción | Código anterior | Código actual |
|---|---|---|
| `ThreadClosedException` (hilo cerrado) | 409 | **422 Unprocessable Entity** |
| `MaxUploadSizeExceededException` (archivo > 20 MB) | 400 | **413 Payload Too Large** |
| `WrongProfileTypeException` (tipo de perfil incorrecto) | 409 | **400 Bad Request** |
| JWT inválido/expirado (filtro) | 401 sin body | **401 con JSON body** |
| `AccessDeniedException` Spring Security | sin capturar | **403 Forbidden** con mensaje uniforme |

### Endpoints del catálogo — método HTTP
Los endpoints de asociación curso↔área y curso↔carrera cambiaron de `POST` a `PUT` (semántica de idempotencia):
- `PUT /api/v1/catalog/areas/{areaId}/courses/{courseId}` (antes era POST)
- `PUT /api/v1/catalog/careers/{careerId}/courses/{courseId}` (antes era POST)

### Recuperación de contraseña — comportamiento para email no registrado
- `POST /auth/forgot-password` **siempre retorna 200** con el mismo mensaje genérico, independientemente de si el email existe.
- Esto previene enumeración de usuarios (OWASP A07). No existe el error 404 en este endpoint.

---

## Usuarios seed disponibles en local

| Email | Contraseña | Rol |
|---|---|---|
| `admin@mentoredu.com` | Ver `.env` (BCrypt en V10) | ADMIN |
| `moderator@mentoredu.com` | Ver `.env` (BCrypt en V10) | MODERATOR |

Registrar usuarios STUDENT/TEACHER/ACADEMY via `POST /api/v1/auth/register` con el campo `role`.

---

## Agregar casos a una Historia existente

1. Crea `caso-{NN}-{descripcion}.json` en la carpeta de la US correspondiente.
2. Actualiza el `README.md` de la US con el nuevo escenario.
3. Si cambió la respuesta de un endpoint existente, actualiza los `caso-XX.json` afectados.
