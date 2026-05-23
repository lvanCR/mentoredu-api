# Postman — MentorEdu API v2.0

Casos de validación manual organizados por Historia de Usuario (US01–US28).

## Importar la colección

Desde Swagger/OpenAPI con el backend corriendo:

1. Abre Postman → **Import** → **Link**
2. URL: `http://localhost:8080/v3/api-docs`
3. Importar entorno: `environments/local.postman_environment.json`

Variables de entorno disponibles: `api_v1`, `access_token`, `refresh_token`, `teacher_token`, `academy_token`, `moderator_token`, `target_user_id`, `thread_id`, `resource_id`, `report_id`.

## Estructura

```
postman/
├── environments/
│   └── local.postman_environment.json
├── auth/               US01 registro · US02 login/logout/refresh · US03 recuperar contraseña
├── profile/            US04 perfil estudiante · US05 perfil docente · US06 perfil academia
├── library/            US07 subir PDF · US08 metadatos · US09 buscar · US10 descargar · US11 mis recursos · US16 ejercicio
├── forum/              US12 crear hilo · US13 responder · US14 reaccionar · US15 comentar
├── pedagogy/           US17 ver resoluciones · US18 enviar resolución · US19 dar feedback · US20 ver mi solución
├── community/          US21 seguir · US22 verificación · US23 revisar verificación · US24 asociación
│                       US25 reportar · US26 resolver reporte · US27 notificaciones
└── catalog/            US28 catálogo (solo ADMIN)
```

## Convención de archivos

`caso-{NN}-{descripcion-corta}.json` — usar `{{variable}}` de entorno para UUIDs y tokens.

## Flujo de trabajo

1. `mvn spring-boot:run` (Docker Compose levanta PostgreSQL automáticamente)
2. Importar colección desde `http://localhost:8080/v3/api-docs`
3. Activar entorno `MentorEdu — Local`
4. Consultar el `README.md` de la carpeta de la US antes de ejecutar
5. Copiar `request.body` del `caso-XX.json` a Postman → Body → raw → JSON
