# Historias de Usuario — MentorEdu (Documentación Funcional)
### Versión orientada al diseño de producto y la experiencia de usuario

> **¿Para quién es este documento?**
> Para diseñadores de interfaz, product managers, clientes y cualquier persona involucrada en el producto que quiera entender el comportamiento esperado del sistema sin necesidad de conocer programación o bases de datos.

---

## Bounded Contexts del Sistema

Un **Bounded Context** es una zona de responsabilidad clara dentro de la aplicación. Cada una gestiona su propio conjunto de funcionalidades y datos, y tiene una razón de existir bien definida.

| Bounded Context | Responsabilidad en el negocio |
|---|---|
| **Auth (Acceso)** | Gestiona todo lo relacionado con la identidad del usuario: crear cuenta, iniciar sesión, cerrar sesión, y recuperar la contraseña olvidada. Es la puerta de entrada a la plataforma. |
| **Profile (Perfiles)** | Administra la información profesional o académica de cada usuario según su rol. Un estudiante tiene sus metas universitarias, un docente su experiencia profesional, y una academia sus datos institucionales. |
| **Catalog (Catálogo)** | Contiene los datos maestros del sistema: las universidades, áreas de examen, cursos y carreras disponibles en la plataforma. Solo los administradores pueden gestionarlo. |
| **Library (Biblioteca)** | Gestiona los recursos académicos: documentos PDF que los docentes y academias suben para que los estudiantes puedan buscarlos y descargarlos. También permite publicar ejercicios sin solución. |
| **Forum (Foro)** | Es el espacio de preguntas y respuestas de la comunidad. Los usuarios pueden crear hilos de consulta, responder a otros, comentar y reaccionar al contenido. |
| **Pedagogy (Pedagógico)** | Gestiona el ciclo de aprendizaje activo: los estudiantes envían resoluciones a ejercicios publicados y los docentes les dan retroalimentación personalizada con una puntuación. |
| **Community (Comunidad)** | Cubre todo lo relacionado con la confianza y la interacción social: seguir a otros usuarios, solicitar verificación de identidad, vincular docentes a academias, reportar contenido, y recibir notificaciones. |

---

## Épicas y su relación con los Bounded Contexts

| Épica | Nombre | Descripción funcional | Bounded Contexts involucrados |
|---|---|---|---|
| **EP-01** | Acceso y Perfiles | Permite a cualquier persona crear su cuenta, elegir su rol dentro de la plataforma y completar su perfil profesional o académico para que el sistema y otros usuarios le reconozcan correctamente. | Auth, Profile |
| **EP-02** | Biblioteca de Recursos | Permite a docentes y academias subir y publicar material educativo en PDF (exámenes, guías, apuntes, prácticas), y a los estudiantes buscarlo y descargarlo para su preparación. | Library |
| **EP-03** | Foro de Preguntas y Respuestas | Crea un espacio de comunidad donde cualquier usuario puede plantear dudas académicas, responderlas, comentar y reaccionar al contenido de otros. | Forum |
| **EP-04** | Ciclo Pedagógico | Habilita el proceso de evaluación formativa: el docente publica un ejercicio sin solución, el estudiante lo resuelve y lo envía, y el docente revisa el trabajo y da retroalimentación. | Library, Pedagogy |
| **EP-05** | Comunidad y Confianza | Construye la capa de confianza de la plataforma: verificación de identidad, asociación entre docentes y academias, seguimiento de usuarios, moderación de contenido, y notificaciones de actividad. | Community, Catalog |

---

## EP-01: Acceso y Perfiles

---

### US01 — Registrar cuenta con email y rol

**Como** visitante de la plataforma,
**quiero** crear una cuenta eligiendo si soy estudiante, docente o academia,
**para** acceder a todas las funcionalidades de MentorEdu con los permisos adecuados a mi perfil.

**Escenario 1 — Registro exitoso**
```
Dado que soy un nuevo visitante y tengo mis datos a mano
Cuando completo el formulario de registro con mi nombre, apellido, correo electrónico,
  una contraseña segura y selecciono mi rol (Estudiante, Docente o Academia)
  y presiono el botón "Registrarme"
Entonces el sistema crea mi cuenta exitosamente con estado ACTIVO
Y me otorga acceso inmediato entregando las llaves de seguridad (tokens) necesarias
Y se crea automáticamente un perfil base para mí según el tipo de cuenta elegido
```

**Escenario 2 — Correo electrónico ya registrado**
```
Dado que ya existe una cuenta con ese correo en la plataforma
Cuando intento registrarme usando ese mismo correo
Entonces el sistema me indica que el email ya está registrado (Error de Conflicto)
Y el formulario permanece visible para que pueda corregir el dato
```

