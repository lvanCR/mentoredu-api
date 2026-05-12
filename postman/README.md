# MentorEdu Postman

Importa en Postman:

- `MentorEdu_API.postman_collection.json`
- `MentorEdu_Local.postman_environment.json`

Orden recomendado para demo:

1. Ejecuta `Auth / Register - estudiante principal`.
2. Ejecuta `Auth / Login - estudiante principal`; guarda `token` y `userId`.
3. Ejecuta `Documents / Publish JSON - examen UNI`; guarda `documentId`.
4. Ejecuta los requests de busqueda, descarga, reportes, follow y gamificacion.

Para `Documents / Upload PDF`, crea antes un PDF de prueba en `target/sample.pdf` o selecciona manualmente un archivo PDF en Postman.
