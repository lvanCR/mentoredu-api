# Historias de Usuario — MentorEdu (v2.0)

## Tabla de Épicas

| Épica | Dominio | Historias | BCs involucrados |
|---|---|---|---|
| EP-01 | Acceso y Perfiles | US01–US06 | Auth, Profile |
| EP-02 | Biblioteca de Recursos | US07–US11 | Library |
| EP-03 | Foro de Preguntas y Respuestas | US12–US15 | Forum |
| EP-04 | Ciclo Pedagógico | US16–US20 | Library, Pedagogy |
| EP-05 | Comunidad y Confianza | US21–US28 | Community, Catalog |

---

## EP-01: Acceso y Perfiles

### US01 — Registrar cuenta con email y rol

**Como** visitante no autenticado,
**quiero** crear una cuenta eligiendo mi rol (estudiante, docente o academia),
**para** acceder a la plataforma con los permisos adecuados a mi perfil.

**Escenario 1 — Registro exitoso como estudiante**
```gherkin
Dado que soy un visitante con datos válidos y rol STUDENT
Cuando envío POST /api/v1/auth/register con email, contraseña y role="STUDENT"
Entonces recibo 201 Created con access token JWT y refresh token
Y el usuario se crea con estado ACTIVE y rol STUDENT
Y se crea un registro base en profiles con profile_type=STUDENT
```

**Escenario 2 — Email duplicado**
```gherkin
Dado que ya existe un usuario con el mismo email
Cuando envío POST /api/v1/auth/register
Entonces recibo 409 Conflict con mensaje "El email ya está registrado"
```

**Escenario 3 — Rol inválido**
```gherkin
Dado que envío un rol que no es STUDENT, TEACHER ni ACADEMY
Cuando envío POST /api/v1/auth/register
Entonces recibo 400 Bad Request con mensaje "Rol inválido"
```

**Escenario 4 — Datos faltantes**
```gherkin
Dado que omito email o contraseña
Cuando envío POST /api/v1/auth/register
Entonces recibo 400 Bad Request
```

**Reglas:** RN-01, RN-02
**Notificación:** ninguna

---

### US02 — Iniciar y cerrar sesión

**Como** usuario registrado,
**quiero** iniciar sesión con mi email y contraseña y cerrar mi sesión activa,
**para** acceder a los recursos protegidos y terminar mi sesión de forma segura.

**Escenario 1 — Login exitoso**
```gherkin
Dado que tengo una cuenta activa
Cuando envío POST /api/v1/auth/login con credenciales correctas
Entonces recibo 200 OK con access token (15 min) y refresh token (7 días)
Y se registra una nueva sesión en la tabla sessions
```

**Escenario 2 — Credenciales incorrectas**
```gherkin
Dado que envío contraseña incorrecta
Cuando envío POST /api/v1/auth/login
Entonces recibo 401 Unauthorized
```

**Escenario 3 — Logout exitoso**
```gherkin
Dado que tengo una sesión activa y envío el refresh token
Cuando envío POST /api/v1/auth/logout
Entonces recibo 204 No Content
Y la sesión queda revocada (revoked_at actualizado)
```

**Escenario 4 — Refresh token expirado o revocado**
```gherkin
Dado que el refresh token es inválido o expiró
Cuando envío POST /api/v1/auth/refresh
Entonces recibo 401 Unauthorized
```

**Escenario 5 — Renovación de sesión exitosa**
```gherkin
Dado que tengo un refresh token válido
Cuando envío POST /api/v1/auth/refresh
Entonces recibo 200 OK con nuevo access token
```

**Reglas:** RN-01, RN-02
**Notificación:** ninguna

---

### US03 — Recuperar contraseña

**Como** usuario que olvidó su contraseña,
**quiero** solicitar un enlace de restablecimiento,
**para** recuperar el acceso a mi cuenta.

**Escenario 1 — Solicitud exitosa**
```gherkin
Dado que soy un usuario registrado
Cuando envío POST /api/v1/auth/forgot-password con mi email
Entonces recibo 200 OK con mensaje genérico (nunca revela si el email existe)
Y se genera un token seguro (32 bytes hex, 1 hora de vigencia)
Y se envía el enlace por email (log en dev, SendGrid SMTP en prod)
```

**Escenario 2 — Email no registrado**
```gherkin
Dado que el email no existe en la BD
Cuando envío POST /api/v1/auth/forgot-password
Entonces recibo 200 OK con el mismo mensaje genérico
```

**Escenario 3 — Restablecimiento exitoso**
```gherkin
Dado que tengo un token válido y no expirado
Cuando envío POST /api/v1/auth/reset-password con token y nueva contraseña
Entonces recibo 200 OK
Y la contraseña se actualiza cifrada con BCrypt
Y el token queda marcado como usado
```

**Escenario 4 — Token expirado o ya usado**
```gherkin
Dado que el token expiró o ya fue usado
Cuando envío POST /api/v1/auth/reset-password
Entonces recibo 400 Bad Request
```

**Reglas:** RN-02
**Notificación:** ninguna

---

### US04 — Editar perfil de estudiante

**Como** usuario con rol STUDENT,
**quiero** completar y editar mis datos académicos personales,
**para** que otros usuarios conozcan mi meta universitaria.

**Escenario 1 — Crear perfil de estudiante**
```gherkin
Dado que tengo rol STUDENT y aún no tengo student_profile
Cuando envío POST /api/v1/profiles/student con datos válidos
Entonces recibo 201 Created con los datos del perfil
Y se crea un registro en student_profiles vinculado a mi profile
```

**Escenario 2 — Actualizar perfil existente**
```gherkin
Dado que ya tengo student_profile
Cuando envío PATCH /api/v1/profiles/student/me con campos a modificar
Entonces recibo 200 OK con el perfil actualizado
```

