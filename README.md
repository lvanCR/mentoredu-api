# MentorEdu API

API REST principal de la plataforma MentorEdu. Gestiona autenticación, perfiles, biblioteca, foro, pedagogía y comunidad.

## Stack

- Java 21 + Spring Boot 4.0.6
- PostgreSQL 16 + Flyway
- JWT + Spring Security
- Docker · Render.com

## Versión desplegada

`v1.0.0` — [ver tag](https://github.com/mentoredu-app/mentoredu-api/releases/tag/v1.0.0)

## Health check

https://mentoredu-api.onrender.com/actuator/health

## Repositorios del proyecto

| Repositorio | Descripción | Deploy |
|---|---|---|
| [mentoredu-api](https://github.com/mentoredu-app/mentoredu-api) | API REST principal | [Render](https://mentoredu-api.onrender.com/actuator/health) |
| [File-service](https://github.com/mentoredu-app/File-service) | Gestión de archivos e imágenes | [Render](https://file-service-e9i8.onrender.com/actuator/health) |
| [mentoredu-frontend](https://github.com/mentoredu-app/mentoredu-frontend) | Aplicación web | [Netlify](https://mentor-edu-frontend.netlify.app) |
| [mentoredu-landing](https://github.com/mentoredu-app/mentoredu-landing) | Landing page | [GitHub Pages](https://mentoredu-app.github.io/mentoredu-landing/) |