**Escenario 3 — Rol no válido**
```
Dado que el registro solo permite perfiles de Estudiante, Docente o Academia
Cuando intento enviar un rol distinto (como Administrador o Moderador)
Entonces el sistema me indica que el rol es inválido
```

**Escenario 4 — Datos obligatorios faltantes**
```
Dado que el formulario tiene campos obligatorios
Cuando intento registrarme omitiendo el correo o la contraseña
Entonces el sistema me informa que los datos están incompletos
```

**Reglas aplicadas:** RN-01, RN-02

---

### US02 — Iniciar y cerrar sesión

**Como** usuario registrado en la plataforma,
**quiero** poder iniciar sesión con mi correo y contraseña y también cerrar mi sesión activa,
**para** acceder a mi cuenta de forma segura y terminar mi sesión cuando lo decida.

**Escenario 1 — Inicio de sesión exitoso**
```
Dado que tengo una cuenta activa en la plataforma
Cuando ingreso mi correo electrónico y mi contraseña correctos
  y presiono el botón "Iniciar sesión"
Entonces el sistema me autentica y me entrega mis tokens de acceso y renovación
Y registra esta nueva sesión en el historial del sistema
```

**Escenario 2 — Credenciales incorrectas**
```
Dado que ingreso una contraseña incorrecta o un correo no registrado
Cuando presiono el botón "Iniciar sesión"
Entonces el sistema me deniega el acceso con un mensaje genérico por seguridad
```

**Escenario 3 — Cierre de sesión exitoso**
```
Dado que tengo una sesión activa y quiero salir
Cuando envío mi solicitud de cierre con mi token de renovación
Entonces el sistema revoca mi sesión de forma segura
Y mis tokens dejan de ser válidos para futuras acciones
```

**Escenario 4 — Renovación de sesión con token de refresco**
```
Dado que mi llave de acceso temporal ha vencido pero tengo una llave de renovación válida
Cuando solicito una nueva llave de acceso
Entonces el sistema me entrega una nueva sin necesidad de volver a escribir mis credenciales
```

**Reglas aplicadas:** RN-01, RN-02

---

### US03 — Recuperar contraseña

**Como** usuario que olvidó su contraseña,
**quiero** solicitar un enlace de restablecimiento a mi correo registrado,
**para** recuperar el acceso a mi cuenta sin perder mis datos.

**Escenario 1 — Solicitud de recuperación exitosa**
```
Dado que soy un usuario registrado y olvidé mi contraseña
Cuando ingreso mi correo electrónico en la sección de recuperación
Entonces el sistema me muestra un mensaje genérico confirmando el proceso
Y envía internamente un enlace con un código seguro (token) que dura 60 minutos
```

**Escenario 2 — Restablecimiento exitoso de la clave**
```
Dado que tengo un código de recuperación válido y no expirado
Cuando ingreso mi nueva contraseña y la confirmo
Entonces el sistema actualiza mi clave de forma cifrada (BCrypt)
Y marca el código como utilizado para que no se use de nuevo
```

**Escenario 3 — Código expirado o ya usado**
```
Dado que intento usar un código que ya venció o que ya fue utilizado anteriormente
Cuando intento cambiar mi contraseña
Entonces el sistema me indica que la solicitud no es válida
```

**Reglas aplicadas:** RN-02

---

### US04 — Editar perfil de estudiante

**Como** usuario con rol Estudiante,
**quiero** completar y editar mis datos académicos personales,
**para** que otros usuarios conozcan mi meta universitaria.

**Escenario 1 — Crear perfil de estudiante**
```
Dado que soy Estudiante y aún no he completado mi información académica
Cuando envío mis datos válidos (como grado, universidad de interés, etc.)
Entonces el sistema vincula esta información a mi cuenta de usuario
```

**Escenario 2 — Actualizar perfil existente**
```
Dado que ya tengo un perfil académico creado
Cuando realizo cambios en mi información (ej. cambio de carrera meta)
Entonces el sistema actualiza mis datos y me muestra la versión más reciente
```

**Escenario 3 — Selección desde el catálogo oficial**
```
Dado que el sistema requiere datos precisos
Cuando intento guardar una universidad, área o carrera que no existe en el catálogo oficial
Entonces el sistema me indica que el valor no es válido
```

**Escenario 4 — Intento de acceso con rol incorrecto**
```
Dado que soy Docente o Academia
Cuando intento crear o editar un perfil de Estudiante
Entonces el sistema me deniega la acción por falta de permisos
```

**Reglas aplicadas:** RN-03

---

### US05 — Editar perfil de docente

**Como** usuario con rol Docente,
**quiero** editar mis datos profesionales y mis especialidades académicas,
**para** que los estudiantes puedan conocer mi perfil y mis áreas de enseñanza.

