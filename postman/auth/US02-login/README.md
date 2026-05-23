# HU02 — Sign in with email and password

## Endpoint

```
POST /api/v1/auth/login
Content-Type: application/json
```

No requiere autenticación previa.

---

## Request body

| Campo    | Tipo   | Requerido | Validaciones                        |
|----------|--------|-----------|-------------------------------------|
| email    | String | Sí        | Formato email válido, no vacío      |
| password | String | Sí        | No vacío                            |

```json
{
  "email": "juan@example.com",
  "password": "Password123"
}
```

---

## Response exitosa — 200 OK

```json
{
  "accessToken": "<JWT>",
  "refreshToken": "<UUID>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "<UUID>",
    "email": "juan@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "STUDENT"
  }
}
```

| Campo        | Descripción                              |
|--------------|------------------------------------------|
| accessToken  | JWT firmado con HS256, vigencia 15 min   |
| refreshToken | UUID aleatorio, vigencia 7 días          |
| tokenType    | Siempre "Bearer"                         |
| expiresIn    | Segundos de vigencia del access token    |
| user         | Datos básicos del usuario autenticado    |

---

## Reglas de negocio aplicadas

| Regla | Descripción                                                                  |
|-------|------------------------------------------------------------------------------|
| RN-02 | El usuario debe tener exactamente un rol activo                              |
| RN-03 | La contraseña se verifica con BCrypt (nunca texto plano)                     |

---

## Escenarios de aceptación

| Caso | Archivo                          | Status esperado |
|------|----------------------------------|-----------------|
| 1    | caso-01-exitoso.json             | 200 OK          |
| 2    | caso-02-credenciales-invalidas.json | 401 Unauthorized |
| 3    | caso-03-cuenta-inactiva.json     | 401 Unauthorized |
| 4    | caso-04-email-invalido.json      | 400 Bad Request |
| 5    | caso-05-campo-faltante.json      | 400 Bad Request |

---

## Nombre del request en Postman

```
MentorEduAuthHU02-LoginPOST
```

---

## Uso del token

Tras un login exitoso, copia el valor de `accessToken` y guárdalo en la variable de entorno `{{access_token}}` de Postman para usar en los endpoints protegidos.
