# Diagnóstico y Solución: Startup Lento + Email Bloqueante en Render

---

## PROBLEMA 1 — Startup de 146 segundos (resuelto 2026-05-23)

**Síntoma:** El API tardó **146.497 segundos** en arrancar en Render free plan.  
Render envió dos sondas "No open ports detected" (10:55:19 y 10:56:21) antes de que Tomcat terminara de iniciarse (10:56:54).

```
10:54:55 — Tomcat initialized with port 8080 (http)
10:55:19 — No open ports detected after 24s
10:56:21 — No open ports detected (second probe)
10:56:54 — Tomcat started on port(s): 8080
10:57:21 — Started MentoreduApiApplication in 146.497 seconds
```

### Causas y soluciones aplicadas

| # | Causa | Archivo | Solución |
|---|---|---|---|
| C-01 ⚠️ | Puerto fijo `8080` — Render usa `PORT=10000` | `application.yml` | `server.port: ${PORT:8080}` |
| C-02 | Eager initialization de ~80+ beans | `application.yml` | `spring.main.lazy-initialization: true` |
| C-03 | Open Session in View activo | `application.yml` | `spring.jpa.open-in-view: false` |
| C-04 | HikariCP abre 10 conexiones al arranque | `application-prod.yml` | `minimum-idle: 1`, `maximum-pool-size: 5` |
| C-05 | Classpath scan sin acotar | `MentoreduApiApplication.java` | `scanBasePackages = "com.mentoredu"` |
| C-06 | Banner ASCII en logs | `application.yml` | `spring.main.banner-mode: off` |

**Resultado:** 289/289 tests pasan. Pendiente medición en Render post-deploy.

---

## PROBLEMA 2 — Health Check congelado por SMTP de SendGrid (resuelto 2026-05-23)

### Descripción

El endpoint `/actuator/health` se congelaba varios minutos en producción. Causa raíz: Spring Boot Actuator incluye automáticamente un `MailHealthIndicator` que abre una conexión de diagnóstico a `smtp.sendgrid.net:587` durante el health check. En Render, la conexión SMTP saliente sufre alta latencia o políticas restrictivas de red → el hilo del Actuator se bloqueaba esperando la respuesta del socket SMTP.

### Corrección inmediata (ya aplicada)

```yaml
# application-prod.yml
management:
  health:
    mail:
      enabled: false
```

Elimina el `MailHealthIndicator` del health check en producción. El servicio sube instantáneamente en Render.

---

## PROBLEMA 3 — `POST /auth/forgot-password` bloqueante en producción ⚠️ CRÍTICO

### Análisis de impacto

El mismo cuello de botella de SMTP que congelaba `/actuator/health` afecta **directamente** al flujo de recuperación de contraseña en producción.

**Cadena de llamadas (antes del fix):**

```
HTTP POST /auth/forgot-password
  └─► AuthService.forgotPassword()        @Transactional
        ├─ PasswordResetToken saved to DB  ← rápido (~5 ms)
        └─ emailService.sendPasswordResetEmail()   ← BLOQUEANTE
              └─ mailSender.send(message)           ← espera SMTP: 5-30 s
```

**Consecuencias en producción:**
- El usuario llama a "Olvidé mi contraseña" → la app tarda 5-30 s en responder (o timeout)
- Render puede matar la request antes de que SMTP responda (502 Gateway Timeout)
- No hay email enviado, no hay mensaje de error al usuario, experiencia rota

**Endopoints NO afectados** (no tocan SMTP):
- `POST /auth/register` — solo guarda en BD y publica un evento interno
- `POST /auth/login` — solo BD y JWT
- Todos los demás endpoints — ninguno llama a `emailService`

### Solución: `@Async("emailExecutor")` en SendGridEmailService

**Principio:** el token se persiste en BD dentro de la transacción de `forgotPassword()`. El envío del email se despacha a un hilo secundario y la HTTP response se retorna al usuario **inmediatamente**. El email sale en background; si falla, queda registrado en logs.

**Flujo después del fix:**

```
HTTP POST /auth/forgot-password
  └─► AuthService.forgotPassword()           @Transactional
        ├─ PasswordResetToken saved to DB      ← ~5 ms
        ├─ emailService.sendPasswordResetEmail()  ← Spring proxy intercepta
        │     └─ despacha a emailExecutor pool    ← retorna inmediatamente
        └─ HTTP 200 {"message": "..."} enviado en < 50 ms

[email-1 thread — background]
  └─ mailSender.send(message)                  ← SMTP sin bloquear el usuario
       ├─ OK  → log.info "[MAIL] sent to user@..."
       └─ ERR → log.error "[MAIL] Failed to send..."
```

#### Archivos modificados

**`AsyncConfig.java`** — nuevo bean `emailExecutor`:
```java
@Bean(name = "emailExecutor")
public Executor emailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(3);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("email-");
    executor.initialize();
    return executor;
}
```

**`SendGridEmailService.java`** — método no bloqueante con manejo de errores:
```java
@Async("emailExecutor")
@Override
public void sendPasswordResetEmail(String to, String token) {
    try {
        // ... construir y enviar mensaje
        mailSender.send(message);
        log.info("[MAIL] Password reset email sent to {}", to);
    } catch (Exception e) {
        log.error("[MAIL] Failed to send password reset email to {}: {}", to, e.getMessage(), e);
    }
}
```

**`application-prod.yml`**:
```yaml
management:
  health:
    mail:
      enabled: false
```