**Escenario 1 — Crear perfil de docente**
```
Dado que soy Docente y mi perfil profesional está vacío
Cuando envío mi información válida (experiencia, especialidades)
Entonces el sistema crea mi perfil profesional docente
```

**Escenario 2 — Actualizar perfil existente**
```
Dado que ya cuento con un perfil docente
Cuando modifico mi biografía profesional o mis datos
Entonces el sistema guarda los cambios y actualiza mi información pública
```

**Escenario 3 — Límite de biografía profesional**
```
Dado que el sistema tiene límites de espacio para la biografía
Cuando intento guardar un texto que excede los 2000 caracteres
Entonces el sistema me solicita que acorte mi descripción
```

**Reglas aplicadas:** RN-03

---

### US06 — Editar perfil de academia

**Como** usuario con rol Academia,
**quiero** editar los datos institucionales de mi academia y ver el perfil público de cualquier usuario,
**para** tener presencia en la plataforma como organización educativa verificable.

**Escenario 1 — Crear perfil de academia**
```
Dado que soy una cuenta de Academia y no he registrado mis datos institucionales
Cuando ingreso el nombre de la organización, RUC y sitio web
Entonces el sistema crea mi perfil institucional verificado
```

**Escenario 2 — Nombre de organización ya existente**
```
Dado que ya existe otra academia registrada con el mismo nombre
Cuando intento registrar el mismo nombre para mi academia
Entonces el sistema me indica que el nombre ya está en uso (Error de Conflicto)
```

**Escenario 3 — Ver perfil público de otros usuarios**
```
Dado que soy cualquier usuario identificado en el sistema
Cuando visito el perfil de otro miembro (estudiante, docente o academia)
Entonces el sistema me muestra su información pública (nombre, biografía, rol) sin revelar datos sensibles como el correo electrónico
```

**Reglas aplicadas:** RN-01

---

## EP-02: Biblioteca de Recursos

---

### US07 — Subir archivo PDF

**Como** usuario con rol Docente o Academia,
**quiero** subir un archivo PDF a la plataforma,
**para** usarlo como recurso académico o ejercicio.

**Escenario 1 — Subida exitosa**
```
Dado que soy Docente o Academia y tengo un archivo PDF válido
Cuando subo el archivo al sistema
Entonces el sistema guarda el documento y me entrega una identificación (ID) y dirección web (URL) del archivo
```

**Escenario 2 — Tipo de archivo no permitido**
```
Dado que el sistema solo acepta documentos PDF por seguridad
Cuando intento subir un archivo de otro tipo (como Word o una imagen)
Entonces el sistema rechaza el archivo indicando que el formato no es compatible
```

**Nota de diseño:** Los estudiantes también pueden subir archivos a este paso inicial porque lo necesitan para enviar sus resoluciones en pasos posteriores. La restricción de quién puede "publicar material oficial" se aplica al registrar los datos del recurso.

---

### US08 — Registrar metadatos de un recurso

**Como** usuario con rol Docente o Academia,
**quiero** registrar la información detallada de un recurso usando un archivo previamente subido,
**para** que el material sea visible y los estudiantes puedan encontrarlo.

**Escenario 1 — Registro de información exitoso**
```
Dado que tengo un archivo subido y completo los datos (título, tipo, universidad, área)
Cuando guardo la información del recurso
Entonces el sistema publica el material en la biblioteca
Y por defecto queda configurado para no aceptar resoluciones de estudiantes
```

**Escenario 2 — Inconsistencia entre carrera y área**
```
Dado que seleccioné un área académica específica
Cuando intento asignar al recurso una carrera que no pertenece a esa área
Entonces el sistema me indica que la carrera no coincide con el área seleccionada
```

**Escenario 3 — Datos obligatorios según el tipo de recurso**
```
Dado que el sistema valida la calidad de la información
Cuando intento publicar una Práctica o un Examen por Sección sin indicar el curso específico
Entonces el sistema me informa que el curso es obligatorio para este tipo de materiales
```

**Reglas aplicadas:** RN-05, RN-06, RN-07, RN-23

---

### US09 — Buscar y filtrar recursos

**Como** cualquier usuario de la plataforma,
**quiero** buscar recursos académicos usando filtros como universidad, área y curso,
**para** encontrar el material más relevante para mi preparación.

**Escenario 1 — Búsqueda con filtros específicos**
```
Dado que estoy buscando material de una universidad y área concretas
Cuando aplico los filtros en la biblioteca
Entonces el sistema me muestra solo los recursos que cumplen exactamente con esos criterios
```

