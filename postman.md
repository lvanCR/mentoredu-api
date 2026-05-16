# Postman y Swagger para MentorEdu

Guía de referencia rápida para pruebas manuales.  
La documentación detallada de cada Historia vive en la carpeta `postman/`.

---

## Objetivo

Cada Historia de Usuario implementada se convierte en un grupo de requests en Postman, uno por escenario de aceptación. El flujo parte de Swagger (OpenAPI) para evitar crear requests a mano.

Ejemplo de organización en Postman para HU01:

```
auth/
└── HU01-registro/
    ├── MentorEduAuthHU01-RegistroPOST — Caso 1: Registro exitoso      → 201 Created
    ├── MentorEduAuthHU01-RegistroPOST — Caso 2: Email duplicado       → 409 Conflict
    ├── MentorEduAuthHU01-RegistroPOST — Caso 3: Contraseña débil      → 400 Bad Request
    ├── MentorEduAuthHU01-RegistroPOST — Caso 4: Email inválido        → 400 Bad Request
    └── MentorEduAuthHU01-RegistroPOST — Caso 5: Campo faltante        → 400 Bad Request
```

---

## Prerrequisito: Springdoc OpenAPI

Para que Swagger esté disponible, la dependencia `springdoc-openapi-starter-webmvc-ui` debe estar en `pom.xml`.  
Ver instrucciones completas en `CLAUDE.md` → sección "Pruebas con Postman y Swagger".

Con el backend corriendo, accede a:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON (para importar a Postman) | `http://localhost:8080/v3/api-docs` |

---

## Flujo recomendado

### 1. Levanta el proyecto

```
mvn spring-boot:run
```

Docker Compose inicia PostgreSQL automáticamente.

### 2. Importa desde Swagger a Postman

1. Abre Postman → **Import**.
2. Selecciona **Link** e ingresa: `http://localhost:8080/v3/api-docs`
3. Postman genera los requests automáticamente a partir de la definición OpenAPI.

### 3. Configura el ambiente local

1. En Postman → **Environments** → **Import**.
2. Selecciona: `postman/environments/local.postman_environment.json`
3. Activa el ambiente **MentorEdu — Local**.

Las variables disponibles son: `{{base_url}}`, `{{api_v1}}`, `{{access_token}}`, `{{refresh_token}}`.

### 4. Consulta la documentación de la Historia

Antes de ejecutar un request, abre el `README.md` de la Historia:

```
postman/<bc>/HU<XX>-<nombre>/README.md
```

Allí encontrarás el endpoint, los headers, las validaciones del body, las reglas de negocio y los escenarios de aceptación mapeados a los archivos `caso-XX.json`.

### 5. Ejecuta cada caso por separado

Copia el contenido de `request.body` del archivo `caso-XX.json` en la pestaña **Body → raw → JSON** de Postman.  
Verifica que el `status` de la respuesta coincida con `expected_response.status`.

### 6. Nombra los requests en Postman

Usa el patrón:

```
MentorEdu<BC>HU<XX>-<AccionMétodoHTTP>
```

Ejemplos:

| Historia | Nombre del request |
|---|---|
| HU01 Registro | `MentorEduAuthHU01-RegistroPOST` |
| HU02 Login | `MentorEduAuthHU02-LoginPOST` |
| HU04 Tipo de cuenta | `MentorEduProfileHU04-SelectAccountTypePATCH` |
| HU12 Subir recurso | `MentorEduLibraryHU12-UploadResourcePOST` |
| HU16 Crear hilo | `MentorEduForumHU16-CreateThreadPOST` |
| HU26 Reset contraseña | `MentorEduAuthHU26-ResetPasswordPOST` |

---

## Organización por bounded context

No mezcles requests de distintos bounded contexts en la misma carpeta. La estructura en Postman debe reflejar la de `postman/`:

```
postman/
├── auth/          → /api/v1/auth/**
├── profile/       → /api/v1/profiles/**
├── academy/       → /api/v1/academies/**
├── library/       → /api/v1/resources/**
├── forum/         → /api/v1/threads/**
├── moderation/    → /api/v1/moderation/**
├── verification/  → /api/v1/verification/**
├── billing/       → /api/v1/billing/**
├── notifications/ → /api/v1/notifications/**
└── gamification/  → /api/v1/gamification/**
```

---

## Cómo agregar una Historia nueva

Cuando una US quede implementada:

1. Crea `postman/<bc>/HU<XX>-<nombre>/README.md` con endpoint, headers, campos, reglas de negocio y escenarios.
2. Agrega un archivo `caso-XX.json` por cada escenario de aceptación.
3. Actualiza `postman/<bc>/README.md`: mueve la HU de "Pendientes" a "Implementadas".
4. Actualiza `postman/README.md`: incrementa el contador de HUs implementadas del BC.

---

## Resultado esperado

Al terminar, en Postman deberías ver:

- carpetas separadas por bounded context,
- subcarpetas por Historia de Usuario,
- requests nombrados con el patrón establecido,
- cada request probando un escenario concreto con body y status code documentados.