**Escenario 3 — Universidad/área/carrera no existe en catálogo**
```gherkin
Dado que envío un ID de universidad que no existe en el catálogo
Cuando envío POST o PATCH a /api/v1/profiles/student
Entonces recibo 400 Bad Request con "Valor no encontrado en catálogo"
```

**Escenario 4 — Rol incorrecto**
```gherkin
Dado que tengo rol TEACHER o ACADEMY
Cuando intento acceder a POST /api/v1/profiles/student
Entonces recibo 403 Forbidden
```

**Reglas:** RN-03
**Notificación:** ninguna

---

### US05 — Editar perfil de docente

**Como** usuario con rol TEACHER,
**quiero** editar mis datos profesionales y mis especialidades académicas,
**para** que los estudiantes puedan conocer mi perfil y mis áreas de enseñanza.

**Escenario 1 — Crear perfil de docente**
```gherkin
Dado que tengo rol TEACHER y aún no tengo teacher_profile
Cuando envío POST /api/v1/profiles/teacher con datos válidos
Entonces recibo 201 Created con el perfil creado
```

**Escenario 2 — Actualizar perfil existente**
```gherkin
Dado que ya tengo teacher_profile
Cuando envío PATCH /api/v1/profiles/teacher/me
Entonces recibo 200 OK con el perfil actualizado
```

**Escenario 3 — Rol incorrecto**
```gherkin
Dado que tengo rol STUDENT o ACADEMY
Cuando intento acceder a POST /api/v1/profiles/teacher
Entonces recibo 403 Forbidden
```

**Reglas:** RN-03
**Notificación:** ninguna

---

### US06 — Editar perfil de academia

**Como** usuario con rol ACADEMY,
**quiero** editar los datos institucionales de mi academia y ver el perfil público de cualquier usuario,
**para** tener presencia en la plataforma como organización educativa verificable.

**Escenario 1 — Crear perfil de academia**
```gherkin
Dado que tengo rol ACADEMY y no tengo academy_profile
Cuando envío POST /api/v1/profiles/academy con nombre, RUC y website
Entonces recibo 201 Created con el perfil institucional
```

**Escenario 2 — Actualizar perfil existente**
```gherkin
Dado que ya tengo academy_profile
Cuando envío PATCH /api/v1/profiles/academy/me con nuevos datos
Entonces recibo 200 OK con el perfil actualizado
```

**Escenario 3 — Nombre de organización duplicado**
```gherkin
Dado que el nombre de organización ya existe en BD
Cuando intento crear academy_profile con el mismo nombre
Entonces recibo 409 Conflict
```

**Escenario 4 — Ver perfil público de un usuario**
```gherkin
Dado que soy cualquier usuario autenticado
Cuando envío GET /api/v1/profiles/{userId}
Entonces recibo 200 OK con los datos públicos del perfil (sin datos sensibles como email)
```

**Reglas:** RN-01
**Notificación:** ninguna

---

## EP-02: Biblioteca de Recursos

### US07 — Subir archivo PDF

**Como** usuario con rol TEACHER o ACADEMY,
**quiero** subir un archivo PDF a la plataforma,
**para** usarlo como recurso académico o ejercicio.

**Escenario 1 — Subida exitosa**
```gherkin
Dado que soy TEACHER o ACADEMY y envío un archivo PDF válido
Cuando envío POST /api/v1/resources/files (multipart/form-data)
Entonces recibo 201 Created con {fileId, fileName, fileUrl, sizeBytes}
Y el archivo se almacena (local en dev, Cloudinary en prod)
```

**Escenario 2 — Tipo de archivo no permitido**
```gherkin
Dado que envío un archivo que no es PDF
Cuando envío POST /api/v1/resources/files
Entonces recibo 415 Unsupported Media Type
```

**Escenario 3 — Sin autenticación**
```gherkin
Dado que no estoy autenticado
Cuando intento POST /api/v1/resources/files
Entonces recibo 401 Unauthorized
```

> **Nota de diseño:** STUDENT puede subir archivos a este endpoint porque lo necesita para enviar su resolución en US18 (POST /resources/{id}/solutions con fileId). La restricción de rol (RN-05) aplica en US08 al registrar metadatos públicos, no en la subida del archivo.

**Reglas:** RN-05
**Notificación:** ninguna

---

### US08 — Registrar metadatos de un recurso

**Como** usuario con rol TEACHER o ACADEMY,
**quiero** registrar los metadatos de un recurso usando un archivo previamente subido,
**para** que el recurso sea visible y buscable por los estudiantes.

**Escenario 1 — Registro exitoso**
```gherkin
Dado que tengo un fileId válido y envío metadatos completos
Cuando envío POST /api/v1/resources con {fileId, title, type, universityId, areaId}
  y opcionalmente {careerId, courseId} según el tipo
Entonces recibo 201 Created con el recurso completo
Y el recurso queda con acepta_resoluciones=false por defecto
```

**Escenario 2 — fileId inválido o no pertenece al usuario**
```gherkin
Dado que el fileId no existe o pertenece a otro usuario
Cuando envío POST /api/v1/resources
Entonces recibo 403 Forbidden
```

**Escenario 3 — Universidad/área/curso/carrera no en catálogo**
```gherkin
Dado que algún ID no existe en el catálogo
Cuando envío POST /api/v1/resources
Entonces recibo 400 Bad Request especificando el campo que falló
```

**Escenario 4 — Campo obligatorio faltante según tipo**
```gherkin
Dado que omito title, type, universityId o areaId
  o que envío type=EXAMEN_SECCION, PRACTICA u OTRO sin courseId
Cuando envío POST /api/v1/resources
Entonces recibo 400 Bad Request con la lista de campos faltantes
```