**Escenario 2 — Búsqueda por texto libre**
```
Dado que ingreso una palabra clave (ej. "matemática") en el buscador
Cuando ejecuto la búsqueda
Entonces el sistema me devuelve todos los materiales que incluyan ese término en su título o descripción
```

**Escenario 3 — Resultados organizados (Paginación)**
```
Dado que hay muchos recursos en el sistema
Cuando realizo una búsqueda general
Entonces el sistema me presenta los resultados en grupos de 20 por página para facilitar la lectura
```

**Reglas aplicadas:** RN-06, RN-07

---

### US10 — Descargar un recurso

**Como** cualquier usuario de la plataforma,
**quiero** descargar el PDF de un recurso académico,
**para** estudiar con el material sin necesidad de estar conectado.

**Escenario 1 — Descarga exitosa**
```
Dado que encuentro un recurso de mi interés y estoy identificado en el sistema
Cuando presiono el botón de descargar
Entonces el sistema me entrega el enlace de descarga directa del archivo
Y registra automáticamente que he realizado la descarga para fines de estadísticas y auditoría
```

**Escenario 2 — Recurso no encontrado**
```
Dado que el material que intento descargar ya no está disponible
Cuando intento acceder al enlace de descarga
Entonces el sistema me informa que el recurso no fue encontrado
```

**Reglas aplicadas:** RN-07

---

### US11 — Ver mis recursos publicados

**Como** usuario Docente o Academia,
**quiero** ver el listado de todos los recursos que he subido a la plataforma,
**para** gestionar y tener control sobre mi contenido.

**Escenario 1 — Listado personal de materiales**
```
Dado que soy un autor con material publicado
Cuando accedo a mi sección privada de materiales
Entonces el sistema me muestra todos mis recursos ordenados desde el más reciente al más antiguo
```

**Escenario 2 — Autor sin publicaciones**
```
Dado que aún no he subido ningún material a la biblioteca
Cuando accedo a mi sección de materiales
Entonces el sistema me muestra una lista vacía confirmando que no tengo publicaciones aún
```

**Reglas aplicadas:** RN-05

---

## EP-03: Foro de Preguntas y Respuestas

---

### US12 — Crear un hilo en el foro

**Como** usuario autenticado,
**quiero** publicar una pregunta o duda en el foro con al menos una categoría de contexto,
**para** que otros usuarios con el mismo interés lo encuentren y puedan ayudarme.

**Escenario 1 — Publicación con clasificación académica**
```
Dado que tengo una duda sobre un curso específico
Cuando publico mi pregunta indicando al menos el curso, la universidad o la carrera
Entonces el sistema crea el hilo de conversación en estado ABIERTO
```

**Escenario 2 — Publicación anónima**
```
Dado que no quiero revelar mi identidad al hacer una pregunta
Cuando activo la opción "Publicar como anónimo"
Entonces el sistema oculta mi nombre al resto de la comunidad
Pero guarda internamente quién soy solo para fines de moderación
```

**Escenario 3 — Combinaciones de categorías prohibidas**
```
Dado que el foro requiere orden en la clasificación
Cuando intento publicar un hilo indicando una carrera y un curso al mismo tiempo
Entonces el sistema me indica que no se pueden combinar ambas categorías
```

**Escenario 4 — Área sin universidad**
```
Dado que las áreas académicas pertenecen a instituciones específicas
Cuando intento publicar indicando un área pero sin seleccionar a qué universidad pertenece
Entonces el sistema me informa que el área requiere una universidad seleccionada
```

**Reglas aplicadas:** RN-12, RN-13

---

### US13 — Responder a un hilo

**Como** usuario autenticado,
**quiero** publicar una respuesta en un hilo del foro,
**para** compartir mi conocimiento o experiencia con quien formuló la pregunta.

**Escenario 1 — Respuesta exitosa**
```
Dado que hay un hilo abierto con una pregunta que sé responder
Cuando escribo mi respuesta y presiono "Responder"
Entonces el sistema publica mi aporte de forma inmediata
Y el autor de la pregunta recibe una notificación (siempre que no sea yo mismo)
```

**Escenario 2 — Intento de responder a un hilo cerrado**
```
Dado que la conversación ya fue finalizada y el hilo está marcado como CERRADO
Cuando intento escribir una respuesta
Entonces el sistema me informa que el hilo ya no acepta nuevos aportes
```

**Escenario 3 — Cerrar una conversación (Autor o Moderador)**
```
Dado que soy el autor de la pregunta o tengo un rol de Moderación
Cuando decido finalizar el hilo
Entonces el sistema marca la conversación como CERRADA y oculta el campo de respuesta
```

**Reglas aplicadas:** RN-13, RN-14

---

### US14 — Reaccionar a contenido del foro