**Por qué `@Async` funciona aquí:**
- `AuthService` inyecta `IEmailService` como bean Spring. La llamada pasa por el proxy AOP → el interceptor `@Async` lo despacha al pool.
- Funciona porque es una llamada *cross-bean*. Si fuera `this.sendPasswordResetEmail()` dentro del mismo servicio, no funcionaría.
- `@EnableAsync` ya estaba activo en `AsyncConfig`.

---

## Plan de Pruebas en Postman — URL de Producción

**Base URL:** `https://mentoredu-api.onrender.com`  
**Nota:** En Render free plan, el primer request tras inactividad (~15 min) dispara el cold start. Espera el primer response; los siguientes serán rápidos.

---

### TEST-01 — Health Check (warmup + referencia)

**Objetivo:** Confirmar que el servicio levanta y el health check no se congela.

```
GET https://mentoredu-api.onrender.com/actuator/health
Headers: (ninguno)
```

**Respuesta esperada:** `200 OK` en < 5 s (o < 60 s si es el primer request del día)
```json
{"status": "UP"}
```

**Si responde `{"status":"DOWN"}`:** revisar logs de Render — puede ser la BD PostgreSQL free que también hace cold start.

---

### TEST-02 — Login (mide latencia BD en producción)

**Objetivo:** Verificar que Login responde rápido. No hay email. Mide latencia pura de BD.

```
POST https://mentoredu-api.onrender.com/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@mentoredu.com",
  "password": "<contraseña-del-admin>"
}
```

**Respuesta esperada:** `200 OK` en < 2 s
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": { "id": "...", "email": "admin@mentoredu.com", "role": "ADMIN" }
}
```

Guarda el `accessToken` como variable de entorno `{{accessToken}}` en Postman.

---

### TEST-03 — Registro (mide latencia BD sin email)

**Objetivo:** Verificar que el registro es rápido. No envía email.

```
POST https://mentoredu-api.onrender.com/api/v1/auth/register
Content-Type: application/json

{
  "firstName": "Test",
  "lastName": "Postman",
  "email": "test.postman.{{$timestamp}}@mentoredu.test",
  "password": "Test1234!",
  "role": "STUDENT"
}
```

**Respuesta esperada:** `201 Created` en < 2 s
```json
{
  "id": "...",
  "email": "test.postman...@mentoredu.test",
  "role": "STUDENT",
  "accessToken": "eyJ..."
}
```

---

### TEST-04 — Forgot Password (mide async email — el más importante)

**Objetivo:** Confirmar que la respuesta HTTP es inmediata **aunque SMTP tarde**. Con el fix `@Async`, el endpoint debe responder en < 500 ms sin importar cuánto tarde SendGrid.

```
POST https://mentoredu-api.onrender.com/api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "admin@mentoredu.com"
}
```

**Respuesta esperada:** `200 OK` en **< 500 ms** (no espera SMTP)
```json
{
  "message": "Si existe una cuenta con ese correo, recibirás un enlace de recuperación en breve."
}
```

**Cómo verificar el envío real del email:**
1. Después de que Postman reciba el `200 OK`, espera 10-30 segundos
2. Revisa la bandeja de entrada del correo del admin
3. Revisa también los logs de Render (`Deploy Logs` → busca `[MAIL]`) — debes ver:
   - `[MAIL] Password reset email sent to admin@mentoredu.com` → SMTP OK
   - `[MAIL] Failed to send password reset email...` → SMTP falló (pero el usuario no lo vio)

---

### TEST-05 — Endpoint protegido (mide JWT + BD)

**Objetivo:** Verificar que un endpoint autenticado responde rápido.

```
GET https://mentoredu-api.onrender.com/api/v1/threads?page=0&size=5
Authorization: Bearer {{accessToken}}
```

**Respuesta esperada:** `200 OK` en < 2 s
```json
{
  "content": [...],
  "page": 0,
  "size": 5,
  "totalElements": ...,
  "totalPages": ...,
  "last": ...
}
```

---

### Tabla de criterios de éxito

| Test | Endpoint | Tiempo esperado | Criterio |
|---|---|---|---|
| TEST-01 | `GET /actuator/health` | < 5 s (warm) | `status: UP` |
| TEST-02 | `POST /auth/login` | < 2 s | `200` + `accessToken` |
| TEST-03 | `POST /auth/register` | < 2 s | `201` + `accessToken` |
| TEST-04 | `POST /auth/forgot-password` | **< 500 ms** | `200` + email llega en ~30 s |
| TEST-05 | `GET /threads` | < 2 s | `200` + `content[]` |

---

## Fallbacks documentados

### Si startup sigue > 60 s tras PROBLEMA 1

`ddl-auto: validate` hace que Hibernate consulte `information_schema` por cada entidad (~20 tablas). Con latencia de red puede sumar 10-30 s. Solución: confiar en Flyway y deshabilitar la validación en prod:
```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

### Si `@Async` no está disponible al llamar `emailService`

`@Async` requiere que `@EnableAsync` esté activo (ya está en `AsyncConfig`) y que la llamada pase por el proxy Spring (ya es cross-bean: `AuthService` → `IEmailService`). Si hay un problema de proxy, el log del servidor mostrará la llamada como síncrona. Verificar en logs de Render que la request de `forgot-password` retorna antes de que aparezca `[MAIL] Password reset email sent`.

---

## Comandos de referencia

```bash
# Health check rápido
curl -s https://mentoredu-api.onrender.com/actuator/health | python -m json.tool

# Medir tiempo de forgot-password (el más crítico)
time curl -s -X POST https://mentoredu-api.onrender.com/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mentoredu.com"}'

# Ver logs en Render Dashboard → tu servicio → Logs
```
