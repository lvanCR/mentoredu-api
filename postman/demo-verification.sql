-- Ejecuta estas consultas antes y despues de correr la coleccion Postman.
-- Sirven para mostrar visualmente como los endpoints agregan o alteran la BD.

SELECT id, first_name, last_name, email, points, coins, role_id
FROM users
ORDER BY id;

SELECT id, title, university, year, area, version, verified, anonymous, author_id, created_at
FROM documents
ORDER BY id;

SELECT id, user_id, document_id, downloaded_at
FROM download_logs
ORDER BY id;

SELECT id, follower_id, followed_id, created_at
FROM follows
ORDER BY id;

SELECT id, reported_by, target_type, target_id, reason, status, created_at
FROM reports
ORDER BY id;