**Como** usuario autenticado,
**quiero** reaccionar con "me gusta" o "no me gusta" a un hilo o respuesta,
**para** expresar si el contenido me resultó útil o no.

**Escenario 1 — Activar y desactivar reacciones (Toggle)**
```
Dado que veo un aporte valioso
Cuando presiono "Me gusta", el sistema registra mi reacción
Y si vuelvo a presionar el mismo botón, el sistema elimina mi reacción anterior
```

**Escenario 2 — Cambiar de tipo de reacción**
```
Dado que ya reaccioné con un "No me gusta"
Cuando presiono "Me gusta" sobre el mismo aporte
Entonces el sistema reemplaza mi reacción anterior por la nueva
```

**Escenario 3 — Notificación al autor**
```
Dado que reacciono al aporte de otro usuario
Cuando mi reacción es guardada
Entonces el autor del contenido recibe una notificación avisándole que alguien reaccionó a su aporte
```

**Reglas aplicadas:** RN-15

---

### US15 — Comentar en una respuesta

**Como** usuario autenticado,
**quiero** agregar un comentario breve a una respuesta del foro,
**para** hacer aclaraciones, agradecer la información o continuar la conversación.

**Escenario 1 — Comentario exitoso**
```
Dado que quiero complementar una respuesta ya existente
Cuando escribo mi comentario bajo la respuesta
Entonces el sistema publica mi texto vinculado a esa respuesta específica
Y notifica al autor de la respuesta original
```

**Escenario 2 — Contenido obligatorio**
```
Dado que los comentarios deben aportar valor
Cuando intento publicar un comentario vacío
Entonces el sistema me indica que el comentario no puede estar en blanco
```

---

## EP-04: Ciclo Pedagógico

---

### US16 — Publicar un ejercicio sin solución

**Como** usuario con rol Docente o Academia,
**quiero** marcar un recurso tipo "Práctica" para que los estudiantes puedan enviar sus resoluciones,
**para** evaluar su proceso de aprendizaje de forma activa y personalizada.

**Escenario 1 — Activar evaluación al publicar**
```
Dado que soy un autor y publico una Práctica
Cuando activo la opción de "Aceptar resoluciones"
Entonces el sistema marca el recurso como un ejercicio evaluable
```

**Escenario 2 — Activar evaluación en material ya existente**
```
Dado que ya tengo una Práctica publicada anteriormente
Cuando accedo a la configuración del recurso y activo la recepción de resoluciones
Entonces el sistema habilita el botón de envío para todos los estudiantes
```

**Escenario 3 — Restricción de tipo de recurso**
```
Dado que solo las prácticas son evaluables por diseño
Cuando intento activar esta opción en una Guía o Examen Completo
Entonces el sistema me informa que solo los recursos de tipo Práctica aceptan resoluciones
```

**Reglas aplicadas:** RN-05, RN-08

---

### US17 — Ver resoluciones de mis ejercicios

**Como** usuario Docente o Academia,
**quiero** ver el listado y el detalle de las resoluciones que los estudiantes enviaron a mis ejercicios,
**para** revisar su trabajo antes de dar retroalimentación.

**Escenario 1 — Ver lista de estudiantes que respondieron**
```
Dado que soy el autor del ejercicio evaluable
Cuando accedo a la sección de resoluciones
Entonces el sistema me muestra a todos los estudiantes que han enviado su trabajo, indicando quiénes están pendientes de revisión
```

**Escenario 2 — Ver el trabajo de un estudiante**
```
Dado que quiero calificar a un estudiante específico
Cuando abro su envío
Entonces el sistema me muestra el texto o el archivo que el estudiante subió como solución
```

**Escenario 3 — Privacidad de las resoluciones**
```
Dado que el trabajo de los estudiantes es privado
Cuando otro estudiante intenta acceder a la lista de resoluciones de un docente
Entonces el sistema le deniega el acceso con un mensaje de permiso denegado
```

**Reglas aplicadas:** RN-08, RN-10

---

### US18 — Enviar mi resolución a un ejercicio

**Como** usuario con rol Estudiante,
**quiero** subir mi resolución en PDF a un ejercicio publicado por un docente,
**para** que pueda revisarla y darme retroalimentación sobre mi proceso de aprendizaje.

**Escenario 1 — Envío exitoso de la tarea**
```
Dado que soy Estudiante y el ejercicio está abierto para recibir respuestas
Cuando envío mi explicación en texto o adjunto mi archivo de resolución
Entonces el sistema confirma la recepción y notifica al docente evaluador
```

**Escenario 2 — Intento de envío duplicado**
```
Dado que ya entregué mi resolución para un ejercicio
Cuando intento realizar un segundo envío sobre el mismo material
Entonces el sistema me informa que ya tienes una resolución enviada y no permite duplicados
```

