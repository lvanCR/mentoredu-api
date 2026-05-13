# MentorEdu Postman

Importa en Postman:

- `MentorEdu_API.postman_collection.json`
- `MentorEdu_Local.postman_environment.json`

Orden recomendado para demo:

1. Ejecuta `00.1 - Seed Data / Demo / Login seed - Ana demo`.
2. Ejecuta `00.1 - Seed Data / Demo / Seed search - documentos iniciales`; guarda `documentId`.
3. Ejecuta los requests de `Documents`, `Gamification`, `Follows` y `Reports`.
4. Abre pgAdmin o tu cliente SQL y ejecuta `postman/demo-verification.sql` antes y despues para visualizar cambios en tablas.

Usuarios semilla:

- `ana.demo@mentoredu.com` / `Password123`
- `luis.demo@mentoredu.com` / `Password123`
- `premium.demo@mentoredu.com` / `Password123`
- `admin.demo@mentoredu.com` / `Password123`

Para `Documents / Upload PDF`, la coleccion usa `postman/assets/sample.pdf`. Si Postman no toma la variable `{{samplePdfPath}}`, selecciona ese archivo manualmente en el campo `file`.

Requests que alteran la base de datos y sirven para sustentacion:

- `Auth / Register ...`: agrega usuarios.
- `Documents / Publish JSON ...`: agrega documentos.
- `Documents / Upload PDF - valido`: agrega archivo y metadatos.
- `Documents / Download ...`: agrega filas en `download_logs`.
- `Documents / Toggle anonymous ...`: cambia el campo `anonymous`.
- `Gamification / Add points`, `Add coins`, `Redeem coins`: cambia `users.points` y `users.coins`.
- `Follows / Follow`, `Unfollow`: cambia `follows`.
- `Reports / Create report`: agrega reportes.
