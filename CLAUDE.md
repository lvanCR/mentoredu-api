# MentorEdu API — Protocolo de Desarrollo para Claude

## Contexto del Proyecto

Backend de MentorEdu: API REST construida con **Java 21 + Spring Boot 4.0.6**, base de datos **PostgreSQL**, migraciones con **Flyway**, organizada en **Bounded Contexts** siguiendo **Clean Architecture**.

- **Package base**: `com.mentoredu`
- **Branch actual**: ver rama activa en Git
- **Documentación de referencia**:
    - `docs/historias-usuario.md` → User Stories con criterios de aceptación (Gherkin)
    - `docs/diagrama-er.puml` → Diagrama ER Físico (fuente de verdad para BD)
    - `docs/diagrama-clases.puml` → Diagrama de Clases UML (fuente de verdad para entidades Java)

---

## Estructura del Proyecto

### Bounded Contexts definitivos

| Bounded Context | Epic | Paquete objetivo | Tablas principales |
|---|---|---|---|
| auth | EP-01 | `auth/` | users, roles, sessions, password_reset_tokens |
| profile | EP-02 | `profile/` | profiles, student_profiles, teacher_profiles, organization_profiles, notification_preferences |
| academy | EP-03 | `academy/` | academies, campuses, programs, cycles, teacher_academies |
| library | EP-04 | `library/` | academic_resources, resource_files, institutions, subjects, tags, resource_tags, download_logs |
| forum | EP-05 | `forum/` | threads, answers, comments, reactions, follow_relations |
| moderation | EP-06 | `moderation/` | reports, moderation_actions, audit_logs, appeals |
| verification | EP-07 | `verification/` | verification_requests, verification_documents |
| billing | EP-08 | `billing/` | plans, subscriptions, payments, coin_packages, coin_purchases, premium_access |
| notifications | EP-09 | `notifications/` | notifications |
| gamification | EP-10 | `gamification/` | coin_wallets, point_transactions, badges, user_badges, level_progress |
| config | — | `config/` | SecurityConfig, BeanConfig, GlobalExceptionHandler |

### Paquetes Java en `develop` / `feat/HU01-registro`

| Paquete | BC | Estado |
|---|---|---|
| `com.mentoredu.auth` | auth | ✅ Implementado (US01) |
| `com.mentoredu.config` | — | ✅ SecurityConfig, BeanConfig, GlobalExceptionHandler |
| `com.mentoredu.profile` | profile | ⏳ Pendiente |
| `com.mentoredu.academy` | academy | ⏳ Pendiente |
| `com.mentoredu.library` | library | ⏳ Pendiente |
| `com.mentoredu.forum` | forum | ⏳ Pendiente |
| `com.mentoredu.billing` | billing | ⏳ Pendiente |
| `com.mentoredu.notifications` | notifications | ⏳ Pendiente |
| `com.mentoredu.gamification` | gamification | ⏳ Pendiente |
| `com.mentoredu.moderation` | moderation | ⏳ Pendiente |
| `com.mentoredu.verification` | verification | ⏳ Pendiente |

Migraciones Flyway: `src/main/resources/db/migration/`
Formato: `V{n}__{descripcion_en_snake_case}.sql`

---

## Reglas de Oro (OBLIGATORIAS)

1. **UUID siempre**: Todas las PKs son `UUID`. Nunca usar `Long` o `Integer` como identificador.
2. **Convención de nombres**: `snake_case` en BD, `camelCase` en Java.
3. **Contraseñas cifradas**: `BCryptPasswordEncoder`. Nunca texto plano (RN-03).
4. **Trazabilidad anónima**: Las publicaciones anónimas mantienen `author_user_id` interno para moderación (RN-16).
5. **ER como fuente de verdad**: Consultar `docs/diagrama-er.puml` antes de crear cualquier entidad o migración.
6. **Estructura de capas**: `Controller → Service → Repository → Entity`. No saltar capas.
7. **Criterios de aceptación**: No dar una US por terminada sin verificar todos los escenarios Gherkin de `docs/historias-usuario.md`.
8. **Auditoría en moderación**: Toda acción de moderación registra entrada en `audit_logs` (RN-22).
9. **Un rol activo por usuario**: Exactamente un rol principal activo por usuario (RN-02).
10. **Restricciones de negocio**: Respetar RN-01 a RN-35 definidas en `docs/historias-usuario.md`.
11. **Gamificación inmutable**: `point_transactions` es de solo lectura. No modificar ni eliminar registros (RN-34).
12. **`notification_preferences` en `profile`**: Este entity pertenece al BC `profile`, no a `notifications`.
13. **`AuditLog.actorUserId`**: El campo es `actorUserId: UUID` (FK a `users`). No usar `actorType/actorId` genérico.