**Escenario 5 — career_id inconsistente con area_id**
```gherkin
Dado que envío un careerId cuya carrera no pertenece al areaId enviado
Cuando envío POST /api/v1/resources
Entonces recibo 400 Bad Request con "La carrera no pertenece al área seleccionada"
```

**Reglas:** RN-05, RN-06, RN-07, RN-23
**Notificación:** ninguna

---

### US09 — Buscar y filtrar recursos

**Como** cualquier usuario autenticado,
**quiero** buscar recursos académicos usando filtros como universidad, área y curso,
**para** encontrar el material más relevante para mi preparación.

**Escenario 1 — Búsqueda con filtros**
```gherkin
Dado que soy usuario autenticado
Cuando envío GET /api/v1/resources?universityId=X&areaId=Y&careerId=Z&type=EXAMEN_COMPLETO
Entonces recibo 200 OK con lista paginada de recursos que coinciden con los filtros
Y si careerId está presente, solo se devuelven recursos de esa carrera o del área completa
```

**Escenario 2 — Sin filtros (listado general)**
```gherkin
Dado que no paso filtros opcionales
Cuando envío GET /api/v1/resources
Entonces recibo 200 OK con los recursos más recientes (paginado, 20 por página)
```

**Escenario 3 — Búsqueda por texto libre**
```gherkin
Dado que paso el parámetro ?q=matematica
Cuando envío GET /api/v1/resources?q=matematica
Entonces recibo 200 OK con recursos cuyo título contiene el término buscado
```

**Escenario 4 — Sin resultados**
```gherkin
Dado que los filtros son válidos pero no hay coincidencias
Cuando envío GET /api/v1/resources?universityId=X&type=EXAMEN
Entonces recibo 200 OK con lista vacía []
```

**Reglas:** RN-06, RN-07
**Notificación:** ninguna

---

### US10 — Descargar un recurso

**Como** cualquier usuario autenticado,
**quiero** descargar el PDF de un recurso académico,
**para** estudiar con el material offline.

**Escenario 1 — Descarga exitosa**
```gherkin
Dado que el recurso existe y estoy autenticado
Cuando envío GET /api/v1/resources/{id}/download
Entonces recibo 200 OK con la URL de descarga del archivo
```

**Escenario 2 — Recurso no encontrado**
```gherkin
Dado que el ID no corresponde a ningún recurso
Cuando envío GET /api/v1/resources/{id}/download
Entonces recibo 404 Not Found
```

**Escenario 3 — Sin autenticación**
```gherkin
Dado que no estoy autenticado
Cuando intento GET /api/v1/resources/{id}/download
Entonces recibo 401 Unauthorized
```

**Reglas:** RN-07
**Notificación:** ninguna

---

### US11 — Ver mis recursos publicados

**Como** usuario TEACHER o ACADEMY,
**quiero** ver el listado de recursos que he publicado,
**para** gestionar mi contenido en la plataforma.

**Escenario 1 — Listado de mis recursos**
```gherkin
Dado que soy TEACHER o ACADEMY autenticado
Cuando envío GET /api/v1/resources/me
Entonces recibo 200 OK con mis recursos paginados, ordenados por created_at DESC
```

**Escenario 2 — Sin recursos publicados**
```gherkin
Dado que aún no he publicado ningún recurso
Cuando envío GET /api/v1/resources/me
Entonces recibo 200 OK con lista vacía []
```

**Reglas:** RN-05
**Notificación:** ninguna

---

## EP-03: Foro de Preguntas y Respuestas

### US12 — Crear un hilo en el foro

**Como** usuario autenticado,
**quiero** publicar una pregunta o duda en el foro con al menos una categoría de contexto,
**para** que otros usuarios con el mismo interés lo encuentren y puedan ayudarme.

**Escenario 1 — Creación exitosa (modo académico global)**
```gherkin
Dado que soy usuario autenticado y envío solo courseId válido
Cuando envío POST /api/v1/threads con {courseId, title, body, isAnonymous}
Entonces recibo 201 Created con el hilo creado con status=OPEN
```

**Escenario 2 — Creación exitosa (modo institucional con área)**
```gherkin
Dado que soy usuario autenticado y envío universityId y areaId válidos
Cuando envío POST /api/v1/threads con {universityId, areaId, title, body, isAnonymous}
Entonces recibo 201 Created con el hilo creado
Y el área pertenece a la universidad enviada
```

**Escenario 3 — Creación exitosa (modo vocacional)**
```gherkin
Dado que soy usuario autenticado y envío solo careerId válido
Cuando envío POST /api/v1/threads con {careerId, title, body, isAnonymous}
Entonces recibo 201 Created con el hilo creado
```

**Escenario 4 — Publicación anónima**
```gherkin
Dado que envío isAnonymous=true
Cuando el hilo se crea y se consulta
Entonces la respuesta muestra author como null o "Anónimo"
Pero internamente se almacena el author_id para moderación
```

**Escenario 5 — Área enviada sin universidad**
```gherkin
Dado que envío areaId pero omito universityId
Cuando envío POST /api/v1/threads
Entonces recibo 400 Bad Request con "El área requiere una universidad seleccionada"
```

**Escenario 6 — Carrera y curso enviados simultáneamente**
```gherkin
Dado que envío tanto careerId como courseId
Cuando envío POST /api/v1/threads
Entonces recibo 400 Bad Request con "No puedes combinar carrera y curso en el mismo hilo"
```

**Escenario 7 — Área no pertenece a la universidad enviada**
```gherkin
Dado que envío universityId=PUCP y areaId=Área-Ingeniería-UNI
Cuando envío POST /api/v1/threads
Entonces recibo 400 Bad Request con "El área no pertenece a la universidad seleccionada"
```