**Escenario 3 — Restricción por rol de cuenta**
```
Dado que las resoluciones son solo para perfiles de aprendizaje
Cuando un usuario con rol de Docente intenta enviar una resolución a su propio ejercicio o al de otro
Entonces el sistema le indica que solo los estudiantes pueden realizar envíos
```

**Reglas aplicadas:** RN-08, RN-09, RN-10

---

### US19 — Dar feedback correctivo a una resolución

**Como** usuario Docente o Academia,
**quiero** revisar la resolución de un estudiante y escribir mis comentarios con una puntuación,
**para** guiar su aprendizaje de forma formal y personalizada.

**Escenario 1 — Registro de calificación y comentarios**
```
Dado que soy el evaluador y reviso el trabajo de un estudiante
Cuando ingreso mi retroalimentación y asigno una nota entre 0 y 10
Entonces el sistema guarda mi revisión y cambia el estado de la tarea a REVISADA
```

**Escenario 2 — Inmutabilidad de la calificación**
```
Dado que la calificación es un registro formal de progreso
Cuando intento editar o enviar un segundo feedback sobre la misma tarea
Entonces el sistema me informa que la resolución ya cuenta con feedback y no puede modificarse
```

**Escenario 3 — Validación de la nota**
```
Dado que la escala de evaluación es de 0 a 10
Cuando intento ingresar una nota fuera de ese rango (ej. 15)
Entonces el sistema me solicita corregir el valor antes de guardar
```

**Reglas aplicadas:** RN-10, RN-11, RN-22

---

### US20 — Ver mi resolución y el feedback recibido

**Como** usuario Estudiante,
**quiero** ver la resolución que envié y los comentarios que el docente dejó sobre ella,
**para** entender en qué aspectos debo mejorar mi preparación.

**Escenario 1 — Ver mi nota y correcciones**
```
Dado que el docente ya terminó de revisar mi entrega
Cuando accedo a mi sección de resolución del ejercicio
Entonces el sistema me muestra mi trabajo original junto a las observaciones y la puntuación del docente
```

**Escenario 2 — Ver entrega en espera de revisión**
```
Dado que acabo de enviar mi tarea y aún no ha sido calificada
Cuando consulto mi envío
Entonces el sistema me muestra mi contenido entregado pero me indica que el feedback todavía no está disponible
```

**Reglas aplicadas:** RN-09, RN-10

---

## EP-05: Comunidad y Confianza

---

### US21 — Seguir a un usuario

**Como** usuario autenticado,
**quiero** seguir a otros miembros de la plataforma (docentes, academias u otros estudiantes),
**para** mantenerme conectado con las personas cuyo contenido me resulta valioso.

**Escenario 1 — Seguir y dejar de seguir (Toggle)**
```
Dado que visito el perfil de una persona interesante
Cuando presiono "Seguir", el sistema me vincula a su actividad
Y si vuelvo a presionar el botón, el sistema elimina el vínculo de seguimiento de forma inmediata
```

**Escenario 2 — Prohibición de seguirse a uno mismo**
```
Dado que estoy viendo mi propio perfil público
Cuando intento presionar el botón de seguimiento
Entonces el sistema detecta que es mi cuenta y me indica que no es posible seguirse a uno mismo
```

**Reglas aplicadas:** RN-21

---

### US22 — Solicitar verificación de identidad

**Como** usuario con rol Docente o Academia,
**quiero** enviar una solicitud de verificación adjuntando documentos que acrediten mi identidad,
**para** obtener un distintivo de cuenta verificada que inspire confianza a los demás usuarios.

**Escenario 1 — Solicitud con respaldo documental**
```
Dado que soy Docente o Academia y deseo verificar mi cuenta
Cuando envío mi solicitud adjuntando al menos un documento oficial (DNI, Título, RUC)
Entonces el sistema registra mi pedido con estado EN REVISIÓN
```

**Escenario 2 — Solicitud vacía rechazada**
```
Dado que la confianza requiere pruebas
Cuando intento enviar una solicitud de verificación sin adjuntar ningún documento
Entonces el sistema me solicita incluir al menos un archivo para poder proceder
```

**Escenario 3 — Una solicitud a la vez**
```
Dado que ya tengo una solicitud siendo evaluada por el equipo
Cuando intento enviar otra solicitud idéntica
Entonces el sistema me informa que ya cuento con una verificación en proceso o activa
```

**Reglas aplicadas:** RN-16

---

### US23 — Aprobar o rechazar una verificación

**Como** usuario con rol Moderador o Administrador,
**quiero** revisar y resolver solicitudes de verificación de identidad,
**para** otorgar o denegar el sello de cuenta verificada según los documentos presentados.

