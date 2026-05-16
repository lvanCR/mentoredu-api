-- V3 — Datos semilla: roles del sistema
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'STUDENT',   'Estudiante preuniversitario'),
    (gen_random_uuid(), 'TEACHER',   'Docente o tutor académico'),
    (gen_random_uuid(), 'ACADEMY',   'Organización o academia de preparación'),
    (gen_random_uuid(), 'MODERATOR', 'Moderador de contenido'),
    (gen_random_uuid(), 'ADMIN',     'Administrador del sistema');
