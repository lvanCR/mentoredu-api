# HU03 — Request password recovery

## Endpoint

```
POST /api/v1/auth/forgot-password
Content-Type: application/json
```

No requiere autenticación previa.

---

## Request body

| Campo | Tipo   | Requerido | Validaciones                   |
|-------|--------|-----------|--------------------------------|
| email | String | Sí        | Formato email válido, no vacío |

```json
{
  "email": "juan@example.com"
}
```

---

## Response exitosa — 200 OK

```json
{
  "message": "Recovery token generated. In production this would be sent via email.",
  "token": "<hex_token_64_chars>"
}
```

| Campo   | Descripción                                                                       |
|---------|-----------------------------------------------------------------------------------|
| message | Confirmación de la operación                                                      |
| token   | Token de 64 caracteres hex (32 bytes aleatorios). En producción se enviaría por email. Vigencia: 60 minutos. |

---

## Reglas de negocio aplicadas

| Regla | Descripción                                                    |
|-------|----------------------------------------------------------------|
| RN-04 | El token de recuperación expira a los 60 minutos de generarse |

---

## Escenarios de aceptación

| Caso | Archivo                              | Status esperado  |
|------|--------------------------------------|------------------|
| 1    | caso-01-exitoso.json                 | 200 OK           |
| 2    | caso-02-email-no-registrado.json     | 404 Not Found    |
| 3    | caso-03-email-invalido.json          | 400 Bad Request  |
| 4    | caso-04-campo-faltante.json          | 400 Bad Request  |

---

## Nombre del request en Postman

```
MentorEduAuthHU03-ForgotPasswordPOST
```

---

## Flujo completo con US26

1. Ejecutar este endpoint para obtener el `token`.
2. Copiar el `token` de la respuesta.
3. Usar ese token en `POST /api/v1/auth/reset-password` (HU26).