**Escenario 8 — Sin ninguna categoría**
```gherkin
Dado que envío solo title y body sin ningún ID de clasificación
Cuando envío POST /api/v1/threads
Entonces recibo 400 Bad Request con "El hilo requiere al menos una categoría"
```

**Escenario 9 — Campos obligatorios faltantes**
```gherkin
Dado que omito title o body
Cuando envío POST /api/v1/threads
Entonces recibo 400 Bad Request
```

**Reglas:** RN-12, RN-13
**Notificación:** ninguna al crear

---

### US13 — Responder a un hilo

**Como** usuario autenticado,
**quiero** publicar una respuesta en un hilo del foro,
**para** compartir mi conocimiento o ayudar a quien preguntó.

**Escenario 1 — Respuesta exitosa**
```gherkin
Dado que el hilo existe y está en status=OPEN
Cuando envío POST /api/v1/threads/{id}/answers con {body}
Entonces recibo 201 Created con la respuesta registrada
Y se dispara notificación tipo "answer_received" al autor del hilo (si ≠ respondedor)
```

**Escenario 2 — Hilo cerrado**
```gherkin
Dado que el hilo tiene status=CLOSED
Cuando intento responder con POST /api/v1/threads/{id}/answers
Entonces recibo 409 Conflict con "El hilo está cerrado"
```

**Escenario 3 — Hilo no encontrado**
```gherkin
Dado que el threadId no existe
Cuando envío POST /api/v1/threads/{id}/answers
Entonces recibo 404 Not Found
```

**Escenario 4 — Cerrar un hilo (autor o moderador)**
```gherkin
Dado que soy el autor del hilo o tengo rol MODERATOR/ADMIN
Cuando envío PATCH /api/v1/threads/{id}/close
Entonces recibo 200 OK con el hilo en status=CLOSED
```

**Escenario 5 — No autorizado para cerrar**
```gherkin
Dado que no soy el autor ni tengo rol de moderación
Cuando intento PATCH /api/v1/threads/{id}/close
Entonces recibo 403 Forbidden
```

**Reglas:** RN-13, RN-14
**Notificación:** `answer_received` → autor del hilo (si ≠ respondedor)

---

### US14 — Reaccionar a contenido del foro

**Como** usuario autenticado,
**quiero** reaccionar (like/dislike) a un hilo o respuesta,
**para** expresar si el contenido me resultó útil.

**Escenario 1 — Primera reacción (toggle on)**
```gherkin
Dado que aún no he reaccionado a ese contenido
Cuando envío POST /api/v1/threads/{id}/reactions o /api/v1/answers/{id}/reactions con {reactionType}
Entonces recibo 201 Created con mi reacción registrada
Y se dispara notificación tipo "reaction_received" al autor del contenido (si ≠ yo)
```

**Escenario 2 — Segunda reacción del mismo tipo (toggle off)**
```gherkin
Dado que ya reaccioné con el mismo tipo a ese contenido
Cuando envío nuevamente el mismo POST
Entonces recibo 204 No Content y la reacción se elimina
```

**Escenario 3 — Contenido inexistente**
```gherkin
Dado que el threadId o answerId no existe
Cuando envío POST de reacción
Entonces recibo 404 Not Found
```

**Reglas:** RN-15
**Notificación:** `reaction_received` → autor del contenido (si ≠ reactante)

---

### US15 — Comentar en una respuesta

**Como** usuario autenticado,
**quiero** agregar un comentario a una respuesta del foro,
**para** hacer aclaraciones o continuar el hilo de conversación.

**Escenario 1 — Comentario exitoso**
```gherkin
Dado que la respuesta existe
Cuando envío POST /api/v1/answers/{id}/comments con {body}
Entonces recibo 201 Created con el comentario registrado
Y se dispara notificación tipo "comment_received" al autor de la respuesta (si ≠ yo)
```

**Escenario 2 — Respuesta no encontrada**
```gherkin
Dado que el answerId no existe
Cuando envío POST /api/v1/answers/{id}/comments
Entonces recibo 404 Not Found
```

**Escenario 3 — Body vacío**
```gherkin
Dado que envío body vacío o nulo
Cuando envío POST /api/v1/answers/{id}/comments
Entonces recibo 400 Bad Request
```

**Reglas:** ninguna adicional
**Notificación:** `comment_received` → autor de la respuesta (si ≠ comentarista)

---

## EP-04: Ciclo Pedagógico

### US16 — Publicar un ejercicio sin solución

**Como** usuario TEACHER o ACADEMY,
**quiero** marcar un recurso como ejercicio que acepta resoluciones de los estudiantes,
**para** poder evaluar el proceso de aprendizaje.

**Escenario 1 — Activar acepta_resoluciones al crear el recurso**
```gherkin
Dado que soy TEACHER o ACADEMY
Cuando envío POST /api/v1/resources con acepta_resoluciones=true
Entonces recibo 201 Created con el recurso marcado como ejercicio
```

**Escenario 2 — Activar acepta_resoluciones en recurso existente**
```gherkin
Dado que ya tengo un recurso publicado y soy su autor
Cuando envío PATCH /api/v1/resources/{id}/settings con {acepta_resoluciones: true}
Entonces recibo 200 OK con el recurso actualizado
```

**Escenario 3 — STUDENT intenta activar acepta_resoluciones**
```gherkin
Dado que soy STUDENT
Cuando intento enviar acepta_resoluciones=true
Entonces recibo 403 Forbidden
```

**Escenario 4 — Ver resoluciones de mi ejercicio**
```gherkin
Dado que soy el autor del ejercicio
Cuando envío GET /api/v1/resources/{id}/solutions
Entonces recibo 200 OK con la lista de resoluciones enviadas por los estudiantes
```