---

## Estado de Épicas

> Actualiza este bloque cada vez que completes una US. Mueve el ítem a COMPLETADO con la fecha.

### 📋 PENDIENTES (TODO)

#### EP-01 Auth
- [ ] US03: Request password recovery
- [ ] US26: Reset password with token

#### EP-02 Profile
- [ ] US04: Select account type
- [ ] US05: Update common profile data
- [ ] US06: Create student profile
- [ ] US07: Update student target university
- [ ] US08: Create teacher profile
- [ ] US09: Update teacher specialty
- [ ] US10: Create organization profile

#### EP-03 Academy
- [ ] US33: Create academy
- [ ] US11: Register academic offering

#### EP-04 Library
- [ ] US12: Upload academic PDF resource
- [ ] US13: Register resource metadata
- [ ] US14: Search resources by filters
- [ ] US15: Download academic resource

#### EP-05 Forum
- [ ] US16: Create forum thread
- [ ] US17: Reply to forum thread
- [ ] US18: Close forum thread
- [ ] US27: React to forum content
- [ ] US28: Comment on forum answer
- [ ] US29: Follow a user

#### EP-06 Moderation
- [ ] US19: Report content
- [ ] US20: Resolve report

#### EP-07 Verification
- [ ] US21: Request teacher verification
- [ ] US22: Request organization verification

#### EP-08 Billing
- [ ] US23: Activate premium subscription
- [ ] US24: Buy coin package

#### EP-09 Notifications
- [ ] US25: View pending notifications

#### EP-10 Gamification
- [ ] US30: Earn experience points
- [ ] US31: View personal level and progress
- [ ] US32: Earn and view badges

### ✅ COMPLETADO (DONE)

| US | Descripción | Fecha | Rama |
|---|---|---|---|
| US01 | Register account with email and password | 2026-05-15 | feat/HU01-registro |
| US02 | Sign in with email and password | 2026-05-16 | feat/HU02-login |

---

## Flujo de Trabajo Recomendado

Para cada User Story:

1. **Migración SQL** → `V{n}__descripcion.sql` con la tabla o alteración necesaria
2. **Entity** → Clase Java con anotaciones JPA, mapeo exacto al ER
3. **Repository** → Interface `JpaRepository` + queries custom si se necesitan
4. **Service** → Lógica de negocio, validaciones, RN-XX
5. **DTOs** → Request/Response separados de la entidad
6. **Controller** → Endpoints REST con `@Valid`
7. **Tests** → Al menos un test `@WebMvcTest` por endpoint
8. **Documentación Postman** → crear `postman/<bc>/HU<XX>-<nombre>/README.md` y archivos `caso-XX.json` para cada escenario de aceptación. Actualizar `postman/<bc>/README.md` moviendo la HU de "Pendientes" a "Implementadas".

---

## Convenciones de Endpoints

```
Base path:      /api/v1/
Auth:           /api/v1/auth/**
Perfil:         /api/v1/profiles/**
Academy:        /api/v1/academies/**
Recursos:       /api/v1/resources/**
Foro:           /api/v1/threads/**
Moderación:     /api/v1/moderation/**
Verificación:   /api/v1/verification/**
Billing:        /api/v1/billing/**
Notificaciones: /api/v1/notifications/**
Gamificación:   /api/v1/gamification/**
```

---

## Pruebas con Postman y Swagger

### Carpeta `postman/`

La carpeta `postman/` es el workspace de pruebas del proyecto. Está organizada por bounded context y contiene una subcarpeta por cada Historia de Usuario implementada.

```
postman/
├── README.md                              ← guía completa del flujo de pruebas
├── environments/
│   └── local.postman_environment.json     ← variables {{base_url}}, {{api_v1}}, {{access_token}}
├── <bc>/
│   ├── README.md                          ← estado de HUs del bounded context
│   └── HU<XX>-<nombre>/
│       ├── README.md                      ← endpoint, headers, campos, reglas de negocio, escenarios
│       ├── caso-01-exitoso.json
│       └── caso-XX-<nombre>.json          ← un archivo por escenario de aceptación
└── profile/ academy/ library/ ...        ← carpetas placeholder con .gitkeep
```

### Prerrequisito: Springdoc OpenAPI

Para activar Swagger UI, agregar en `pom.xml` antes de documentar una nueva HU:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

Con el backend corriendo (`mvn spring-boot:run`), las URLs disponibles son:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

### Flujo de pruebas para cada Historia

1. Levanta el backend: `mvn spring-boot:run`
2. Importa en Postman desde **Import → Link**: `http://localhost:8080/v3/api-docs`
3. Activa el ambiente **MentorEdu — Local** (`postman/environments/local.postman_environment.json`)
4. Abre `postman/<bc>/HU<XX>-<nombre>/README.md` para ver los escenarios y campos
5. Copia el `request.body` del archivo `caso-XX.json` en Postman → Body → raw → JSON
6. Ejecuta y verifica que el `status` coincida con `expected_response.status`

