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
├── auth/                                  ← EP-01: registro, login, recuperación, refresh
│   ├── README.md
│   ├── HU01-registro/                     ← 8 casos (incluye rol TEACHER, ACADEMY, inválido)
│   ├── HU02-login/
│   ├── HU03-password-recovery/
│   ├── HU26-reset-password/
│   └── HU-refresh/                        ← F0.5: POST /auth/refresh
├── profile/                               ← EP-02: perfiles
│   ├── README.md
│   ├── HU04-select-account-type/
│   ├── HU05-update-profile/
│   ├── HU06-create-student-profile/
│   └── F04-get-my-profile/                ← F0.4: GET /profiles/me
├── billing/                               ← EP-08: suscripciones, monedas
│   ├── README.md
│   └── F13-catalog/                       ← F1.3: GET /billing/plans y /coin-packages
├── academy/                               ← EP-03 (pendiente de documentar)
├── library/                               ← EP-04 (pendiente de documentar)
├── forum/                                 ← EP-05 (pendiente de documentar)
├── moderation/                            ← EP-06 (pendiente de documentar)
├── verification/                          ← EP-07 (pendiente de documentar)
├── notifications/                         ← EP-09 (pendiente de documentar)
└── gamification/                          ← EP-10 (pendiente de documentar)
```

---

## Prerrequisito: activar Swagger UI

El proyecto necesita `springdoc-openapi-starter-webmvc-ui` para exponer Swagger.  
Agregar en `pom.xml` antes de empezar a probar una nueva HU:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

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

| Bounded Context | Implementados | Pendientes de documentar |
|---|---|---|
| auth | HU01 (v2+role), HU02, HU03, HU26, F0.5 (refresh) | — |
| profile | HU04, HU05, HU06, F0.4 (GET /me) | HU07, HU08, HU09, HU10 |
| academy | — | HU33, HU11, HU36, HU37 |
| library | — | HU12, HU13, HU14, HU15 |
| forum | — | HU16, HU17, HU18, HU27, HU28, HU29 |
| moderation | — | HU19, HU20, HU38 |
| verification | — | HU21, HU22 |
| billing | HU23, HU24, F1.3 (GET /plans, GET /coin-packages) | — |
| notifications | — | HU25 |
| gamification | — | HU30, HU31, HU32 |
| feedback | — | HU34, HU35 |

> **Nota**: Las columnas muestran el estado de la *documentación Postman*. El código de todas las HUs de las épicas EP-01 a EP-11 y EP-03 está implementado. Ver `CLAUDE.md` para el estado completo del código.
