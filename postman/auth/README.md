# Bounded Context: Auth (EP-01)

Gestiona el registro, inicio de sesión y recuperación de acceso de los usuarios.

---

## Implementados

| ID | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU01 | Registro de cuenta con correo y contraseña | `POST /api/v1/auth/register` | `MentorEduAuthHU01-RegistroPOST` | 2026-05-15 |
| HU02 | Login con correo y contraseña | `POST /api/v1/auth/login` | `MentorEduAuthHU02-LoginPOST` | 2026-05-16 |
| HU03 | Solicitar recuperación de contraseña | `POST /api/v1/auth/forgot-password` | `MentorEduAuthHU03-ForgotPasswordPOST` | 2026-05-16 |
| HU26 | Resetear contraseña con token | `POST /api/v1/auth/reset-password` | `MentorEduAuthHU26-ResetPasswordPOST` | 2026-05-16 |
| F0.5 | Renovar access token con refresh token | `POST /api/v1/auth/refresh` | `MentorEduAuthF05-RefreshTokenPOST` | 2026-05-21 |

## Historial de cambios en HU01

**US01-v2 (F0.3 — 2026-05-21)**: El campo `role` es ahora **obligatorio** en `POST /auth/register`. Valores válidos: `STUDENT`, `TEACHER`, `ACADEMY`. Los roles `MODERATOR` y `ADMIN` se rechazan con 400. Casos 06–08 documentan los nuevos escenarios.

## Historias pendientes

Todas las HUs del bounded context **auth** han sido implementadas.

---

## Estructura de carpetas

```
auth/
├── README.md           ← este archivo
├── HU01-registro/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-email-duplicado.json
│   ├── caso-03-contrasena-debil.json
│   ├── caso-04-email-invalido.json
│   └── caso-05-campo-faltante.json
├── HU02-login/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-credenciales-invalidas.json
│   ├── caso-03-cuenta-inactiva.json
│   ├── caso-04-email-invalido.json
│   └── caso-05-campo-faltante.json
├── HU03-password-recovery/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-email-no-registrado.json
│   ├── caso-03-email-invalido.json
│   └── caso-04-campo-faltante.json
├── HU26-reset-password/
│   ├── README.md
│   ├── caso-01-exitoso.json
│   ├── caso-02-token-expirado.json
│   ├── caso-03-token-ya-usado.json
│   ├── caso-04-contrasena-debil.json
│   ├── caso-05-campo-faltante.json
│   └── caso-06-token-inexistente.json
└── HU-refresh/
    ├── README.md
    ├── caso-01-exitoso.json
    ├── caso-02-token-revocado.json
    ├── caso-03-token-expirado.json
    └── caso-04-campo-faltante.json
```

Cuando se implemente una nueva HU, crear su carpeta dentro de `auth/` con el mismo patrón y moverla de "Pendientes" a "Implementadas" en este README.

---

## Notas del bounded context

- El endpoint `/api/v1/auth/**` es público (`permitAll` en `SecurityConfig`). No requiere token.
- **US01-v2**: el campo `role` es obligatorio en el registro. Valores: `STUDENT`, `TEACHER`, `ACADEMY`.
- Las contraseñas se almacenan cifradas con BCrypt (RN-03).
- El login devuelve `accessToken` (corta duración) y `refreshToken` (larga duración).
- Usar `POST /auth/refresh` (F0.5) para renovar el `accessToken` sin re-autenticarse.
- A partir de HU02, los endpoints del resto de la API requieren `Authorization: Bearer <access_token>`.
