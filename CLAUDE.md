# MentorEdu API — Protocolo de Desarrollo para Claude

## Contexto del Proyecto

Backend de MentorEdu: API REST construida con **Java 21 + Spring Boot 4**, base de datos **PostgreSQL**, migraciones con **Flyway**, organizada en **Bounded Contexts** siguiendo **Clean Architecture**.

- **Package base**: `com.mentoredu`
- **Branch actual**: ver rama activa en Git
- **Documentación de referencia**:
    - `docs/historias-usuario.md` → User Stories con criterios de aceptación (Gherkin)
    - `docs/diagrama-er.txt` → Diagrama ER Físico (fuente de verdad para BD)
    - `docs/diagrama-clases.txt` → Diagrama de Clases UML (fuente de verdad para entidades Java)

---

## Estructura del Proyecto

```
com.mentoredu/
├── auth/           → users, roles, sessions, password_reset_tokens
├── profile/        → student_profiles, teacher_profiles, academy_profiles,
│                     moderator_profiles, admin_profiles, notification_preferences
├── content/        → academic_resources, resource_files, universities, subjects, tags
├── community/      → threads, answers, comments, reactions, follow_relations
├── gamification/   → coin_wallets, point_transactions, badges, user_badges, level_progress
├── moderation/     → reports, moderation_actions, audit_logs, appeals
├── verification/   → verification_requests, verification_documents
├── subscription/   → plans, subscriptions, payments, coin_packages, coin_purchases
├── notification/   → notifications
└── config/         → configuración global (seguridad, beans, etc.)
```

> **Nota**: `notification_preferences` vive en el paquete `profile` (alineado con ER y diagrama de clases),
> no en `notification`.

Migraciones Flyway en: `src/main/resources/db/migration/`
Formato obligatorio: `V{n}__{descripcion_en_snake_case}.sql`

---

## Reglas de Oro (OBLIGATORIAS)

1. **UUID siempre**: Todas las PKs son `UUID`. Nunca usar `Long` o `Integer` como identificador.
2. **Convención de nombres**: `snake_case` en base de datos, `camelCase` en Java.
3. **Contraseñas cifradas**: Usar `BCryptPasswordEncoder`. Nunca almacenar en texto plano (RN-04).
4. **Trazabilidad anónima**: Las publicaciones anónimas deben mantener `author_user_id` interno para moderación, aunque no se exponga públicamente (RN-07).
5. **Verificar ER antes de crear entidades**: Consultar `docs/diagrama-er.txt` para tipos de datos, longitudes de campo y restricciones antes de escribir cualquier entidad o migración.
6. **Estructura de capas**: `Controller → Service → Repository → Entity`. No saltar capas.
7. **Criterios de aceptación**: No dar una US por terminada sin verificar todos los escenarios Gherkin del `docs/historias-usuario.md`.
8. **Auditoría en moderación**: Toda acción de moderación debe registrar entrada en `audit_logs` (RN-12).
9. **Un rol activo por usuario**: Un usuario tiene exactamente un rol principal activo (RN-02).
10. **Restricciones de negocio**: Respetar todas las RN definidas en `docs/historias-usuario.md` (RN-01 a RN-20).

---

## Estado de Épicas

> Actualiza este bloque cada vez que completes una tarea. Mueve el ítem a COMPLETADO y crea una nueva rama en Git.

### 🔄 EN PROGRESO
- [ ] **feature/password-reset** → US20: Recuperación de contraseña (EP01)
    - Tablas involucradas: `password_reset_tokens`, `users`
    - Endpoint: `POST /api/v1/auth/forgot-password`, `POST /api/v1/auth/reset-password`
    - Lógica: generar token UUID, guardar hash, enviar email, validar expiración (1 hora), marcar `used = true`

