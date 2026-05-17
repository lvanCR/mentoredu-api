# Postman — Guía de Pruebas MentorEdu

Workspace de documentación y pruebas organizado por bounded context.  
Cada Historia de Usuario implementada tiene su carpeta con ejemplos de requests y escenarios de aceptación.

---

## Estructura de carpetas

```
postman/
├── README.md                              ← esta guía
├── environments/
│   └── local.postman_environment.json     ← variables de entorno para local
├── auth/                                  ← EP-01 completo (HU01, HU02, HU03, HU26)
│   ├── README.md
│   ├── HU01-registro/
│   ├── HU02-login/
│   ├── HU03-password-recovery/
│   └── HU26-reset-password/
├── profile/                               ← EP-02 completo (HU04–HU10)
│   ├── README.md
│   ├── HU04-select-account-type/
│   ├── HU05-update-profile/
│   ├── HU06-create-student-profile/
│   ├── HU07-update-target-university/
│   ├── HU08-create-teacher-profile/
│   ├── HU09-update-teacher-specialty/
│   └── HU10-create-organization-profile/
├── academy/                               ← EP-03 completo (HU33, HU11)
│   ├── README.md
│   ├── HU33-create-academy/
│   └── HU11-register-academic-offering/
├── library/                               ← EP-04 completo (HU12–HU15)
│   ├── README.md
│   ├── HU12-upload-pdf-resource/
│   ├── HU13-register-resource-metadata/
│   ├── HU14-search-resources/
│   └── HU15-download-resource/
├── forum/                                 ← EP-05 completo (HU16–HU18, HU27–HU29)
│   ├── README.md
│   ├── HU16-create-forum-thread/
│   ├── HU17-reply-to-forum-thread/
│   ├── HU18-close-forum-thread/
│   ├── HU27-react-to-forum-content/
│   ├── HU28-comment-on-answer/
│   └── HU29-follow-user/
├── moderation/                            ← EP-06 parcial (HU19 ✅ · HU20 pendiente)
│   ├── README.md
│   └── HU19-report-content/
├── verification/                          ← EP-07 pendiente (HU21, HU22)
│   └── README.md
├── billing/                               ← EP-08 pendiente (HU23, HU24)
│   └── README.md
├── notifications/                         ← EP-09 pendiente (HU25)
│   └── README.md
├── gamification/                          ← EP-10 pendiente (HU30, HU31, HU32)
│   └── README.md
└── feedback/                              ← EP-11 pendiente (HU34, HU35)
    └── README.md
```

---

## Prerrequisito: Swagger UI

La dependencia `springdoc-openapi-starter-webmvc-ui 2.8.8` ya está incluida en `pom.xml`. No requiere ninguna acción adicional.

Con el backend corriendo, las URLs disponibles son:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## Flujo de trabajo completo

### Paso 1 — Levantar el backend

```
mvn spring-boot:run
```

Docker Compose arranca PostgreSQL automáticamente mediante Spring Boot Docker Compose integration.

### Paso 2 — Importar desde Swagger a Postman

1. Abre Postman → **Import**.
2. Selecciona **Link** e ingresa: `http://localhost:8080/v3/api-docs`
3. Postman genera los requests automáticamente a partir de la definición OpenAPI.

### Paso 3 — Activar el ambiente local

1. En Postman → **Environments** → **Import**.
2. Selecciona el archivo `postman/environments/local.postman_environment.json`.
3. Activa el ambiente **MentorEdu — Local**.
4. Todos los requests usarán `{{base_url}}` y `{{api_v1}}` automáticamente.

### Paso 4 — Consultar la documentación de la Historia

Antes de ejecutar un request, abre el `README.md` de la Historia correspondiente:

```
postman/<bc>/HU<XX>-<nombre>/README.md
```

Allí encontrarás:
- el endpoint exacto y los headers requeridos,
- los campos del body y sus validaciones,
- las reglas de negocio aplicables,
- los escenarios Gherkin mapeados a cada `caso-XX.json`.

### Paso 5 — Copiar el body del escenario

Abre el archivo `caso-XX.json` correspondiente y copia el valor de `request.body`  
en la pestaña **Body → raw → JSON** de Postman.

### Paso 6 — Ejecutar y verificar

1. Ejecuta el request.
2. Verifica que el `status` coincida con el `expected_response.status` del archivo.
3. Guarda la respuesta como evidencia si la historia está siendo revisada.

---

## Nota sobre los archivos de colección

Los archivos `MentorEdu-API.postman_collection.json` y `MentorEdu_API.postman_collection.json` en la raíz de esta carpeta son colecciones **legacy** generadas en etapas tempranas del proyecto. Su estructura de carpetas no está alineada con la arquitectura actual de bounded contexts.

**No usar esas colecciones para pruebas.** La fuente de verdad son los `caso-XX.json` individuales y los `README.md` de cada HU.

Para obtener una colección Postman actualizada, importar directamente desde Swagger (Paso 2 del flujo de trabajo).

---

### Paso 7 — Nombrar los requests en Postman

Usa el patrón:

```
MentorEdu<BC>HU<XX>-<AccionMétodoHTTP>
```

Ejemplos:

| Request | Nombre |
|---|---|
| Registro | `MentorEduAuthHU01-RegistroPOST` |
| Login | `MentorEduAuthHU02-LoginPOST` |
| Seleccionar tipo de cuenta | `MentorEduProfileHU04-SelectAccountTypePATCH` |
| Crear hilo en foro | `MentorEduForumHU16-CreateThreadPOST` |

---

## Cómo agregar una nueva Historia

Cuando una US quede implementada y testeada:

1. Crea la carpeta `postman/<bc>/HU<XX>-<nombre>/`.
2. Agrega `README.md` con: endpoint, headers, body fields, reglas de negocio y escenarios Gherkin.
3. Agrega un archivo `caso-XX.json` por cada escenario de aceptación.
4. Actualiza `postman/<bc>/README.md`: mueve la HU de "Pendientes" a "Implementadas".

---

## Estado actual por bounded context

| Bounded Context | Dominio | HUs implementadas | HUs pendientes |
|---|---|---|---|
| auth | Identity & Access | HU01, HU02, HU03, HU26 | — |
| profile | Identity & Access | HU04, HU05, HU06, HU07, HU08, HU09, HU10 | — |
| academy | Academic Content | HU33, HU11 | — |
| library | Academic Content | HU12, HU13, HU14, HU15 | — |
| forum | Community | HU16, HU17, HU18, HU27, HU28, HU29 | — |
| moderation | Trust & Safety | HU19 | HU20 |
| verification | Trust & Safety | — | HU21, HU22 |
| billing | Commerce | — | HU23, HU24 |
| notifications | Engagement | — | HU25 |
| gamification | Engagement | — | HU30, HU31, HU32 |
| feedback | Feedback | — | HU34, HU35 |