Ver `postman/README.md` para la guía detallada y `postman.md` para el flujo general.

### Nombre de requests en Postman

```
MentorEdu<BC>HU<XX>-<AccionMétodoHTTP>
```

Ejemplos: `MentorEduAuthHU01-RegistroPOST`, `MentorEduAuthHU02-LoginPOST`, `MentorEduProfileHU04-SelectAccountTypePATCH`

---

## Notas Técnicas Importantes

- **Sesiones**: `sessions` maneja refresh tokens. JWT de acceso es stateless (no se guarda en BD).
- **Roles**: `STUDENT`, `TEACHER`, `ACADEMY`, `MODERATOR`, `ADMIN` (tabla `roles`, seeded en V2).
- **Rol por defecto en registro**: `STUDENT`. US04 permite cambiar el tipo de cuenta.
- **Perfiles**: Tabla base `profiles` + subtipos `student_profiles`, `teacher_profiles`, `organization_profiles` enlazados por `profile_id` (PK compartida).
- **Visibilidad de recursos**: `PUBLIC` → todos, `PREMIUM` → suscripción activa o monedas, `PRIVATE` → solo el autor.
- **Reacciones**: Tabla `reactions` cubre threads, answers y comments (FKs nullable para polimorfismo). Entidad `Reaction` en el BC `forum`.
- **Puntos vs Monedas**: Sistemas independientes. `point_transactions` para puntos de reputación. `coin_wallets` para monedas canjeables (RN-35).
- **`academic_resources` ↔ `resource_files`**: Relación 1:1 con FK explícita `file_id` en `academic_resources` (UNIQUE constraint).
- **`notification_preferences`**: BC `profile`, tabla en el paquete `profile`, no en `notifications`.
- **`AuditLog`**: Campo `actorUserId: UUID` (FK a `users`). No usar genérico `actorType/actorId`.
- **Gamificación**: Disparada por eventos internos. No hay endpoints de escritura directa de puntos ni niveles.
- **Flujo de organización**: US04 (tipo cuenta) → US10 (perfil org) → US33 (crear academia) → US11 (oferta académica).
- **`subjects` compartido**: Tabla `subjects` (BC `library`) usada por `threads` (BC `forum`). En `Thread.java` usar `subjectId: UUID` (FK simple, no `@ManyToOne`) para evitar dependencia cruzada.
- **Tests — Spring Boot 4.x**: Los paquetes de test cambiaron respecto a SB 3.x:
  - `@WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
  - `SecurityAutoConfiguration` → `org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration`
  - `SecurityFilterAutoConfiguration` → `org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration`
  - Dependencia necesaria: `spring-boot-webmvc-test` (artefacto separado, no starter)
  - `ObjectMapper` no se auto-registra en `@WebMvcTest` — instanciar directamente: `new ObjectMapper()`
  - `MentoreduApiApplicationTests` usa H2 en memoria (perfil de test en `src/test/resources/application.yml`)
- **Docker local**: `compose.yml` arranca PostgreSQL (`mentoredu`/`mentoredu`/`mentoredu`) y pgAdmin en puerto 8082. Spring Boot Docker Compose integration auto-inicia el contenedor al hacer `mvn spring-boot:run`.

---

## Migraciones aplicadas

| Versión | Archivo | Descripción |
|---|---|---|
| V1 | `V1__initial_schema.sql` | Schema completo de todos los bounded contexts (fuente: diagrama-er.puml) |
| V2 | `V2__seed_roles.sql` | Datos semilla: roles STUDENT, TEACHER, ACADEMY, MODERATOR, ADMIN |

## Dependencias clave (pom.xml)

| Dependencia | Propósito |
|---|---|
| `spring-boot-starter-web` | MVC + Jackson + Tomcat embebido |
| `spring-boot-starter-validation` | `@Valid`, `@NotBlank`, `@Email`, `@Pattern` |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-data-jpa` | JPA + Hibernate |
| `flyway-core` + `flyway-database-postgresql` | Migraciones SQL (Flyway 10.x split) |
| `jjwt-api/impl/jackson 0.12.6` | JWT para US02 (login) |
| `spring-boot-webmvc-test` | `@WebMvcTest` en Spring Boot 4.x |
| `spring-security-test` | `@WithMockUser` y utils de seguridad en tests |
| `h2` (test) | BD en memoria para `MentoreduApiApplicationTests` |
| `springdoc-openapi-starter-webmvc-ui 2.8.8` | Swagger UI + export OpenAPI para Postman (agregar al implementar cada HU) |