### ✅ ALINEACIÓN ESTRUCTURAL COMPLETADA (chore/reorganize-architecture)
- [x] **Bloque A** — Migración V1 única y limpia (`db/migration/V1__initial_schema.sql`). `ddl-auto: validate`.
- [x] **Bloque B** — `User` alineado al ER: sin `age`/`points`/`coins`, con `provider`/`status`/timestamps. `passwordHash` correcto.
- [x] **Bloque C** — `Follow` → `community.FollowRelation` (tabla `follow_relations`, FKs `follower_user_id`/`followed_user_id`).
- [x] **Bloque D** — `SecurityConfig` movido a `config`. `BCryptPasswordEncoder` como `@Bean`. `JwtUtil` usa firma real (jjwt parser).
- [x] **Bloque E** — Paquete `document` → `content`. `AuthenticationProvider` registrado.
- [x] **Bloque F** — `ThreadEntity` → `Thread`, `AnswerEntity` → `Answer`. FKs corregidas a `author_user_id`.
- [x] **Bloque G** — Todos los endpoints con prefijo `/api/v1/`. Documentos en `/api/v1/resources/**`. Reports en `/api/v1/moderation/reports`.
- [x] **Bloque H** — `register()` usa `RegisterRequest` DTO. `ThreadService.listRecent()` paginado. `ReportController` devuelve `ReportResponse`.
- [x] **Bloque I** — `IGamificationService` e `IFollowService` creadas. `GamificationService` usa `CoinWallet`/`PointTransaction`.
- [x] **Bloque J** — Seed corregido: 5 roles `STUDENT`, `TEACHER`, `ACADEMY`, `MODERATOR`, `ADMIN` (no `PREMIUM`).
- [x] **Bloque K** — `Document` → `AcademicResource` (tabla `academic_resources`). V1 migration corregida. Campos ER añadidos (`resource_type`, `visibility`, `verification_status`, `exam_year`, etc.). `DocumentRepository` → `AcademicResourceRepository`.
- [x] **Bloque L** — Entidades faltantes añadidas: `PasswordResetToken`, `Session` (auth); `Comment`, `Reaction` (community); `Badge`, `UserBadge` (gamification); `ModerationAction`, `AuditLog`, `Appeal` (report).
- [x] **Bloque M** — Paquetes faltantes creados con entidades y repositorios: `profile/` (5 perfiles + NotificationPreference), `verification/`, `subscription/` (con enums SubscriptionStatus/PaymentStatus), `notification/`.
- [x] **Bloque N** — `ReportStatus` corregido: `OPEN, IN_REVIEW, RESOLVED, REJECTED`. `TargetType` ampliado: `THREAD, ANSWER, COMMENT, RESOURCE`. `download_logs.document_id` → `resource_id` FK → `academic_resources`. FK de `resource_tags.resource_id` → `academic_resources` añadida.

### 📋 PENDIENTES (TODO)

#### EP01 – Autenticación y gestión de cuenta
- [ ] US01: Registro de usuario (email + Google OAuth2)
- [ ] US02: Inicio de sesión + generación de JWT + refresh token
- [ ] US20: ~~Recuperación de contraseña~~ → EN PROGRESO

#### EP02 – Perfil y progreso personal
- [ ] US03: Edición del perfil básico
- [ ] US04: Visualización del progreso personal

#### EP03 – Repositorio de documentos
- [ ] US06: Subida de documentos con metadatos
- [ ] US07: Límites de descarga para usuarios gratuitos
- [ ] US08: Búsqueda con filtros avanzados
- [ ] US09: Visor integrado de PDF (mobile-first)

#### EP04 – Foros y colaboración
- [ ] US05: Anonimato en foros y documentos
- [ ] US10: Reporte de contenido inapropiado
- [ ] US11: Reputación de contenido (votos y ordenamiento)
- [ ] US12: Foro anclado por pregunta de examen
- [ ] US13: Subir imágenes al responder
- [ ] US14: Notificaciones de actividad

#### EP05 – Gamificación y recompensas
- [ ] US15: Sistema de puntos acumulables
- [ ] US16: Niveles que desbloquean ventajas
- [ ] US17: Monedas virtuales

#### EP06 – Monetización y modelo freemium
- [ ] US18: Modelo freemium (suscripción y compra de monedas)

#### EP07 – Interacción social
- [ ] US19: Seguir a otros usuarios

#### EP08 – Moderación y verificación
- [ ] US21: Verificar identidad (docente o academia)

### ✅ COMPLETADO (DONE)
- (Mover aquí los ítems terminados con fecha de completado)

---

## Flujo de Trabajo Recomendado

Para cada User Story, seguir este orden:

1. **Migración SQL**: Crear `V{n}__descripcion.sql` con la tabla o alteración necesaria
2. **Entity**: Clase Java con anotaciones JPA, mapeo exacto al ER
3. **Repository**: Interface `JpaRepository` + queries custom si se necesitan
4. **Service**: Lógica de negocio, validaciones, reglas de negocio (RN-XX)
5. **DTOs**: Request/Response separados de la entidad
6. **Controller**: Endpoints REST, validaciones de entrada con `@Valid`
7. **Tests**: Al menos test de integración del endpoint principal

---

## Convenciones de Endpoints

```
Base path: /api/v1/
Auth:         /api/v1/auth/**
Usuarios:     /api/v1/users/**
Recursos:     /api/v1/resources/**
Comunidad:    /api/v1/threads/**
Gamificación: /api/v1/gamification/**
Moderación:   /api/v1/moderation/**
Suscripciones:/api/v1/subscriptions/**
```

---

## Notas Técnicas Importantes

- **Sesiones**: La tabla `sessions` maneja refresh tokens. JWT de acceso es stateless (sin guardar en BD).
- **Roles disponibles**: STUDENT, TEACHER, ACADEMY, MODERATOR, ADMIN (tabla `roles`).
- **Perfiles**: Cada rol tiene su propia tabla de perfil. Un usuario puede tener perfil de alumno Y ser moderador (tablas separadas).
- **Visibilidad de recursos**: `PUBLIC` → todos, `PREMIUM` → suscripción activa o monedas, `PRIVATE` → solo el autor.
- **Reacciones**: La tabla `reactions` cubre threads, answers y comments (polimórfica con FK nullable).
- **Puntos vs Monedas**: Son sistemas separados. `point_transactions` para puntos de reputación. `coin_wallets` para monedas canjeables.