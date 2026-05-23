# Guía de Despliegue y Prueba — MentorEdu API

> **Stack:** Java 21 · Spring Boot 4 · PostgreSQL · Docker · Render

---

## Índice

1. [Prerrequisitos](#1-prerrequisitos)
2. [Servicios externos a configurar antes del deploy](#2-servicios-externos)
   - 2.1 Cloudinary (almacenamiento de archivos)
   - 2.2 SendGrid (email)
   - 2.3 Generar JWT_SECRET
3. [Despliegue en Render](#3-despliegue-en-render)
   - 3.1 Crear cuenta y conectar GitHub
   - 3.2 Crear la base de datos PostgreSQL
   - 3.3 Crear el Web Service
   - 3.4 Configurar variables de entorno
   - 3.5 Primer deploy y monitoreo
4. [Verificar que la API está en pie](#4-verificar-que-la-api-está-en-pie)
5. [Probar los endpoints](#5-probar-los-endpoints)
   - 5.1 Opción A — Postman (recomendada)
   - 5.2 Opción B — Swagger UI (solo en local)
   - 5.3 Flujo de prueba rápido
6. [Prueba local completa](#6-prueba-local-completa)
7. [Solución de problemas frecuentes](#7-solución-de-problemas-frecuentes)

---

## 1. Prerrequisitos

| Qué | Por qué |
|---|---|
| Cuenta en [GitHub](https://github.com) | El repo debe estar publicado para que Render lo clone |
| Cuenta en [Render](https://render.com) | Plataforma de despliegue |
| Cuenta en [Cloudinary](https://cloudinary.com) | Almacenamiento de PDFs en producción |
| Cuenta en [SendGrid](https://sendgrid.com) | Envío de emails (recuperación de contraseña) |
| Repo publicado en GitHub | El `main` branch debe estar actualizado |

---

## 2. Servicios externos

### 2.1 Cloudinary

1. Entra a [cloudinary.com](https://cloudinary.com) → **Sign Up** (plan Free es suficiente).
2. Tras el registro, en el **Dashboard** verás:
   - **Cloud name** (ej. `mi-cloud`)
   - **API Key** (ej. `123456789012345`)
   - **API Secret** (ej. `AbCdEf_123...`)
3. Construye la URL con este formato:
   ```
   cloudinary://API_KEY:API_SECRET@CLOUD_NAME
   ```
   Ejemplo: `cloudinary://123456789012345:AbCdEf_123@mi-cloud`
4. Guarda este valor — lo necesitarás como `CLOUDINARY_URL` en Render.

### 2.2 SendGrid

1. Entra a [sendgrid.com](https://sendgrid.com) → **Start For Free**.
2. Confirma tu email y completa el onboarding.
3. En el menú lateral: **Settings → API Keys → Create API Key**.
4. Dale un nombre (ej. `mentoredu-prod`), selecciona **Full Access** → **Create & View**.
5. Copia la clave que empieza con `SG.` — solo se muestra una vez.
6. Guarda este valor — lo necesitarás como `SENDGRID_API_KEY`.

> **Nota:** SendGrid requiere verificar el dominio del remitente para envíos en producción. En la cuenta gratuita puedes usar **Single Sender Verification**: Settings → Sender Authentication → Verify a Single Sender. Usa el mismo email que pondrás en `MAIL_FROM_ADDRESS`.

### 2.3 JWT_SECRET

Genera una clave aleatoria segura (mínimo 32 caracteres). Puedes hacerlo de varias formas:

**En Windows PowerShell:**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

**En Linux/Mac:**
```bash
openssl rand -base64 32
```

**Alternativa online:** [generate.plus/en/base64](https://generate.plus/en/base64) (elige 32 bytes).

Guarda el resultado — lo usarás como `JWT_SECRET`.

---

## 3. Despliegue en Render

### 3.1 Crear cuenta y conectar GitHub

1. Ve a [render.com](https://render.com) → **Get Started for Free**.
2. Haz clic en **Sign up with GitHub** e inicia sesión con tu cuenta de GitHub.
3. Render pedirá permisos para acceder a tus repos → acepta.
4. Una vez en el **Dashboard** de Render, ya estás listo.

### 3.2 Crear la base de datos PostgreSQL

1. En el Dashboard → clic en **New +** → **PostgreSQL**.
2. Rellena el formulario:
   - **Name:** `mentoredu-db`
   - **Database:** `mentoredu`
   - **User:** `mentoredu`
   - **Region:** elige la más cercana (ej. Ohio o Frankfurt)
   - **Plan:** Free (suficiente para desarrollo/demo; expira a los 90 días en el plan gratuito)
3. Clic en **Create Database**.
4. Render tardará ~1 minuto en aprovisionar la base de datos.
5. Una vez lista, ve a la página de la BD y anota:
   - **Internal Database URL** → este será tu `DB_URL` (formato: `postgres://user:password@host/db`)
   - **Username** → `DB_USERNAME`
   - **Password** → `DB_PASSWORD`

> **Importante:** Usa la **Internal Database URL** (no la External) para que la comunicación sea dentro de la red de Render y sea gratuita en ancho de banda.

> La URL interna de Render usa el prefijo `postgres://` pero Spring Boot necesita `jdbc:postgresql://`. Convierte así:
> - Render te da: `postgres://mentoredu:abc123@dpg-xxx/mentoredu`
> - Tú escribes: `jdbc:postgresql://dpg-xxx/mentoredu`
> Y pones `DB_USERNAME=mentoredu` y `DB_PASSWORD=abc123` por separado.

### 3.3 Crear el Web Service

1. En el Dashboard → **New +** → **Web Service**.
2. Conecta tu repositorio de GitHub:
   - Verás la lista de tus repos → busca `mentoredu-api` → **Connect**.
3. Configura el servicio:
   - **Name:** `mentoredu-api`
   - **Region:** la misma que la base de datos
   - **Branch:** `main`
   - **Runtime:** `Docker` (Render lo detecta automáticamente por el `Dockerfile`)
   - **Plan:** Free (0 USD, se duerme tras 15 min de inactividad)
4. No hagas clic en **Create Web Service** todavía — primero configura las variables de entorno.

### 3.4 Configurar variables de entorno

En la sección **Environment Variables** del formulario de creación (o en **Settings → Environment** si ya creaste el servicio):

| Variable | Valor | Notas |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Ya definido en `render.yaml` |
| `DB_URL` | `jdbc:postgresql://dpg-xxx.render.com/mentoredu` | URL interna de tu BD (sin `postgres://`) |
| `DB_USERNAME` | `mentoredu` | Del dashboard de la BD |
| `DB_PASSWORD` | `abc123xyz...` | Del dashboard de la BD |
| `JWT_SECRET` | `<clave generada en paso 2.3>` | Mínimo 32 chars |
| `CLOUDINARY_URL` | `cloudinary://key:secret@cloud` | Del paso 2.1 |
| `SENDGRID_API_KEY` | `SG.xxxx...` | Del paso 2.2 |
| `MAIL_FROM_ADDRESS` | `noreply@tudominio.com` | Email verificado en SendGrid |
| `FRONTEND_BASE_URL` | `https://tu-frontend.onrender.com` | URL del frontend Angular (o `http://localhost:4200` si aún no está desplegado) |

> **Seguridad:** Render guarda estas variables cifradas. Nunca las pongas en el código.

### 3.5 Primer deploy y monitoreo

1. Haz clic en **Create Web Service**.
2. Render comenzará el build automáticamente. En la pestaña **Logs** verás:
   ```
   ==> Building with Dockerfile...
   ==> Running mvn dependency:go-offline...
   ==> Running mvn clean package -DskipTests...
   ==> Build successful
   ==> Starting service...
   ```
3. El primer build tarda entre **5 y 10 minutos** (descarga dependencias Maven).
4. Cuando la app arranca, Spring Boot ejecuta las **migraciones Flyway** (V1–V9). Verás en los logs:
   ```
   Flyway: Migrating schema to version 1
   ...
   Flyway: Successfully applied 9 migrations
   ```
5. La app está lista cuando los logs muestren:
   ```
   Started MentoreduApiApplication in X.XXX seconds
   ```
6. El estado en el Dashboard cambia de `In Progress` → **`Live`** (punto verde).

---

## 4. Verificar que la API está en pie

Una vez que el estado sea `Live`, verifica el health check:

```
GET https://mentoredu-api.onrender.com/actuator/health
```

Respuesta esperada:
```json
{ "status": "UP" }
```

Puedes hacerlo desde:
- El navegador directamente
- Postman
- PowerShell: `Invoke-WebRequest https://mentoredu-api.onrender.com/actuator/health`

> **Plan gratuito de Render:** El servicio se "duerme" tras 15 minutos sin tráfico. El primer request tras el sueño tarda ~30 segundos en despertar. Esto es normal en el plan Free.

---

## 5. Probar los endpoints

### 5.1 Opción A — Postman (recomendada para producción)

El Swagger UI está **deshabilitado en producción** (por seguridad). La forma recomendada de probar en Render es con Postman importando el contrato OpenAPI.

**Paso 1 — Importar el contrato OpenAPI:**
1. Abre Postman.
2. Clic en **Import** → **File**.
3. Selecciona `docs/openapi.json` del repositorio.
4. Postman genera automáticamente una colección con todos los endpoints.

**Paso 2 — Configurar el entorno:**
1. En Postman: **Environments** → **+** → nombre: `Render Prod`.
2. Añade las variables:
   | Variable | Initial value |
   |---|---|
   | `base_url` | `https://mentoredu-api.onrender.com` |
   | `api_v1` | `api/v1` |
   | `accessToken` | *(se rellena automáticamente tras login)* |
   | `refreshToken` | *(se rellena automáticamente tras login)* |
3. Guarda el entorno y selecciónalo.

También puedes usar la colección existente en `postman/environments/local.postman_environment.json` como base y cambiar `base_url` a la URL de Render.

**Paso 3 — Usar los endpoints predefinidos:**

El directorio `postman/` ya tiene casos de prueba organizados por Historia de Usuario:
- `auth/HU01-registro/` — registro de usuarios
- `auth/HU02-login/` — login y logout
- `auth/HU03-password-recovery/` — recuperación de contraseña
- `profile/` — edición de perfiles
- `library/` — recursos académicos (upload, búsqueda, descarga)
- `forum/` — hilos, respuestas, reacciones
- etc.

### 5.2 Opción B — Swagger UI (solo en local)

Si corres la app localmente con perfil `local`, el Swagger UI está disponible en:
```
http://localhost:8080/swagger-ui.html
```

Desde ahí puedes explorar todos los endpoints, ver los schemas de request/response, y ejecutar llamadas directamente en el navegador.

### 5.3 Flujo de prueba rápido

Para verificar que los endpoints básicos funcionan, este es el flujo mínimo:

**1. Registrar un usuario:**
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Password123!",
  "role": "STUDENT"
}
```
Respuesta esperada: `201 Created` con `accessToken` y `refreshToken`.

**2. Guardar el token** en la variable de entorno `accessToken` de Postman.

**3. Iniciar sesión:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Password123!"
}
```
Respuesta esperada: `200 OK` con nuevos tokens.

**4. Verificar un endpoint protegido:**
```http
GET /api/v1/catalog/universities
Authorization: Bearer {{accessToken}}
```
Respuesta esperada: `200 OK` con la lista de 7 universidades Lima (seed V9).

---

## 6. Prueba local completa

Para desarrollo y pruebas con Swagger UI, trabaja en local:

### Requisitos previos
- Java 21 instalado
- Maven 3.9+ instalado
- Docker Desktop corriendo

### Pasos

**1. Copia el archivo de entorno:**
```bash
cp .env.example .env
```

**2. Rellena `.env`** con tus valores. Los mínimos para local:
```dotenv
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5433/mentoredu
DB_USERNAME=mentoredu
DB_PASSWORD=mentoredu
JWT_SECRET=cualquier-cadena-de-al-menos-32-caracteres-aqui
```
> Los demás tienen defaults seguros.

**3. Levanta la base de datos con Docker:**
```bash
docker compose up -d
```
Esto inicia PostgreSQL (puerto 5433) y pgAdmin (puerto 8082).

**4. Arranca la app:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Spring Boot aplica automáticamente las migraciones Flyway y el seed de datos (V9).

**5. Verifica que arrancó:**
```
GET http://localhost:8080/actuator/health
→ { "status": "UP" }
```

**6. Abre Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

Desde Swagger UI puedes:
- Ver todos los endpoints agrupados por controlador
- Ver los schemas de request y response de cada endpoint
- Ejecutar llamadas directamente (usa **Authorize** con el Bearer token del login)

**7. pgAdmin (gestor visual de la BD):**
```
http://localhost:8082
Email: admin@mentoredu.com
Password: admin
```
Servidor a conectar: host `mentoredu_db`, puerto `5432`, BD `mentoredu`, usuario `mentoredu`, contraseña `mentoredu`.

---

## 7. Solución de problemas frecuentes

### La app no arranca en Render / logs muestran error de BD

- Verifica que `DB_URL` empieza con `jdbc:postgresql://` (no con `postgres://`).
- Verifica que la BD y el Web Service están en la **misma región** en Render.
- Confirma que `DB_USERNAME` y `DB_PASSWORD` coinciden exactamente con los del dashboard de la BD.

### Error `JWT signature does not match` o `IllegalArgumentException: The specified key byte array is X bits...`

- El `JWT_SECRET` debe tener al menos 32 caracteres (256 bits para HS256).
- Genera uno nuevo con `openssl rand -base64 32`.

### Flyway falla al arrancar (`Migration checksum mismatch`)

- Indica que alguien modificó un archivo de migración ya aplicado.
- **En Render:** ve al dashboard de la BD → **Reset Database** (solo en dev/staging, nunca en prod con datos reales). Luego redeploya.
- **En local:** `docker compose down -v && docker compose up -d` y vuelve a arrancar la app.

### Upload de PDF falla en producción

- Verifica que `CLOUDINARY_URL` tiene el formato exacto: `cloudinary://API_KEY:API_SECRET@CLOUD_NAME`.
- Asegúrate de que el Cloud Name no tiene espacios ni caracteres especiales.

### Emails no llegan

- Verifica que el remitente (`MAIL_FROM_ADDRESS`) está verificado en SendGrid como Single Sender.
- En local, los emails no se envían: el link de recuperación se loguea en la consola de Spring Boot.

### El servicio de Render está dormido (primer request tarda 30s)

- Normal en el plan Free.
- Solución: implementa un ping periódico (cron job externo cada 14 minutos) o actualiza al plan Starter (7 USD/mes).

---

## Resumen de URLs en producción

| Servicio | URL |
|---|---|
| API base | `https://mentoredu-api.onrender.com/api/v1/` |
| Health check | `https://mentoredu-api.onrender.com/actuator/health` |
| Swagger UI | ❌ Deshabilitado en prod (usar Postman) |
| OpenAPI JSON | ❌ Deshabilitado en prod (usar `docs/openapi.json` del repo) |

## Resumen de URLs en local

| Servicio | URL |
|---|---|
| API base | `http://localhost:8080/api/v1/` |
| Health check | `http://localhost:8080/actuator/health` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| pgAdmin | `http://localhost:8082` |