**Escenario 5 — Tipo incorrecto para acepta_resoluciones**
```gherkin
Dado que soy TEACHER o ACADEMY y envío acepta_resoluciones=true con resource_type != PRACTICA
Cuando envío POST /api/v1/resources o PATCH /api/v1/resources/{id}/settings
Entonces recibo 400 Bad Request con "Solo los recursos de tipo PRACTICA aceptan resoluciones"
```

**Reglas:** RN-05, RN-08
**Notificación:** `solution_submitted` → autor del ejercicio (disparado en US18)

---

### US17 — Ver resoluciones de mis ejercicios

**Como** usuario TEACHER o ACADEMY,
**quiero** ver el detalle de una resolución específica enviada por un estudiante,
**para** revisar su trabajo antes de dar feedback.

**Escenario 1 — Ver listado de resoluciones**
```gherkin
Dado que soy el autor del ejercicio
Cuando envío GET /api/v1/resources/{id}/solutions
Entonces recibo 200 OK con {solutionId, studentName, status, submittedAt} por cada resolución
```

**Escenario 2 — Ver detalle de una resolución específica**
```gherkin
Dado que soy el autor del ejercicio
Cuando envío GET /api/v1/resources/{id}/solutions/{solutionId}
Entonces recibo 200 OK con el detalle incluyendo fileUrl del PDF subido por el estudiante
```

**Escenario 3 — No soy el autor del ejercicio**
```gherkin
Dado que intento ver resoluciones de un ejercicio que no es mío
Cuando envío GET /api/v1/resources/{id}/solutions
Entonces recibo 403 Forbidden
```

**Escenario 4 — Sin resoluciones aún**
```gherkin
Dado que nadie ha enviado resoluciones al ejercicio
Cuando envío GET /api/v1/resources/{id}/solutions
Entonces recibo 200 OK con lista vacía []
```

**Reglas:** RN-08, RN-10
**Notificación:** ninguna (solo lectura)

---

### US18 — Enviar mi resolución a un ejercicio

**Como** usuario STUDENT,
**quiero** subir mi resolución en PDF a un ejercicio publicado,
**para** que el docente la evalúe y me dé retroalimentación.

**Escenario 1 — Envío exitoso**
```gherkin
Dado que soy STUDENT, el recurso acepta resoluciones y no he enviado una antes
Cuando envío POST /api/v1/resources/{id}/solutions con {fileId}
Entonces recibo 201 Created con {solutionId, status: "SUBMITTED", submittedAt}
Y se dispara notificación tipo "solution_submitted" al autor del ejercicio
```

**Escenario 2 — Resolución duplicada**
```gherkin
Dado que ya envié una resolución a ese ejercicio
Cuando intento enviar otra
Entonces recibo 409 Conflict con "Ya enviaste una resolución para este ejercicio"
```

**Escenario 3 — Recurso no acepta resoluciones**
```gherkin
Dado que acepta_resoluciones=false en ese recurso
Cuando envío POST /api/v1/resources/{id}/solutions
Entonces recibo 403 Forbidden
```

**Escenario 4 — Recurso no encontrado**
```gherkin
Dado que el resourceId no existe
Cuando envío POST /api/v1/resources/{id}/solutions
Entonces recibo 404 Not Found
```

**Reglas:** RN-08, RN-09, RN-10
**Notificación:** `solution_submitted` → autor del ejercicio

---

### US19 — Dar feedback correctivo a una resolución

**Como** usuario TEACHER o ACADEMY,
**quiero** dar retroalimentación escrita y con puntuación a la resolución de un estudiante,
**para** guiar su aprendizaje de forma formal.

**Escenario 1 — Feedback exitoso**
```gherkin
Dado que soy el autor del ejercicio y la resolución tiene status=SUBMITTED
Cuando envío POST /api/v1/solutions/{solutionId}/feedback con {body, score}
Entonces recibo 201 Created con el feedback registrado
Y la resolución cambia automáticamente a status=REVIEWED
Y se dispara notificación tipo "feedback_received" al estudiante autor de la resolución
```

**Escenario 2 — No estoy autorizado para dar feedback**
```gherkin
Dado que no soy el autor del recurso ni un TEACHER con TeacherAcademyLink ACCEPTED a la academia autora
Cuando intento dar feedback a una de sus resoluciones
Entonces recibo 403 Forbidden
```

**Escenario 6 — Docente vinculado a academia puede dar feedback**
```gherkin
Dado que el ejercicio fue subido por una ACADEMY
  y soy un TEACHER con TeacherAcademyLink en status=ACCEPTED a esa academia
Cuando envío POST /api/v1/solutions/{solutionId}/feedback con {body, score}
Entonces recibo 201 Created con el feedback registrado bajo mi usuario
Y se dispara notificación tipo "feedback_received" al estudiante
```

**Escenario 3 — Score fuera de rango**
```gherkin
Dado que envío score < 0 o score > 10
Cuando envío POST /api/v1/solutions/{solutionId}/feedback
Entonces recibo 400 Bad Request
```

**Escenario 4 — La resolución ya tiene feedback (inmutabilidad)**
```gherkin
Dado que ya existe un feedback_entry para esa solución
Cuando intento crear otro feedback
Entonces recibo 409 Conflict con "Esta resolución ya tiene feedback"
```

**Escenario 5 — Solución no encontrada**
```gherkin
Dado que el solutionId no existe
Cuando envío POST /api/v1/solutions/{solutionId}/feedback
Entonces recibo 404 Not Found
```

**Reglas:** RN-10, RN-11, RN-22
**Notificación:** `feedback_received` → estudiante autor de la resolución

---

### US20 — Ver mi resolución y feedback recibido

