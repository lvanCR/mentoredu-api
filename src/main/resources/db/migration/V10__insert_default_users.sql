-- V10 — Usuarios de sistema: ADMIN y MODERADOR
--
-- UUIDs fijos (prefijo 00000000) para idempotencia: ON CONFLICT DO NOTHING
-- garantiza que re-ejecutar esta migración (ej. en un fresh DB) no falla.
--
-- Contraseñas BCrypt (strength=12), generadas con BCryptPasswordEncoder de Spring Security.
-- NO cambiar los hashes aquí — rotar contraseñas directamente en la BD con UPDATE.
--
-- Admin:     AdminMentor2026!
-- Moderador: ModMentor2026!
--
-- IMPORTANTE: Cambiar estas contraseñas inmediatamente después del primer deploy en producción.

INSERT INTO users (id, first_name, last_name, email, password_hash, provider, status, role_id)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Admin',
    'Sistema',
    'admin@mentoredu.com',
    '$2a$12$2e0mzRPxw13zZudYv0eE4ujli4XAkSZulKBHnm5YAlfOXUrWJSAoe',
    'EMAIL',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'ADMIN')
)
ON CONFLICT DO NOTHING;

INSERT INTO users (id, first_name, last_name, email, password_hash, provider, status, role_id)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'Moderador',
    'Sistema',
    'moderator@mentoredu.com',
    '$2a$12$miXzkKb..Ffk2jnSSrvtpe8uL8zUGEVw0XQBqaDIbJ68StJc5zLqq',
    'EMAIL',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'MODERATOR')
)
ON CONFLICT DO NOTHING;
