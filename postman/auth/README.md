# Auth (US01–US03)

Registro, inicio de sesión y recuperación de contraseña.

| US | Descripción | Endpoint | Carpeta |
|---|---|---|---|
| US01 | Registrar cuenta con email y rol | `POST /api/v1/auth/register` | `US01-registro/` |
| US02 | Iniciar sesión / cerrar sesión | `POST /api/v1/auth/login` · `POST /api/v1/auth/logout` | `US02-login/` |
| US02 | Renovar access token | `POST /api/v1/auth/refresh` | `US02-refresh/` |
| US03 | Solicitar recuperación de contraseña | `POST /api/v1/auth/forgot-password` | `US03-password-recovery/` |
| US03 | Resetear contraseña con token | `POST /api/v1/auth/reset-password` | `US03-reset-password/` |

## Notas

- `/api/v1/auth/**` es público (no requiere token).
- El campo `role` es obligatorio en US01: `STUDENT`, `TEACHER`, `ACADEMY`. Los roles `MODERATOR`/`ADMIN` devuelven 400.
- El login devuelve `accessToken` (15 min) y `refreshToken` (7 días, almacenado en `sessions`).
- Contraseñas almacenadas con BCrypt (RN-02).