**Como** usuario STUDENT,
**quiero** ver mi resolución enviada y el feedback que el docente dejó,
**para** entender en qué debo mejorar.

**Escenario 1 — Ver resolución con feedback**
```gherkin
Dado que soy el autor de la resolución y el docente ya dio feedback
Cuando envío GET /api/v1/resources/{id}/solutions/mine
Entonces recibo 200 OK con {solution: {..., fileUrl}, feedback: {body, score, createdAt}}
```

**Escenario 2 — Ver resolución sin feedback aún**
```gherkin
Dado que soy el autor de la resolución pero el docente no ha respondido todavía
Cuando envío GET /api/v1/resources/{id}/solutions/mine
Entonces recibo 200 OK con {solution: {...}, feedback: null}
```

**Escenario 3 — Sin resolución enviada**
```gherkin
Dado que no envié resolución para ese ejercicio
Cuando envío GET /api/v1/resources/{id}/solutions/mine
Entonces recibo 404 Not Found
```
**Reglas:** RN-09, RN-10
**Notificación:** ninguna (solo lectura)

---

## EP-05: Comunidad y Confianza

### US21 — Seguir a un usuario

**Como** usuario autenticado,
**quiero** seguir a otros usuarios (docentes, academias o estudiantes),
**para** mantenerme al tanto de su actividad en la plataforma.

**Escenario 1 — Seguir exitoso**
```gherkin
Dado que no sigo aún a ese usuario
Cuando envío POST /api/v1/users/{id}/follow
Entonces recibo 201 Created
Y se crea una fila en follows(follower_id, followed_id)
Y se dispara notificación tipo "new_follower" al usuario seguido
```

**Escenario 2 — Dejar de seguir (toggle)**
```gherkin
Dado que ya sigo a ese usuario
Cuando envío nuevamente POST /api/v1/users/{id}/follow
Entonces recibo 204 No Content y el registro se elimina de follows
```

**Escenario 3 — Intentar seguirse a sí mismo**
```gherkin
Dado que el {id} corresponde a mi propio usuario
Cuando envío POST /api/v1/users/{id}/follow
Entonces recibo 400 Bad Request con "No puedes seguirte a ti mismo"
```

**Escenario 4 — Usuario no encontrado**
```gherkin
Dado que el userId no existe
Cuando envío POST /api/v1/users/{id}/follow
Entonces recibo 404 Not Found
```

**Reglas:** RN-21
**Notificación:** `new_follower` → usuario seguido

---

### US22 — Solicitar verificación de identidad

**Como** usuario TEACHER o ACADEMY,
**quiero** enviar una solicitud de verificación con documentos adjuntos,
**para** obtener un sello de cuenta verificada que genere confianza en la comunidad.

**Escenario 1 — Solicitud exitosa**
```gherkin
Dado que soy TEACHER o ACADEMY y no tengo verificación PENDING o VERIFIED activa
Cuando envío POST /api/v1/verification/requests con al menos un documento adjunto
Entonces recibo 201 Created con {requestId, status: "PENDING"}
```

**Escenario 2 — Ya tiene verificación pendiente o activa**
```gherkin
Dado que tengo una solicitud en status=PENDING o estoy VERIFIED
Cuando intento enviar otra solicitud
Entonces recibo 409 Conflict
```

**Escenario 3 — Sin documentos adjuntos**
```gherkin
Dado que no adjunto ningún documento
Cuando envío POST /api/v1/verification/requests
Entonces recibo 400 Bad Request con "Se requiere al menos un documento"
```

**Escenario 4 — Ver mis solicitudes**
```gherkin
Dado que soy TEACHER o ACADEMY autenticado
Cuando envío GET /api/v1/verification/requests/me
Entonces recibo 200 OK con mi historial de solicitudes
```

**Reglas:** RN-16
**Notificación:** ninguna al crear. Sí al resolverse (ver US23).

---

### US23 — Aprobar o rechazar una verificación

**Como** usuario MODERATOR o ADMIN,
**quiero** revisar y resolver una solicitud de verificación,
**para** dar o denegar el sello de cuenta verificada.

**Escenario 1 — Aprobar verificación**
```gherkin
Dado que hay una solicitud en status=PENDING
Cuando envío PATCH /api/v1/verification/requests/{id}/review con {action: "APPROVED", notes: "..."}
Entonces recibo 200 OK
Y la solicitud queda en status=APPROVED
Y se dispara notificación tipo "verification_processed" al solicitante
```

**Escenario 2 — Rechazar verificación con razón**
```gherkin
Dado que hay una solicitud en status=PENDING
Cuando envío PATCH /api/v1/verification/requests/{id}/review con {action: "REJECTED", notes: "..."}
Entonces recibo 200 OK
Y se dispara notificación tipo "verification_processed" al solicitante
```

**Escenario 3 — Notes faltante en rechazo**
```gherkin
Dado que envío action=REJECTED sin proporcionar notes
Cuando envío PATCH /api/v1/verification/requests/{id}/review
Entonces recibo 400 Bad Request con "Se requiere una razón para el rechazo"
```

**Escenario 4 — Solicitud ya procesada**
```gherkin
Dado que la solicitud ya está en status=APPROVED o REJECTED
Cuando intento procesarla de nuevo
Entonces recibo 409 Conflict con "La solicitud ya fue procesada"
```

**Reglas:** RN-17
**Notificación:** `verification_processed` → solicitante

---

### US24 — Asociar docente a academia

**Como** usuario TEACHER,
**quiero** solicitar asociarme a una academia,
**para** aparecer como parte de su equipo docente y ganar visibilidad.

