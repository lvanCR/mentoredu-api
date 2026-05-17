# HU10 — Crear perfil de organización

**Epic**: EP-02 Profile  
**Endpoint**: `POST /api/v1/profiles/organization`  
**Autenticación**: Bearer Token requerido  
**Nombre Postman**: `MentorEduProfileHU10-CreateOrganizationProfilePOST`

---

## Descripción

Permite a la organización autenticada crear su perfil institucional. Solo puede ejecutarse una vez por cuenta (RN-10). Requiere que la cuenta sea de tipo ORGANIZATION (US04) y que el nombre institucional sea único en la plataforma.

**Flujo previo requerido**:
1. US01 — Registrar cuenta
2. US02 — Iniciar sesión (obtener token)
3. US04 — Seleccionar tipo de cuenta (ORGANIZATION)

**Flujo siguiente** (EP-03):
4. US33 — Crear academia
5. US11 — Registrar oferta académica

---

## Headers requeridos

| Header | Valor |
|---|---|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer {{access_token}}` |

---

## Body (JSON)

```json
{
  "organizationName": "string (obligatorio, no vacío)",
  "ruc": "string (opcional)",
  "website": "string (opcional)",
  "contactEmail": "email válido (opcional)"
}
```

### Campos del body

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `organizationName` | string | ✅ Sí | No puede estar vacío, máx. 120 caracteres, único |
| `ruc` | string | No | Máx. 20 caracteres, único |
| `website` | string | No | Máx. 255 caracteres |
| `contactEmail` | string | No | Formato email válido, máx. 120 caracteres |

---

## Reglas de negocio aplicables

| Regla | Descripción |
|---|---|
| RN-10 | Una organización solo puede tener un perfil institucional activo |
| RN-11 | Cada perfil debe registrar solo los campos que le corresponden |

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: cuenta ORGANIZATION, nombre válido enviado | `caso-01-exitoso-campos-obligatorios.json` | 201 Created |
| Alt exitoso: todos los campos válidos | `caso-02-exitoso-todos-campos.json` | 201 Created |
| Error: organizationName vacío | `caso-03-nombre-vacio.json` | 400 Bad Request |
| Alt error: nombre institucional ya existe | `caso-04-nombre-duplicado.json` | 409 Conflict |
| Error: tipo de cuenta incorrecto | `caso-05-tipo-incorrecto.json` | 409 Conflict |
| Error: perfil de organización ya existe (RN-10) | `caso-06-perfil-ya-existe.json` | 409 Conflict |
| Error: sin perfil base (US04 no ejecutada) | `caso-07-sin-perfil-base.json` | 404 Not Found |
| Sin autenticación | `caso-08-sin-autenticacion.json` | 401 Unauthorized |

---

## Respuesta exitosa (201)

```json
{
  "profileId": "uuid",
  "organizationName": "Academia Preuniversitaria Lima",
  "ruc": "20123456789",
  "website": "https://academia-lima.pe",
  "contactEmail": "contacto@academia-lima.pe"
}
```
