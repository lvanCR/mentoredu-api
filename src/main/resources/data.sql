INSERT INTO roles (name) VALUES ('STUDENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('PREMIUM') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (first_name, last_name, age, email, password, role_id, points, coins)
SELECT 'Ana', 'Torres', 18, 'ana.demo@mentoredu.com',
       '$2a$10$5tH.fY2AOxfZlE90OtYRW.qXqZt9v0MieSk1lv1jpjPmMAh7lvsSy',
       r.id, 120, 8
FROM roles r
WHERE r.name = 'STUDENT'
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (first_name, last_name, age, email, password, role_id, points, coins)
SELECT 'Luis', 'Ramos', 19, 'luis.demo@mentoredu.com',
       '$2a$10$5tH.fY2AOxfZlE90OtYRW.qXqZt9v0MieSk1lv1jpjPmMAh7lvsSy',
       r.id, 80, 3
FROM roles r
WHERE r.name = 'STUDENT'
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (first_name, last_name, age, email, password, role_id, points, coins)
SELECT 'Valeria', 'Premium', 20, 'premium.demo@mentoredu.com',
       '$2a$10$5tH.fY2AOxfZlE90OtYRW.qXqZt9v0MieSk1lv1jpjPmMAh7lvsSy',
       r.id, 250, 30
FROM roles r
WHERE r.name = 'PREMIUM'
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (first_name, last_name, age, email, password, role_id, points, coins)
SELECT 'Mario', 'Admin', 28, 'admin.demo@mentoredu.com',
       '$2a$10$5tH.fY2AOxfZlE90OtYRW.qXqZt9v0MieSk1lv1jpjPmMAh7lvsSy',
       r.id, 500, 99
FROM roles r
WHERE r.name = 'ADMIN'
ON CONFLICT (email) DO NOTHING;

INSERT INTO documents (
    title, type, category, file_url, file_name, content_type, file_size, file_hash, version,
    university, year, area, verified, anonymous, created_at, author_id
)
SELECT 'Examen Quimica UNI 2025', 'PDF', 'Quimica',
       'src/main/resources/sample-pdfs/seed-preview.pdf', 'seed-preview.pdf', 'application/pdf', 352,
       'seed-uni-quimica-2025', 1, 'UNI', 2025, 'Quimica', true, false, NOW(), u.id
FROM users u
WHERE u.email = 'ana.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM documents d
      WHERE d.file_hash = 'seed-uni-quimica-2025'
  );

INSERT INTO documents (
    title, type, category, file_url, file_name, content_type, file_size, file_hash, version,
    university, year, area, verified, anonymous, created_at, author_id
)
SELECT 'Practica Algebra UNMSM 2024', 'PDF', 'Matematica',
       '/uploads/documents/algebra-unmsm-2024.pdf', 'algebra-unmsm-2024.pdf', 'application/pdf', 204800,
       'seed-unmsm-algebra-2024', 1, 'UNMSM', 2024, 'Matematica', false, false, NOW(), u.id
FROM users u
WHERE u.email = 'luis.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM documents d
      WHERE d.file_hash = 'seed-unmsm-algebra-2024'
  );

INSERT INTO documents (
    title, type, category, file_url, file_name, content_type, file_size, file_hash, version,
    university, year, area, verified, anonymous, created_at, author_id
)
SELECT 'Solucionario Fisica PUCP 2023', 'PDF', 'Fisica',
       '/uploads/documents/fisica-pucp-2023.pdf', 'fisica-pucp-2023.pdf', 'application/pdf', 307200,
       'seed-pucp-fisica-2023', 1, 'PUCP', 2023, 'Fisica', true, true, NOW(), u.id
FROM users u
WHERE u.email = 'premium.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM documents d
      WHERE d.file_hash = 'seed-pucp-fisica-2023'
  );

INSERT INTO documents (
    title, type, category, file_url, file_name, content_type, file_size, file_hash, version,
    university, year, area, verified, anonymous, created_at, author_id
)
SELECT 'Examen Biologia ULima 2025', 'PDF', 'Biologia',
       '/uploads/documents/biologia-ulima-2025.pdf', 'biologia-ulima-2025.pdf', 'application/pdf', 256000,
       'seed-ulima-biologia-2025', 1, 'ULima', 2025, 'Biologia', false, false, NOW(), u.id
FROM users u
WHERE u.email = 'ana.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM documents d
      WHERE d.file_hash = 'seed-ulima-biologia-2025'
  );

INSERT INTO follows (follower_id, followed_id, created_at)
SELECT follower.id, followed.id, NOW()
FROM users follower
JOIN users followed ON followed.email = 'premium.demo@mentoredu.com'
WHERE follower.email = 'ana.demo@mentoredu.com'
ON CONFLICT (follower_id, followed_id) DO NOTHING;

INSERT INTO follows (follower_id, followed_id, created_at)
SELECT follower.id, followed.id, NOW()
FROM users follower
JOIN users followed ON followed.email = 'ana.demo@mentoredu.com'
WHERE follower.email = 'luis.demo@mentoredu.com'
ON CONFLICT (follower_id, followed_id) DO NOTHING;

INSERT INTO reports (reported_by, target_type, target_id, reason, status, created_at)
SELECT u.id, 'DOCUMENT', d.id, 'Material semilla marcado para revision de moderacion', 'PENDING', NOW()
FROM users u
JOIN documents d ON LOWER(d.title) = LOWER('Practica Algebra UNMSM 2024')
WHERE u.email = 'ana.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM reports r
      WHERE r.reported_by = u.id
        AND r.target_type = 'DOCUMENT'
        AND r.target_id = d.id
        AND r.reason = 'Material semilla marcado para revision de moderacion'
  );

INSERT INTO download_logs (user_id, document_id, downloaded_at)
SELECT u.id, d.id, NOW()
FROM users u
JOIN documents d ON LOWER(d.title) = LOWER('Examen Quimica UNI 2025')
WHERE u.email = 'luis.demo@mentoredu.com'
  AND NOT EXISTS (
      SELECT 1 FROM download_logs dl
      WHERE dl.user_id = u.id
        AND dl.document_id = d.id
        AND dl.downloaded_at >= CURRENT_DATE
  );