**Escenario 1 — Resolución del equipo de moderación**
```
Dado que existe una solicitud pendiente de un usuario
Cuando el Moderador revisa los documentos y elige APROBAR o RECHAZAR
Entonces el sistema actualiza el estado de la cuenta del usuario y le envía una notificación con el resultado
```

**Escenario 2 — Obligatoriedad de motivo en caso de rechazo**
```
Dado que un rechazo debe ser justificado para que el usuario pueda corregirlo
Cuando el Moderador intenta rechazar una cuenta sin escribir el motivo
Entonces el sistema le exige indicar la razón del rechazo antes de finalizar
```

**Reglas aplicadas:** RN-17

---

### US24 — Asociar docente a academia

**Como** usuario con rol Docente,
**quiero** solicitar vincularme a una academia de la plataforma,
**para** aparecer como parte de su equipo docente y ampliar mi visibilidad profesional.

**Escenario 1 — Solicitud de vínculo laboral/académico**
```
Dado que soy Docente y encuentro mi academia en el sistema
Cuando envío una solicitud de asociación
Entonces la academia recibe una propuesta de vínculo en estado PENDIENTE
```

**Escenario 2 — Resolución por parte de la academia**
```
Dado que soy el administrador de una Academia y veo una solicitud de un docente
Cuando presiono ACEPTAR o RECHAZAR
Entonces el sistema formaliza el vínculo (si es aceptado) y notifica al docente sobre la decisión institucional
```

**Reglas aplicadas:** RN-18

---

### US25 — Reportar contenido inapropiado

**Como** usuario autenticado,
**quiero** reportar hilos, respuestas, comentarios o recursos que violen las normas de la comunidad,
**para** ayudar a mantener la plataforma como un espacio seguro y de calidad.

**Escenario 1 — Reporte de contenido de forma exitosa**
```
Dado que encuentro algo que va en contra de las normas (ej. insultos o material no académico)
Cuando selecciono la opción "Reportar", elijo el motivo y lo envío
Entonces el sistema genera un reporte ABIERTO para que los moderadores lo atiendan
```

**Escenario 2 — Revisión de reportes (Solo personal autorizado)**
```
Dado que soy Moderador o Administrador
Cuando accedo al panel de control
Entonces el sistema me permite ver y filtrar todos los reportes que los usuarios han realizado
```

**Reglas aplicadas:** RN-13, RN-19

---

### US26 — Resolver un reporte de moderación

**Como** usuario con rol Moderador o Administrador,
**quiero** revisar y resolver un reporte asignándole una acción concreta,
**para** mantener la integridad y el buen ambiente del contenido en la plataforma.

**Escenario 1 — Acción de moderación completada**
```
Dado que estoy atendiendo un reporte de la comunidad
Cuando decido resolverlo e incluyo mis notas de moderación
Entonces el sistema marca el reporte como RESUELTO y registra en un historial de auditoría quién tomó la decisión y en qué momento
```

**Escenario 2 — Reporte ya procesado**
```
Dado que un reporte ya fue cerrado anteriormente
Cuando intento volver a procesarlo
Entonces el sistema me indica que la acción ya fue realizada y no se puede duplicar
```

**Reglas aplicadas:** RN-13, RN-19

---

### US27 — Ver mis notificaciones

**Como** usuario autenticado,
**quiero** ver mis notificaciones no leídas y marcarlas como leídas,
**para** estar al tanto de la actividad relevante que ocurrió en la plataforma relacionada conmigo.

**Escenario 1 — Centro de notificaciones actualizado**
```
Dado que han ocurrido eventos de mi interés (alguien me siguió, calificaron mi tarea, etc.)
Cuando abro mis notificaciones
Entonces el sistema me muestra los avisos más recientes primero, destacando cuáles aún no he leído
```

**Escenario 2 — Limpieza de notificaciones leídas**
```
Dado que ya revisé una notificación
Cuando la marco como leída
Entonces el sistema la quita de mi lista de pendientes y actualiza mi contador visual
```

**Tipos de avisos automáticos del sistema:**

| Evento | Quién lo recibe |
|---|---|
| **Alguien comenzó a seguirte** | El usuario seguido |
| **Respondieron a tu pregunta del foro** | El autor de la pregunta |
| **Comentaron tu respuesta** | El autor de la respuesta |
| **Reaccionaron a tu contenido** | El autor del aporte |
| **Un estudiante envió una resolución** | El docente autor del ejercicio |
| **Tu tarea fue calificada** | El estudiante que envió la tarea |
| **Tu verificación fue procesada** | El usuario solicitante |
| **Tu pedido de asociación fue resuelto** | El docente solicitante |

