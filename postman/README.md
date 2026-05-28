# Postman — MentorEdu API v2.0

Casos de validación manual organizados por Historia de Usuario (US01–US28).

## Importar la colección

Desde Swagger/OpenAPI con el backend corriendo:

1. Abre Postman → **Import** → **Link**
2. URL: `http://localhost:8080/v3/api-docs`
3. Importar entorno: `environments/local.postman_environment.json`

Variables de entorno clave: `api_v1`, `access_token`, `refresh_token`, `teacher_token`, `academy_token`, `moderator_token`, `admin_token`, `student_token`, `target_user_id`, `thread_id`, `answer_id`, `resource_id`, `solution_id`, `report_id`, `verification_id`, `association_id`, `notification_id`, `university_id`, `area_id`, `course_id`, `career_id`.

> **Importante:** `api_v1` contiene la URL base completa (`http://localhost:8080/api/v1`). Todos los requests deben usar `{{api_v1}}/path` directamente. No combinar con `{{base_url}}`.

## Estructura

```
postman/
├── environments/
│   └── local.postman_environment.json
├── auth/               US01 registro · US02 login/logout/refresh · US03 recuperar contraseña
├── profile/            US04 perfil estudiante · US05 perfil docente · US06 perfil academia
├── library/            US07 subir PDF · US08 metadatos · US09 buscar · US10 descargar · US11 mis recursos · US16 ejercicio
├── forum/              US12 crear hilo · US13 responder · US14 reaccionar · US15 comentar
├── pedagogy/           US17 ver resoluciones · US18 enviar resolución · US19 dar feedback · US20 ver mi solución
├── community/          US21 seguir · US22 verificación · US23 revisar verificación · US24 asociación
│                       US25 reportar · US26 resolver reporte · US27 notificaciones
└── catalog/            US28 catálogo (solo ADMIN)
```

## Convención de archivos

`caso-{NN}-{descripcion-corta}.json` — usar `{{variable}}` de entorno para UUIDs y tokens.

## Flujo de trabajo

1. `mvn spring-boot:run` (Docker Compose levanta PostgreSQL automáticamente)
2. Importar colección desde `http://localhost:8080/v3/api-docs`
3. Activar entorno `MentorEdu — Local`
4. Consultar el `README.md` de la carpeta de la US antes de ejecutar
5. Copiar `request.body` del `caso-XX.json` a Postman → Body → raw → JSON

## Campos y esquemas clave (referencia rápida)

| Endpoint | Campo del body | Tipo | Notas |
|---|---|---|---|
| `POST /profiles/teacher` | `bioProfessional` | String (max 2000) | Único campo, opcional |
| `PATCH /profiles/teacher/me` | `bioProfessional` | String (max 2000) | Opcional en PATCH |
| `POST /profiles/academy` | `academyName`, `ruc`, `website`, `contactEmail` | String | `academyName` obligatorio |
| `PATCH /profiles/academy/me` | `academyName`, `website`, `contactEmail` | String | Todos opcionales |
| `POST /profiles/student` | `gradeLevel` (req), `schoolName`, `studyShift`, `targetUniversityId`, `targetAreaId`, `targetCareerId` | UUID para IDs | IDs son UUID del catálogo, no texto libre |
| `PATCH /verification/requests/{id}/review` | `action`, `notes` | `action`: APPROVED/REJECTED; `notes` req si REJECTED | Campo es `action`, no `decision` |
| `PATCH /moderation/reports/{id}/resolve` | `resolutionNote` | String (NotBlank) | Solo este campo |
| `POST /associations/teacher-academy` | `academyProfileId` | UUID | ID del perfil de academia |
| `PATCH /associations/teacher-academy/{id}/accept` | (sin body) | — | Solo el actor autenticado |
| `PATCH /associations/teacher-academy/{id}/reject` | (sin body) | — | Solo el actor autenticado |
| `PATCH /notifications/{id}/read` | (sin body) | — | Retorna 204 No Content |
| `POST /catalog/universities` | `name`, `city` | String | No existe campo `country` |
| `POST /catalog/universities/{id}/areas` | `name`, `description` | String | No existe campo `code` |
| `POST /catalog/universities/{id}/careers` | `areaId`, `name`, `description` | UUID + String | `areaId` obligatorio |

## Tokens necesarios por US

| Historia | Token requerido |
|---|---|
| US01–US03 (registro/login/recuperación) | Sin token (público) |
| US04–US06 (perfiles) | `{{access_token}}` del rol correspondiente |
| US07–US16 (biblioteca) | `{{teacher_token}}` o `{{academy_token}}` para escritura; `{{access_token}}` para lectura |
| US12–US15 (foro) | `{{access_token}}` |
| US17–US20 (pedagogía) | `{{teacher_token}}` para US17/US19; `{{student_token}}` para US18/US20 |
| US21 (seguir) | `{{access_token}}` |
| US22 (verificación) | `{{teacher_token}}` o `{{academy_token}}` |
| US23 (revisar verificación) | `{{moderator_token}}` o `{{admin_token}}` |
| US24 (asociar docente) | `{{teacher_token}}` para solicitar; `{{academy_token}}` para aceptar/rechazar |
| US25 (reportar) | `{{access_token}}` |
| US26 (resolver reporte) | `{{moderator_token}}` |
| US27 (notificaciones) | `{{access_token}}` |
| US28 (catálogo — escritura) | `{{admin_token}}` |
| US28 (catálogo — lectura) | `{{access_token}}` |