**Escenario 1 — Solicitud de asociación**
```gherkin
Dado que soy TEACHER y la academia existe
Cuando envío POST /api/v1/associations/teacher-academy con {academyProfileId: "<uuid>"}
Entonces recibo 201 Created con {id, teacherProfileId, academyProfileId, status: "PENDING", requestedAt}
```

**Escenario 2 — Academia acepta la solicitud**
```gherkin
Dado que soy ACADEMY y hay un link en status=PENDING
Cuando envío PATCH /api/v1/associations/teacher-academy/{id}/accept
Entonces recibo 200 OK con el link actualizado en status=ACCEPTED
Y se dispara notificación tipo "association_resolved" al docente
```

**Escenario 3 — Academia rechaza la solicitud**
```gherkin
Dado que soy ACADEMY y hay un link en status=PENDING
Cuando envío PATCH /api/v1/associations/teacher-academy/{id}/reject
Entonces recibo 200 OK con el link actualizado en status=REJECTED
Y se dispara notificación tipo "association_resolved" al docente
```

**Escenario 4 — Solicitud duplicada**
```gherkin
Dado que ya existe un link PENDING o ACCEPTED para ese par (docente, academia)
Cuando el docente intenta crear otra solicitud
Entonces recibo 409 Conflict
```

**Reglas:** RN-18
**Notificación:** `association_resolved` → docente solicitante

---

### US25 — Reportar contenido inapropiado

**Como** usuario autenticado,
**quiero** reportar un hilo, respuesta, comentario o recurso que viola las normas,
**para** ayudar a mantener la plataforma segura y de calidad.

**Escenario 1 — Reporte exitoso**
```gherkin
Dado que el contenido existe y tengo una razón válida
Cuando envío POST /api/v1/moderation/reports con {targetType, targetId, reason}
Entonces recibo 201 Created con el reporte en status=OPEN
```

**Escenario 2 — Tipo de contenido inválido**
```gherkin
Dado que envío un targetType que no es THREAD, ANSWER, COMMENT o RESOURCE
Cuando envío POST /api/v1/moderation/reports
Entonces recibo 400 Bad Request
```

**Escenario 3 — Contenido no encontrado**
```gherkin
Dado que el targetId no existe para el targetType dado
Cuando envío POST /api/v1/moderation/reports
Entonces recibo 404 Not Found
```

**Escenario 4 — Ver reportes pendientes (MODERATOR/ADMIN)**
```gherkin
Dado que soy MODERATOR o ADMIN
Cuando envío GET /api/v1/moderation/reports?status=OPEN
Entonces recibo 200 OK con los reportes abiertos paginados
```

**Reglas:** RN-13, RN-19
**Notificación:** ninguna directa al reportar

---

### US26 — Resolver un reporte de moderación

**Como** MODERATOR o ADMIN,
**quiero** revisar y resolver un reporte asignándole una acción concreta,
**para** mantener la integridad del contenido en la plataforma.

**Escenario 1 — Resolución exitosa**
```gherkin
Dado que existe un reporte en status=OPEN
Cuando envío PATCH /api/v1/moderation/reports/{id}/resolve con {resolutionNote: "..."}
Entonces recibo 200 OK con el reporte en status=RESOLVED
Y se registra la acción en el log de auditoría (actorId + acción + timestamp) — RN-19
```

**Escenario 2 — Reporte ya resuelto**
```gherkin
Dado que el reporte ya está en status=RESOLVED o REJECTED
Cuando intento resolverlo de nuevo
Entonces recibo 409 Conflict
```

**Escenario 3 — Reporte no encontrado**
```gherkin
Dado que el reportId no existe
Cuando envío PATCH /api/v1/moderation/reports/{id}/resolve
Entonces recibo 404 Not Found
```

**Reglas:** RN-13, RN-19
**Notificación:** ninguna directa (no se notifica al denunciado en el MVP)

---

### US27 — Ver mis notificaciones

**Como** usuario autenticado,
**quiero** ver mis notificaciones no leídas y marcarlas como leídas,
**para** estar al tanto de la actividad relevante en la plataforma sin hacer polling activo.

**Escenario 1 — Ver notificaciones pendientes**
```gherkin
Dado que soy usuario autenticado
Cuando envío GET /api/v1/notifications/me/pending
Entonces recibo 200 OK con notificaciones donde read_at IS NULL, ordenadas por created_at DESC
```

**Escenario 2 — Sin notificaciones pendientes**
```gherkin
Dado que no tengo notificaciones sin leer
Cuando envío GET /api/v1/notifications/me/pending
Entonces recibo 200 OK con lista vacía []
```

**Escenario 3 — Marcar notificación como leída**
```gherkin
Dado que existe una notificación no leída que me pertenece
Cuando envío PATCH /api/v1/notifications/{id}/read
Entonces recibo 204 No Content (sin body) y read_at se actualiza en BD
```

**Escenario 4 — Notificación no encontrada o de otro usuario**
```gherkin
Dado que el notificationId no existe o pertenece a otro usuario
Cuando envío PATCH /api/v1/notifications/{id}/read
Entonces recibo 404 Not Found
```

**Tipos de notificación y su origen:**

| type | Generado en | Destinatario |
|---|---|---|
| `new_follower` | US21: alguien te siguió | Usuario seguido |
| `answer_received` | US13: respondieron tu hilo | Autor del hilo |
| `comment_received` | US15: comentaron tu respuesta | Autor de la respuesta |
| `reaction_received` | US14: reaccionaron a tu contenido | Autor del contenido |
| `solution_submitted` | US18: enviaron resolución a tu ejercicio | Autor del ejercicio |
| `feedback_received` | US19: el docente revisó tu resolución | Estudiante autor de la resolución |
| `verification_processed` | US23: verificación resuelta | Solicitante |
| `association_resolved` | US24: solicitud de asociación resuelta | Docente solicitante |

