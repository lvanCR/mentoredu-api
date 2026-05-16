# HU01 — Registro de cuenta con correo y contraseña

**Epic**: EP-01 Auth  
**Bounded Context**: `auth`  
**Estado**: Implementada — 2026-05-15  
**Rama**: `feat/HU01-registro`  
**Nombre en Postman**: `MentorEduAuthHU01-RegistroPOST`

---

## Endpoint

```
POST /api/v1/auth/register
```

No requiere autenticación.

---

## Headers

| Header | Valor |
|---|---|
| `Content-Type` | `application/json` |

---

## Body — Campos requeridos

| Campo | Tipo | Validación |
|---|---|---|
| `firstName` | String | No puede estar vacío (`@NotBlank`) |
| `lastName` | String | No puede estar vacío (`@NotBlank`) |
| `email` | String | Formato válido de correo (`@Email`), único en el sistema (RN-01) |
| `password` | String | Mín 8 / máx 72 chars · al menos 1 mayúscula · 1 minúscula · 1 dígito |

### Política de contraseña (RN-03)

```
Mínimo 8 caracteres, máximo 72
Al menos 1 letra mayúscula  (A–Z)
Al menos 1 letra minúscula  (a–z)
Al menos 1 dígito           (0–9)
```

| Contraseña | ¿Válida? | Motivo |
|---|---|---|
| `Segura2024` | Sí | Cumple todos los criterios |
| `sinmayuscula1` | No | Sin mayúscula |
| `SINMINUSCULA1` | No | Sin minúscula |
| `SinDigito` | No | Sin dígito |
| `Corta1` | No | Menos de 8 caracteres |

---

## Reglas de negocio aplicables

| Código | Regla |
|---|---|
| RN-01 | No puede existir más de una cuenta con el mismo correo. |
| RN-02 | Cada usuario debe tener exactamente un rol activo. El rol asignado por defecto es `STUDENT`. |
| RN-03 | La contraseña debe almacenarse cifrada con BCrypt. Nunca en texto plano. |

---

## Respuesta exitosa

**Status**: `201 Created`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "firstName": "Ana",
  "lastName": "García",
  "email": "ana.garcia@example.com",
  "role": "STUDENT",
  "status": "ACTIVE"
}
```

---

## Escenarios de aceptación

### Caso 1 — Registro exitoso

**Archivo**: `caso-01-exitoso.json`  
**Gherkin (Escenarios 1 y 3)**:
> Dado que ingreso un correo válido y una contraseña válida, cuando envío el formulario, entonces el sistema crea la cuenta y la deja lista para autenticación.

**Status esperado**: `201 Created`  
**Respuesta**: objeto con `id`, `firstName`, `lastName`, `email`, `role: "STUDENT"`, `status: "ACTIVE"`.

---

### Caso 2 — Email ya registrado

**Archivo**: `caso-02-email-duplicado.json`  
**Gherkin (Escenario 2)**:
> Dado que el correo ya existe, cuando envío el formulario, entonces el sistema rechaza el registro.

**Prerequisito**: ejecutar Caso 1 primero para que el email ya exista en la BD.  
**Status esperado**: `409 Conflict`

---

### Caso 3 — Contraseña no cumple la política

**Archivo**: `caso-03-contrasena-debil.json`  
**Gherkin (Escenario 2)**:
> Dado que la contraseña no cumple la política de seguridad, cuando envío el formulario, entonces el sistema rechaza el registro.

**Status esperado**: `400 Bad Request`  
**Nota**: el campo `password` no supera la validación `@Pattern`.

---

### Caso 4 — Email con formato inválido

**Archivo**: `caso-04-email-invalido.json`  
**Gherkin (Escenario alternativo error)**:
> Dado que el correo tiene formato inválido, cuando intento enviar la solicitud, entonces el sistema rechaza el pedido por validación.

**Status esperado**: `400 Bad Request`  
**Nota**: el campo `email` no supera la validación `@Email`.

---

### Caso 5 — Campo obligatorio vacío

**Archivo**: `caso-05-campo-faltante.json`  
**Gherkin (Escenario 4 — Alternativo error)**:
> Dado que no completo un campo obligatorio, cuando intento registrar la cuenta, entonces el sistema devuelve un error de validación y no guarda datos.

**Status esperado**: `400 Bad Request`  
**Nota**: el campo `firstName` vacío no supera la validación `@NotBlank`. Aplica igual para `lastName`.

---

## Cómo probar en Postman

1. Activa el ambiente **MentorEdu — Local** en Postman.
2. Asegúrate de que el backend esté corriendo en `http://localhost:8080`.
3. Crea un request `POST` con URL `{{base_url}}/{{api_v1}}/auth/register`.
4. En **Headers**, agrega `Content-Type: application/json`.
5. En **Body → raw → JSON**, pega el contenido de `request.body` del archivo `caso-XX.json`.
6. Ejecuta y verifica que el código de respuesta coincida con `expected_response.status`.
7. Para el Caso 2, ejecuta primero el Caso 1 con el mismo email.
