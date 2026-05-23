# Diagnóstico y Solución: Startup Lento en Render (146 s → objetivo < 30 s)

## Descripción del Problema

**Síntoma:** El API tardó **146.497 segundos** en arrancar en Render free plan.  
Render envió dos sondas "No open ports detected" (10:55:19 y 10:56:21) antes de que Tomcat terminara de iniciarse (10:56:54).  
El health check en `/actuator/health` devolvía `{"status":"DOWN"}` o directamente fallaba con timeout.

**Línea crítica del log:**
```
10:54:55 — Tomcat initialized with port 8080 (http)
10:55:19 — No open ports detected after 24s
10:56:21 — No open ports detected (second probe)
10:56:54 — Tomcat started on port(s): 8080
10:57:21 — Started MentoreduApiApplication in 146.497 seconds
```

**Render free plan:** servicio duerme tras 15 min de inactividad. Cada cold start paga el costo completo de inicialización.

---

## Causas Identificadas y Soluciones Aplicadas

### CAUSA-01 — Puerto fijo `8080` vs. variable `PORT` de Render ⚠️ CRÍTICO

Render asigna el puerto interno mediante la variable de entorno `PORT` (valor: `10000`).  
Con `server.port: 8080` hardcodeado, el proceso escucha en 8080 pero Render enruta tráfico al 10000 → "No open ports detected".

**Solución aplicada en `application.yml`:**
```yaml
server:
  port: ${PORT:8080}
```
Fallback `8080` para entorno local.

---

### CAUSA-02 — Inicialización eager de todos los beans

Con `lazy-initialization: false` (default), Spring instancia **todos** los beans al arranque: servicios, repositorios, filtros, listeners de eventos, configuraciones de Cloudinary, mail, Flyway, Hibernate, etc.  
Con 28 historias de usuario y ~80+ beans, esto domina el startup time.

**Solución aplicada en `application.yml`:**
```yaml
spring:
  main:
    lazy-initialization: true
```
Los beans se crean en la primera petición que los necesite. El health check `/actuator/health` es muy ligero y activa solo los beans de actuator.

---

### CAUSA-03 — Open Session in View activo

`spring.jpa.open-in-view=true` (default) mantiene una conexión de BD abierta durante **todo** el ciclo de vida de la petición HTTP.  
Hibernate lanza una advertencia en startup y reserva una conexión del pool para cada hilo activo.

**Solución aplicada en `application.yml`:**
```yaml
spring:
  jpa:
    open-in-view: false
```

---

### CAUSA-04 — HikariCP abre 10 conexiones al arranque

Los defaults de Hikari: `minimum-idle=10`, `maximum-pool-size=10`.  
En Render, la BD PostgreSQL gestionada tiene latencia de red (~5-15 ms por handshake). Abrir 10 conexiones al inicio suma ~150-500 ms de overhead solo por el pool.

**Solución aplicada en `application-prod.yml`:**
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 1
      maximum-pool-size: 5
      connection-timeout: 20000
      initialization-fail-timeout: 60000
```
`initialization-fail-timeout: 60000` evita que el app falle si la BD tarda en responder (cold start de Render PostgreSQL free).

---

### CAUSA-05 — Escaneo de classpath sin acotar

`@SpringBootApplication` sin `scanBasePackages` escanea todo el classpath, incluyendo JARs de dependencias.  
Acotar el escaneo a `com.mentoredu` elimina el ruido.

**Solución aplicada en `MentoreduApiApplication.java`:**
```java
@SpringBootApplication(scanBasePackages = "com.mentoredu")
```

---

### CAUSA-06 — Banner de Spring Boot activo (minor)

El banner ASCII de Spring Boot escribe al stdout al inicio. Impacto mínimo, pero elimina ruido en los logs de Render.

**Solución aplicada en `application.yml`:**
```yaml
spring:
  main:
    banner-mode: off
```

---

## Estado de `/actuator/health` — Autenticación

`/actuator/health` ya estaba en la lista `permitAll()` de `SecurityConfig` (línea 51).  
**No se requirió ningún cambio en seguridad.**

---

## Verificación por Causa

| Causa | Verificación |
|---|---|
| CAUSA-01 (PORT) | `curl https://mentoredu-api.onrender.com/actuator/health` responde (no timeout) |
| CAUSA-02 (lazy) | Log de Render: startup time < 30 s |
| CAUSA-03 (OSIV) | Log sin `HibernateJpaDialect - WARN open-in-view` |
| CAUSA-04 (Hikari) | Log: `HikariPool-1 - Added connection...` aparece 1 vez, no 10 |
| CAUSA-05 (scan) | Log: tiempo de `ClassPathBeanDefinitionScanner` reducido |
| CAUSA-06 (banner) | Log de Render sin el ASCII art de Spring |

---

## Plan de Pruebas

### Intento 1 — Todas las optimizaciones juntas (2026-05-23)

**Cambios:**
- `server.port: ${PORT:8080}` ← CRÍTICO
- `spring.main.lazy-initialization: true`
- `spring.main.banner-mode: off`
- `spring.jpa.open-in-view: false`
- Hikari: `minimum-idle=1`, `maximum-pool-size=5`
- `@SpringBootApplication(scanBasePackages = "com.mentoredu")`

**Criterio de éxito:** `curl https://mentoredu-api.onrender.com/actuator/health` → `{"status":"UP"}` en < 30 s desde cold start.

**Resultado:** _pendiente tras deploy_

---

### Intento 2 — Si Intento 1 falla por lazy-init + Flyway

Flyway valida el schema al arranque. Con lazy-init, Flyway puede fallar si el `DataSource` bean no está inicializado cuando Flyway lo necesita.

**Fallback:** Deshabilitar lazy-init solo para beans de Flyway:
```yaml
spring:
  main:
    lazy-initialization: true
  flyway:
    enabled: true
```
Spring Boot garantiza que Flyway se ejecuta antes de que la aplicación sea `READY`, independientemente de lazy-init. No debería ser problema, pero documentado por si acaso.

---

### Intento 3 — Si startup sigue > 60 s

Investigar Hibernate schema validation (`ddl-auto: validate`). Hibernate consulta `information_schema` para cada entidad al arranque. Con ~20 tablas y latencia de red, puede sumar 10-30 s.

**Opción:** Deshabilitar validate en prod (confiar en Flyway):
```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

---

## Comandos Útiles

```bash
# Ver logs en tiempo real en Render
# (desde el dashboard: Deploy Logs)

# Verificar health check manualmente
curl -s https://mentoredu-api.onrender.com/actuator/health | jq

# Medir tiempo de cold start
time curl -s https://mentoredu-api.onrender.com/actuator/health

# Verificar que el puerto es correcto localmente
mvn spring-boot:run
curl http://localhost:8080/actuator/health
```
