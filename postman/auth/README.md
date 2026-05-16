# Bounded Context: Auth (EP-01)

Gestiona el registro, inicio de sesión y recuperación de acceso de los usuarios.

---

## Historias implementadas

| HU | Descripción | Endpoint | Nombre en Postman | Fecha |
|---|---|---|---|---|
| HU01 | Registro de cuenta con correo y contraseña | `POST /api/v1/auth/register` | `MentorEduAuthHU01-RegistroPOST` | 2026-05-15 |
| HU02 | Login con correo y contraseña | `POST /api/v1/auth/login` | `MentorEduAuthHU02-LoginPOST` | 2026-05-16 |
| HU03 | Solicitar recuperación de contraseña | `POST /api/v1/auth/forgot-password` | `MentorEduAuthHU03-ForgotPasswordPOST` | 2026-05-16 |
| HU26 | Resetear contraseña con token | `POST /api/v1/auth/reset-password` | `MentorEduAuthHU26-ResetPasswordPOST` | 2026-05-16 |

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
└── HU26-reset-password/
    ├── README.md
    ├── caso-01-exitoso.json
    ├── caso-02-token-expirado.json
    ├── caso-03-token-ya-usado.json
    ├── caso-04-contrasena-debil.json
    └── caso-05-campo-faltante.json
```

Cuando se implemente una nueva HU, crear su carpeta dentro de `auth/` con el mismo patrón y moverla de "Pendientes" a "Implementadas" en este README.

---

## Notas del bounded context

- El endpoint `/api/v1/auth/**` es público (`permitAll` en `SecurityConfig`). No requiere token.
- El rol asignado por defecto en registro es `STUDENT` (RN-02).
- Las contraseñas se almacenan cifradas con BCrypt (RN-03).
- A partir de HU02, los endpoints del resto de la API requieren `Authorization: Bearer <access_token>`.