**Patrón técnico obligatorio:**
```java
@Async("notificationExecutor")
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

**Reglas:** ninguna adicional
**Notificación:** recibe notificaciones de todos los eventos anteriores

---

### US28 — Administrar catálogo del sistema

**Como** usuario ADMIN,
**quiero** gestionar el catálogo de universidades, áreas, cursos y carreras,
**para** mantener los datos maestros que el resto de la plataforma usa como referencia.

**Escenario 1 — Crear universidad**
```gherkin
Dado que soy ADMIN
Cuando envío POST /api/v1/catalog/universities con {name, city}
Entonces recibo 201 Created con la universidad registrada
```

**Escenario 2 — Crear área bajo una universidad**
```gherkin
Dado que soy ADMIN y la universidad existe
Cuando envío POST /api/v1/catalog/universities/{id}/areas con {name, description?}
Entonces recibo 201 Created con el área registrada
```

**Escenario 3 — Crear curso y asociarlo a un área**
```gherkin
Dado que soy ADMIN
Cuando envío POST /api/v1/catalog/courses con {name}
Y envío PUT /api/v1/catalog/areas/{id}/courses/{courseId}
Entonces recibo 201 Created en crear curso y 204 en asociar
Y el curso queda asociado al área
```

**Escenario 4 — Crear carrera**
```gherkin
Dado que soy ADMIN y la universidad y el área existen
Cuando envío POST /api/v1/catalog/universities/{id}/careers con {areaId, name, description?}
Entonces recibo 201 Created con la carrera registrada
```

**Escenario 5 — Rol incorrecto**
```gherkin
Dado que soy STUDENT, TEACHER o ACADEMY
Cuando intento crear o modificar entidades del catálogo
Entonces recibo 403 Forbidden
```

**Escenario 6 — Consultar catálogo (público, sin autenticación)**
```gherkin
Dado que soy cualquier visitante o usuario
Cuando envío GET /api/v1/catalog/universities o /courses
Entonces recibo 200 OK con los datos del catálogo sin necesitar token
```

> **Nota:** Los seeds iniciales del catálogo (7 universidades de Lima, áreas de examen con pesos, cursos y carreras) están documentados en `docs/seed-plan-admision-lima.md`. Se cargan como migración V9.

**Reglas:** RN-03, RN-20
**Notificación:** ninguna

---

## Tabla de Reglas de Negocio

| ID | Regla |
|---|---|
| RN-01 | Cada usuario tiene exactamente un rol asignado en el registro (STUDENT, TEACHER o ACADEMY). No cambia. |
| RN-02 | Las contraseñas se almacenan cifradas con BCrypt. Nunca en texto plano. |
| RN-03 | Universidad, área, curso y carrera solo se pueden seleccionar desde el catálogo. No se admite texto libre. |
| RN-05 | Solo TEACHER, ACADEMY o ADMIN pueden subir recursos y activar `acepta_resoluciones`. STUDENT → 403. |
| RN-06 | Tipos de recurso válidos: `EXAMEN_COMPLETO`, `EXAMEN_SECCION`, `GUIA`, `APUNTES`, `PRACTICA`, `OTRO`. |
| RN-07 | Un recurso requiere: universidad, área, tipo y título. `course_id` es obligatorio excepto para `EXAMEN_COMPLETO`, `GUIA` y `APUNTES` (abarca el área completa). `career_id` es siempre opcional. |
| RN-08 | `acepta_resoluciones = true` solo es válido para `resource_type = PRACTICA`. Cualquier otro tipo → 400. |
| RN-09 | Un estudiante puede enviar exactamente una resolución por ejercicio. Segunda solicitud → 409. |
| RN-10 | Pueden ver resoluciones y dar feedback: (a) el autor directo del recurso, o (b) si el autor es ACADEMY, cualquier TEACHER con TeacherAcademyLink en status=ACCEPTED a esa academia. Cualquier otro usuario → 403. |
| RN-11 | El feedback es inmutable una vez enviado. No se edita, no se elimina. Segunda solicitud → 409. |
| RN-12 | Un hilo requiere al menos uno de: `university_id`, `course_id` o `career_id`. Combinaciones prohibidas: (a) `area_id` sin `university_id`, (b) `career_id` + `course_id`, (c) `area_id` cuya universidad no coincida con `university_id`, (d) `career_id` cuya universidad no coincida con `university_id` si ambos están presentes. Todos los IDs deben existir en el catálogo. |
| RN-13 | Posts anónimos guardan `author_id` internamente. Solo MODERATOR/ADMIN puede ver la identidad real. |
| RN-14 | Solo el autor del hilo o un MODERATOR/ADMIN puede cerrarlo. Otro rol → 403. |
| RN-15 | Las reacciones son únicas por usuario por contenido. Funcionan como toggle (segunda llamada elimina la primera). |
| RN-16 | Una solicitud de verificación requiere al menos un documento adjunto. Sin documentos → 400. |
| RN-17 | Un rechazo de verificación requiere razón obligatoria. Sin razón → 400. |
| RN-18 | La asociación docente-academia queda en PENDIENTE hasta que la academia la resuelve explícitamente. |
| RN-19 | Toda resolución de reporte genera una entrada en el log de auditoría (actor + acción + timestamp). |
| RN-20 | Solo ADMIN puede crear o modificar entidades del catálogo (universidades, áreas, cursos, carreras). |
| RN-21 | Un usuario no puede seguirse a sí mismo. CHECK constraint en BD y validación en servicio → 400. |
| RN-22 | El score del feedback está entre 0.0 y 10.0 inclusive. Fuera de rango → 400. |
| RN-23 | Si `career_id` está presente en un recurso, la carrera debe pertenecer al `area_id` del mismo recurso. Violación → 400. |
