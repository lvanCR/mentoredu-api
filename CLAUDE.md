# MentorEdu API — Protocolo de Desarrollo para Claude

## Contexto del Proyecto

Backend de MentorEdu: API REST construida con **Java 21 + Spring Boot 4.0.3**, base de datos **PostgreSQL**, migraciones con **Flyway**, organizada en **Bounded Contexts** siguiendo **Clean Architecture**.

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
| config | — | `config/` | SecurityConfig, BeanConfig, OpenApiConfig |

### Paquetes Java definitivos (rama chore/restructuracion-y-mejora)

Todos los paquetes han sido migrados y están alineados con los Bounded Contexts:

| Paquete | BC | Estado |
|---|---|---|
| `com.mentoredu.auth` | auth | ✅ Alineado |
| `com.mentoredu.profile` | profile | ✅ Reestructurado (base `Profile` + subtipos por `profile_id`) |
| `com.mentoredu.academy` | academy | ✅ Creado desde cero |
| `com.mentoredu.library` | library | ✅ Migrado desde `content` (`University` → `Institution`) |
| `com.mentoredu.forum` | forum | ✅ Migrado desde `community` |
| `com.mentoredu.billing` | billing | ✅ Migrado desde `subscription` |
| `com.mentoredu.notifications` | notifications | ✅ Migrado desde `notification` |
| `com.mentoredu.gamification` | gamification | ✅ Sin cambio |
| `com.mentoredu.moderation` | moderation | ✅ Sin cambio |
| `com.mentoredu.verification` | verification | ✅ Sin cambio |

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

### ✅ REESTRUCTURACIÓN ARQUITECTURAL COMPLETADA

**V1** (`chore/reorganize-architecture`): Migración inicial, entidades, seeds, endpoints `/api/v1/`.

**V2** (`chore/restructuracion-y-mejora`): Renombrado de todos los paquetes Java a nombres definitivos de BC. Reestructuración de `profile` con tabla base `profiles`. BC `academy` creado desde cero. `University` → `Institution`. `academic_resources` limpio (sin columnas legacy, con `file_id` + `institution_id`). `premium_access` añadido al BC `billing`.

### 📋 PENDIENTES (TODO)

#### EP-01 Auth
- [ ] US01: Register account with email and password
- [ ] US02: Sign in with email and password
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
_(Mover aquí cada US terminada con fecha de completado)_

---

## Flujo de Trabajo Recomendado

Para cada User Story:

1. **Migración SQL** → `V{n}__descripcion.sql` con la tabla o alteración necesaria
2. **Entity** → Clase Java con anotaciones JPA, mapeo exacto al ER
3. **Repository** → Interface `JpaRepository` + queries custom si se necesitan
4. **Service** → Lógica de negocio, validaciones, RN-XX
5. **DTOs** → Request/Response separados de la entidad
6. **Controller** → Endpoints REST con `@Valid`
7. **Tests** → Al menos un test de integración del endpoint principal

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

## Notas Técnicas Importantes

- **Sesiones**: `sessions` maneja refresh tokens. JWT de acceso es stateless (no se guarda en BD).
- **Roles**: `STUDENT`, `TEACHER`, `ACADEMY`, `MODERATOR`, `ADMIN` (tabla `roles`).
- **Perfiles**: Tabla base `profiles` + subtipos `student_profiles`, `teacher_profiles`, `organization_profiles` enlazados por `profile_id` (PK compartida).
- **Visibilidad de recursos**: `PUBLIC` → todos, `PREMIUM` → suscripción activa o monedas, `PRIVATE` → solo el autor.
- **Reacciones**: Tabla `reactions` cubre threads, answers y comments (FKs nullable para polimorfismo). Entidad `Reaction` en el BC `forum`.
- **Puntos vs Monedas**: Sistemas independientes. `point_transactions` para puntos de reputación. `coin_wallets` para monedas canjeables (RN-35).
- **`academic_resources` ↔ `resource_files`**: Relación 1:1 con FK explícita `file_id` en `academic_resources`.
- **`notification_preferences`**: BC `profile`, tabla en el paquete `profile`, no en `notifications`.
- **`AuditLog`**: Campo `actorUserId: UUID` (FK a `users`). No usar genérico `actorType/actorId`.
- **Gamificación**: Disparada por eventos internos. No hay endpoints de escritura directa de puntos ni niveles.
- **Flujo de organización**: US04 (tipo cuenta) → US10 (perfil org) → US33 (crear academia) → US11 (oferta académica).
- **`subjects` compartido**: La tabla `subjects` (BC `library`) es usada también por `threads` (BC `forum`). Es referencia compartida aceptada entre contextos.
