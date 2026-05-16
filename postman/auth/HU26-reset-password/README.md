# HU26 — Reset password with token

## Endpoint

```
POST /api/v1/auth/reset-password
Content-Type: application/json
```

No requiere autenticación previa. Requiere el token obtenido en HU03.

---

## Request body

| Campo       | Tipo   | Requerido | Validaciones                                                          |
|-------------|--------|-----------|-----------------------------------------------------------------------|
| token       | String | Sí        | No vacío. Token de 64 chars generado en `forgot-password`.           |
| newPassword | String | Sí        | 8–72 chars, al menos 1 mayúscula, 1 minúscula, 1 dígito             |

```json
{
  "token": "<hex_token_64_chars>",
  "newPassword": "NewPassword456"
}
```

---

## Response exitosa — 200 OK

```json
{
  "message": "Password reset successfully. All active sessions have been closed."
}
```

---

## Reglas de negocio aplicadas

| Regla | Descripción                                                                    |
|-------|--------------------------------------------------------------------------------|
| RN-03 | La nueva contraseña se almacena cifrada con BCrypt                             |
| RN-04 | El token debe estar vigente (no expirado) y no haber sido usado previamente    |
| US26  | Al resetear, todas las sesiones activas del usuario quedan revocadas           |

---

## Escenarios de aceptación

| Caso | Archivo                          | Status esperado |
|------|----------------------------------|-----------------|
| 1    | caso-01-exitoso.json             | 200 OK          |
| 2    | caso-02-token-expirado.json      | 400 Bad Request |
| 3    | caso-03-token-ya-usado.json      | 400 Bad Request |
| 4    | caso-04-contrasena-debil.json    | 400 Bad Request |
| 5    | caso-05-campo-faltante.json      | 400 Bad Request |

---

## Nombre del request en Postman

```
MentorEduAuthHU26-ResetPasswordPOST
```

---

## Prerequisito

Ejecutar primero `POST /api/v1/auth/forgot-password` (HU03) con un email registrado y copiar el `token` de la respuesta.