---

### US28 — Administrar catálogo del sistema

**Como** usuario con rol Administrador,
**quiero** gestionar el catálogo de universidades, áreas, cursos y carreras disponibles en la plataforma,
**para** mantener los datos maestros actualizados que el resto de los usuarios utilizan como referencia.

**Escenario 1 — Gestión de datos maestros (Universidades/Cursos/Carreras)**
```
Dado que soy Administrador del sistema
Cuando agrego una nueva universidad o vinculo un curso a un área de examen
Entonces el sistema actualiza el catálogo global para que todos los usuarios puedan usar estos nuevos valores de inmediato
```

**Escenario 2 — Restricción de acceso al catálogo**
```
Dado que soy Estudiante o Docente
Cuando intento acceder a las herramientas de administración del catálogo
Entonces el sistema me bloquea el acceso informando que es una sección exclusiva para administradores
```

**Escenario 3 — Consulta pública del catálogo**
```
Dado que soy cualquier visitante (incluso sin cuenta)
Cuando quiero conocer qué universidades o cursos ofrece la plataforma
Entonces el sistema me permite listar esta información de forma abierta y gratuita
```

**Reglas aplicadas:** RN-03, RN-20

---

## Tabla de Reglas de Negocio (As-Built)

| Código | Regla de Negocio | Tipo |
|---|---|---|
| **RN-01** | Cada usuario nace con un rol fijo (Estudiante, Docente o Academia) que define sus permisos permanentes. | Restricción |
| **RN-02** | Toda contraseña se cifra mediante algoritmos seguros (BCrypt). No existe almacenamiento en texto plano. | Seguridad |
| **RN-03** | Todo dato de ubicación o académico (Universidad, Área, etc.) es de selección obligatoria desde el catálogo oficial. | Validación |
| **RN-05** | La facultad de publicar material académico oficial está reservada para Docentes, Academias y Administradores. | Autorización |
| **RN-06** | Los materiales deben clasificarse estrictamente como: Examen Completo, Examen por Sección, Guía, Apuntes, Práctica u Otro. | Validación |
| **RN-07** | Obligatoriedad de datos: Todo recurso requiere Título, Universidad y Área. El Curso es obligatorio salvo para Exámenes Completos, Guías y Apuntes. | Validación |
| **RN-08** | La recepción de tareas de estudiantes solo se permite si el recurso es de tipo "Práctica". | Restricción |
| **RN-09** | Límite de entrega: Un estudiante solo puede enviar una resolución por cada ejercicio publicado. | Restricción |
| **RN-10** | La evaluación y revisión de tareas es exclusiva del autor del material o de docentes vinculados a la academia autora. | Autorización |
| **RN-11** | La retroalimentación enviada es definitiva. Una vez guardada, no permite ediciones ni eliminaciones. | Restricción |
| **RN-12** | Estructura del Foro: Un hilo requiere al menos una categoría. No se permite mezclar Carrera y Curso, ni registrar Áreas sin su Universidad correspondiente. | Validación |
| **RN-13** | Anonimato Protegido: La identidad real de un autor anónimo solo es accesible para el equipo de Moderación y Auditoría. | Seguridad |
| **RN-14** | El cierre de hilos de conversación es potestad exclusiva del autor o de un Moderador/Administrador. | Autorización |
| **RN-15** | Sistema de Reacciones: Un usuario solo puede tener una reacción (Like/Dislike) por aporte. Funciona como interruptor. | Restricción |
| **RN-16** | La verificación de cuenta exige obligatoriamente adjuntar al menos un documento de identidad o título profesional. | Validación |
| **RN-17** | No se permiten rechazos de verificación sin una nota aclaratoria que indique la razón al usuario. | Validación |
| **RN-18** | El vínculo entre docente y academia es un proceso de dos pasos: solicitud y aceptación institucional explícita. | Restricción |
| **RN-19** | Todo acto de moderación genera un rastro de auditoría imborrable con fecha, responsable y acción tomada. | Trazabilidad |
| **RN-20** | Solo la cuenta de Administrador tiene facultades para modificar los datos maestros del sistema (Catálogo). | Autorización |
| **RN-21** | El sistema impide técnicamente que un usuario se convierta en seguidor de su propia cuenta. | Restricción |
| **RN-22** | El rango de calificación aceptado es estrictamente de 0.0 a 10.0 inclusive. | Validación |
| **RN-23** | Integridad del Recurso: Si se elige una carrera, esta debe pertenecer forzosamente al área académica seleccionada para el recurso. | Validación |

---

*Documento de especificación funcional — MentorEdu v2.0 · 28 Historias de Usuario · 5 Épicas · 7 Bounded Contexts*
