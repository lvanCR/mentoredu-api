# 📘 Resumen Ejecutivo: Edu Enterprise & MentorEdu

## 🚀 Startup

| Campo | Descripción |
|---|---|
| Nombre | Edu Enterprise |
| Producto | MentorEdu |
| Fundación | 2026 |
| Origen | Universidad Peruana de Ciencias Aplicadas (UPC) |
| Propósito | Democratizar el acceso a materiales de preparación universitaria mediante una plataforma colaborativa, gamificada y móvil. |
| ODS asociado | ODS 4: Educación de calidad (metas 4.4, 4.5 y 4.a) |

## 🎯 Segmentos objetivo

| Segmento | Perfil | Principales necesidades |
|---|---|---|
| Estudiantes preuniversitarios | Jóvenes de 15 a 20 años, de 4.º/5.º de secundaria o egresados, aspirantes a universidades competitivas. | Repositorio de materiales, foros colaborativos, clasificación por institución y curso, gamificación. |
| Docentes | Profesores particulares o de academias que comparten materiales y resuelven dudas. | Verificación, visibilidad, publicación de recursos y participación en foros. |
| Organizaciones | Academias, centros de preparación y entidades educativas. | Perfil institucional, sedes, oferta académica, verificación y administración de recursos. |

## 🧭 Objetivos estratégicos

| Perspectiva | Objetivo |
|---|---|
| Para estudiantes | Acceso equitativo a materiales, espacio seguro para resolver dudas y motivación mediante puntos y niveles. |
| Para docentes | Publicación confiable, reputación profesional y participación ordenada en la comunidad. |
| Para organizaciones | Perfil institucional claro, gestión de oferta académica y presencia verificada dentro de la plataforma. |
| Impacto social | Disminuir barreras de acceso a recursos de preparación universitaria y fortalecer el aprendizaje colaborativo. |

## Especificación de Requisitos y Diseño — MentorEdu
## 1. Épicas (Epics)
| ID | Titulo | Descripcion |
|---|---|---|
| EP-01 | Auth | Gestiona el registro, inicio de sesion y recuperacion de acceso de los usuarios. |
| EP-02 | Profile | Gestiona el perfil base y los perfiles especificos de cada tipo de usuario. |
| EP-03 | Academy | Gestiona las academias de preparacion, sus sedes, programas y ciclos de estudio. |
| EP-04 | Library | Gestiona la publicacion, clasificacion, busqueda y descarga de recursos academicos. |
| EP-05 | Forum | Gestiona los hilos de discusion y las respuestas de la comunidad. |
| EP-06 | Moderation | Gestiona reportes, revisiones y resolucion de incidencias de contenido. |
| EP-07 | Verification | Gestiona la solicitud y aprobacion de verificacion de docentes y organizaciones. |
| EP-08 | Billing | Gestiona suscripciones, pagos y compra de beneficios premium. |
| EP-09 | Notifications | Gestiona las notificaciones generadas por eventos del sistema. |
| EP-10 | Gamification | Gestiona la acumulacion de puntos de experiencia, niveles, insignias y monedas del usuario. |
| EP-11 | Feedback | Gestiona la retroalimentacion academica formal emitida por docentes hacia estudiantes y su consulta. |

## 2. User Stories

### US01 — Register account with email and password
- **Epic**: EP-01
- **Descripcion**: Como visitante, quiero registrarme con correo y contraseña para crear mi cuenta.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que ingreso un correo valido y una contrasena valida, cuando envio el formulario, entonces el sistema crea la cuenta. |
| Error | Dado que el correo ya existe o la contrasena no cumple la politica, cuando envio el formulario, entonces el sistema rechaza el registro. |
| Alternativo exitoso | Dado que completo todos los campos obligatorios correctamente, cuando confirmo el registro, entonces el sistema crea la cuenta y la deja lista para autenticacion. |
| Alternativo error | Dado que no completo un campo obligatorio, cuando intento registrar la cuenta, entonces el sistema devuelve un error de validacion y no guarda datos. |

### US02 — Sign in with email and password
- **Epic**: EP-01
- **Descripcion**: Como usuario registrado, quiero iniciar sesion con mi correo y contraseña para acceder a la plataforma.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que ingreso credenciales validas, cuando envio la solicitud, entonces el sistema autentica la sesion. |
| Error | Dado que las credenciales son incorrectas, cuando intento acceder, entonces el sistema responde con error de autenticacion. |
| Alternativo exitoso | Dado que mi cuenta esta activa y mis credenciales son correctas, cuando inicio sesion, entonces el sistema retorna el token o sesion activa. |
| Alternativo error | Dado que mi cuenta esta bloqueada o inactiva, cuando intento iniciar sesion, entonces el sistema rechaza el acceso. |

### US03 — Request password recovery
- **Epic**: EP-01
- **Descripcion**: Como usuario, quiero solicitar recuperacion de contraseña con mi correo para restablecer mi acceso.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que ingreso un correo registrado, cuando envio la solicitud, entonces el sistema genera el enlace de recuperacion. |
| Error | Dado que el correo no existe, cuando envio la solicitud, entonces el sistema no crea la peticion. |
| Alternativo exitoso | Dado que el correo pertenece a una cuenta valida, cuando confirmo la solicitud, entonces el sistema envia el enlace al correo registrado. |
| Alternativo error | Dado que el correo tiene formato invalido, cuando intento enviar la solicitud, entonces el sistema rechaza el pedido por validacion. |

### US04 — Select account type
- **Epic**: EP-02
- **Descripcion**: Como usuario autenticado, quiero seleccionar mi tipo de cuenta para definir mi ruta de uso.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi cuenta aun no tiene tipo asignado, cuando selecciono un tipo permitido, entonces el sistema guarda la eleccion. |
| Error | Dado que la cuenta ya tiene perfil definido, cuando intento cambiarlo, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que selecciono un tipo de cuenta valido, cuando confirmo el cambio, entonces el sistema asocia mi cuenta al tipo elegido. |
| Alternativo error | Dado que intento seleccionar un tipo no permitido por el sistema, cuando envio la solicitud, entonces el sistema devuelve un error de validacion. |

### US05 — Update common profile data
- **Epic**: EP-02
- **Descripcion**: Como usuario, quiero editar mis datos comunes de perfil para mantener mi informacion actualizada.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que estoy autenticado y completo los datos permitidos, cuando guardo los cambios, entonces el sistema actualiza mi perfil. |
| Error | Dado que dejo un campo obligatorio vacio, cuando intento guardar, entonces el sistema rechaza la actualizacion. |
| Alternativo exitoso | Dado que modifico solo datos comunes como nombre o ciudad, cuando confirmo la edicion, entonces el sistema guarda los cambios sin afectar el tipo de cuenta. |
| Alternativo error | Dado que intento cambiar un campo restringido, cuando envio la actualizacion, entonces el sistema devuelve error de permiso o validacion. |

### US06 — Create student profile
- **Epic**: EP-02
- **Descripcion**: Como estudiante preuniversitario, quiero crear mi perfil academico para registrar mi situacion de preparacion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi cuenta fue clasificada como estudiante, cuando ingreso grado, universidad objetivo y area de preparacion, entonces el sistema crea el perfil. |
| Error | Dado que falta un campo obligatorio, cuando intento crear el perfil, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que completo la informacion academica requerida, cuando confirmo el formulario, entonces el sistema registra mi perfil de estudiante. |
| Alternativo error | Dado que ingreso un valor no valido en el grado o universidad, cuando envio el formulario, entonces el sistema devuelve error de validacion. |

### US07 — Update student target university
- **Epic**: EP-02
- **Descripcion**: Como estudiante preuniversitario, quiero actualizar mi universidad objetivo para mantener mi meta academica vigente.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi perfil de estudiante existe, cuando cambio la universidad objetivo, entonces el sistema guarda la actualizacion. |
| Error | Dado que el campo queda vacio, cuando intento guardar, entonces el sistema rechaza el cambio. |
| Alternativo exitoso | Dado que ingreso una universidad valida, cuando confirmo la edicion, entonces el sistema actualiza el dato sin modificar los demas campos. |
| Alternativo error | Dado que mi perfil de estudiante no existe, cuando intento editarlo, entonces el sistema responde que el recurso no fue encontrado. |

### US08 — Create teacher profile
- **Epic**: EP-02
- **Descripcion**: Como docente, quiero crear mi perfil profesional para mostrar mi especialidad y mi institucion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi cuenta fue clasificada como docente, cuando ingreso nombre profesional, especialidad e institucion, entonces el sistema crea el perfil. |
| Error | Dado que falta un dato obligatorio, cuando intento crear el perfil, entonces el sistema rechaza la creacion. |
| Alternativo exitoso | Dado que completo los datos profesionales validos, cuando confirmo el formulario, entonces el sistema registra mi perfil docente. |
| Alternativo error | Dado que la especialidad ingresada no esta permitida, cuando envio el formulario, entonces el sistema devuelve error de validacion. |

### US09 — Update teacher specialty
- **Epic**: EP-02
- **Descripcion**: Como docente, quiero actualizar mi especialidad para reflejar mi area de enseñanza actual.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi perfil docente existe, cuando selecciono una especialidad valida, entonces el sistema guarda el cambio. |
| Error | Dado que la especialidad no esta permitida, cuando intento actualizar, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que elijo una nueva especialidad valida, cuando confirmo la edicion, entonces el sistema actualiza el perfil. |
| Alternativo error | Dado que mi perfil docente no existe, cuando intento modificarlo, entonces el sistema responde que el recurso no fue encontrado. |

### US10 — Create organization profile
- **Epic**: EP-02
- **Descripcion**: Como organizacion, quiero crear mi perfil institucional para registrar mi academia.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi cuenta fue clasificada como organizacion, cuando ingreso nombre, sedes y descripcion institucional, entonces el sistema crea el perfil. |
| Error | Dado que falta un dato obligatorio, cuando intento crear el perfil, entonces el sistema rechaza la creacion. |
| Alternativo exitoso | Dado que completo los datos institucionales validos, cuando confirmo el formulario, entonces el sistema registra mi perfil de organizacion. |
| Alternativo error | Dado que el nombre institucional ya existe, cuando intento registrarlo, entonces el sistema devuelve un error de duplicidad. |

### US11 — Register academic offering
- **Epic**: EP-03
- **Descripcion**: Como organizacion, quiero registrar mi oferta academica para mostrar mis ciclos de preparacion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que mi perfil institucional existe, cuando agrego ciclo, modalidad e intensidad, entonces el sistema guarda la oferta. |
| Error | Dado que el contenido esta incompleto o no cumple formato, cuando envio el registro, entonces el sistema lo rechaza. |
| Alternativo exitoso | Dado que ingreso un nuevo ciclo academico valido, cuando confirmo el registro, entonces el sistema publica la oferta. |
| Alternativo error | Dado que intento registrar una oferta duplicada, cuando envio la solicitud, entonces el sistema la rechaza por duplicidad. |

### US12 — Upload academic PDF resource
- **Epic**: EP-04
- **Descripcion**: Como usuario autenticado, quiero subir un PDF academico para compartir material de estudio.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el archivo es PDF y cumple el tamaño permitido, cuando lo subo, entonces el sistema registra el recurso. |
| Error | Dado que el archivo no es PDF o excede el tamano permitido, cuando intento subirlo, entonces el sistema lo rechaza. |
| Alternativo exitoso | Dado que selecciono un PDF valido, cuando confirmo la carga, entonces el sistema almacena el archivo y genera su referencia. |
| Alternativo error | Dado que el archivo esta corrupto, cuando intento subirlo, entonces el sistema devuelve un error de carga. |

### US13 — Register resource metadata
- **Epic**: EP-04
- **Descripcion**: Como usuario autenticado, quiero registrar los metadatos de un recurso para que sea buscable.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces                                                                                                              |
|---|---------------------------------------------------------------------------------------------------------------------------------------|
| Exitoso | Dado que completo titulo, institucion, curso, anio y categoria, cuando guardo el recurso, entonces el sistema registra los metadatos. |
| Error | Dado que falta un metadato obligatorio, cuando intento guardar, entonces el sistema no crea el registro.                              |
| Alternativo exitoso | Dado que ingreso metadatos validos, cuando confirmo el formulario, entonces el sistema deja el recurso listo para busqueda.           |
| Alternativo error | Dado que el curso o el año no cumplen formato, cuando envio el formulario, entonces el sistema devuelve error de validacion.          |

### US14 — Search resources by filters
- **Epic**: EP-04
- **Descripcion**: Como usuario, quiero buscar recursos por filtros para encontrar material especifico.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que aplico filtros validos, cuando ejecuto la busqueda, entonces el sistema devuelve coincidencias. |
| Error | Dado que ingreso un parametro de busqueda con formato invalido, cuando ejecuto la busqueda, entonces el sistema devuelve un error de validacion. |
| Alternativo exitoso | Dado que aplico filtros validos pero no existen recursos que coincidan, cuando ejecuto la busqueda, entonces el sistema devuelve una lista vacia sin error. |
| Alternativo error | Dado que ingreso filtros inconsistentes, cuando busco, entonces el sistema devuelve un error de validacion. |

### US15 — Download academic resource
- **Epic**: EP-04
- **Descripcion**: Como usuario, quiero descargar un recurso academico para estudiarlo sin conexion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces                                                                                                                |
|---|-----------------------------------------------------------------------------------------------------------------------------------------|
| Exitoso | Dado que el recurso existe y tengo puntos suficientes, cuando solicito la descarga, entonces el sistema entrega el archivo.             |
| Error | Dado que el recurso no existe o no tengo puntos suficientes, cuando intento descargarlo, entonces el sistema rechaza la operacion.      |
| Alternativo exitoso | Dado que el archivo esta disponible, cuando confirmo la descarga, entonces el sistema inicia la transferencia correctamente.            |
| Alternativo error | Dado que el archivo no esta disponible temporalmente, cuando intento descargarlo, entonces el sistema devuelve error de disponibilidad. |

### US16 — Create forum thread
- **Epic**: EP-05
- **Descripcion**: Como usuario autenticado, quiero crear un hilo de foro sobre una duda concreta para iniciar una discusion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el texto del hilo es valido, cuando envio la publicacion, entonces el sistema crea el foro. |
| Error | Dado que el texto esta vacio, cuando intento publicarlo, entonces el sistema rechaza la accion. |
| Alternativo exitoso | Dado que escribo una duda concreta, cuando confirmo la publicacion, entonces el sistema registra el hilo correctamente. |
| Alternativo error | Dado que no estoy autenticado, cuando intento crear el hilo, entonces el sistema rechaza el acceso. |

### US17 — Reply to forum thread
- **Epic**: EP-05
- **Descripcion**: Como usuario autenticado, quiero responder un hilo para aportar una solucion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el hilo existe y mi respuesta es valida, cuando la envio, entonces el sistema la publica. |
| Error | Dado que la respuesta esta vacia, cuando intento responder, entonces el sistema rechaza la accion. |
| Alternativo exitoso | Dado que redacto una respuesta clara, cuando confirmo el envio, entonces el sistema la agrega al hilo. |
| Alternativo error | Dado que el hilo esta cerrado, cuando intento responder, entonces el sistema devuelve un error de estado. |

### US18 — Close forum thread
- **Epic**: EP-05
- **Descripcion**: Como autor del hilo, quiero cerrar el foro para dar por terminada la discusion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que soy el autor del hilo, cuando marco el foro como cerrado, entonces el sistema cambia su estado. |
| Error | Dado que el hilo no es mio, cuando intento cerrarlo, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que la discusion ya no requiere mas aportes, cuando confirmo el cierre, entonces el sistema bloquea nuevas respuestas. |
| Alternativo error | Dado que el hilo no existe, cuando intento cerrarlo, entonces el sistema responde que el recurso no fue encontrado. |

### US19 — Report content
- **Epic**: EP-06
- **Descripcion**: Como usuario autenticado, quiero reportar contenido para que sea revisado por moderacion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que ingreso un motivo valido, cuando envio el reporte, entonces el sistema lo registra. |
| Error | Dado que no indico motivo, cuando intento reportar, entonces el sistema rechaza la solicitud. |
| Alternativo exitoso | Dado que selecciono una categoria de reporte correcta, cuando confirmo el envio, entonces el sistema guarda el caso para revision. |
| Alternativo error | Dado que intento reportar contenido inexistente, cuando envio la solicitud, entonces el sistema responde que el recurso no fue encontrado. |

### US20 — Resolve report
- **Epic**: EP-06
- **Descripcion**: Como moderador autorizado, quiero resolver un reporte para cerrar el caso.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo permisos de moderacion, cuando selecciono una resolucion, entonces el sistema cambia el estado del reporte. |
| Error | Dado que no tengo permisos, cuando intento resolverlo, entonces el sistema rechaza la accion. |
| Alternativo exitoso | Dado que reviso el caso y selecciono una decision valida, cuando confirmo la resolucion, entonces el sistema cierra el reporte. |
| Alternativo error | Dado que el reporte ya fue resuelto, cuando intento modificarlo, entonces el sistema rechaza el cambio por estado invalido. |

### US21 — Request teacher verification
- **Epic**: EP-07
- **Descripcion**: Como docente, quiero solicitar verificacion para obtener un cheque de usuario verificado.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que subo una credencial valida, cuando envio la solicitud, entonces el sistema la deja en revision. |
| Error | Dado que el archivo no cumple formato o peso, cuando intento enviarlo, entonces el sistema rechaza la solicitud. |
| Alternativo exitoso | Dado que adjunto mi documento profesional valido, cuando confirmo la peticion, entonces el sistema registra el estado pendiente. |
| Alternativo error | Dado que mi solicitud ya existe, cuando intento enviar otra, entonces el sistema la rechaza por duplicidad. |

### US22 — Request organization verification
- **Epic**: EP-07
- **Descripcion**: Como organizacion, quiero solicitar verificacion para obtener un logo institucional personalizado.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que subo la documentacion valida, cuando envio la solicitud, entonces el sistema la deja en revision. |
| Error | Dado que la documentacion es invalida o incompleta, cuando intento enviarla, entonces el sistema rechaza la solicitud. |
| Alternativo exitoso | Dado que completo los documentos requeridos, cuando confirmo la peticion, entonces el sistema registra la verificacion pendiente. |
| Alternativo error | Dado que ya existe una solicitud activa, cuando intento crear otra, entonces el sistema la rechaza por duplicidad. |

### US23 — Activate premium subscription
- **Epic**: EP-08
- **Descripcion**: Como usuario, quiero activar una suscripcion premium para acceder a beneficios adicionales.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que completo el pago, cuando se procesa correctamente, entonces el sistema activa la suscripcion. |
| Error | Dado que el pago falla, cuando intento activar el plan, entonces el sistema no lo habilita. |
| Alternativo exitoso | Dado que selecciono un plan premium valido, cuando confirmo el cobro, entonces el sistema deja mi cuenta con acceso premium. |
| Alternativo error | Dado que mi cuenta ya tiene una suscripcion activa, cuando intento contratar otra, entonces el sistema rechaza la operacion. |

### US24 — Buy coin package
- **Epic**: EP-08
- **Descripcion**: Como usuario, quiero comprar un paquete de monedas para usar beneficios de la plataforma.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el pago se confirma, cuando compro el paquete, entonces el sistema acredita las monedas. |
| Error | Dado que el pago falla, cuando intento comprar, entonces el sistema no acredita saldo. |
| Alternativo exitoso | Dado que elijo un paquete disponible, cuando confirmo la compra, entonces el sistema aumenta mi saldo de monedas. |
| Alternativo error | Dado que el paquete no existe, cuando intento comprarlo, entonces el sistema rechaza la operacion. |

### US25 — View pending notifications
- **Epic**: EP-09
- **Descripcion**: Como usuario autenticado, quiero consultar mis notificaciones pendientes para revisar actividad reciente.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo notificaciones pendientes, cuando abro la bandeja, entonces el sistema muestra las notificaciones no leidas ordenadas por fecha. |
| Error | Dado que no estoy autenticado, cuando intento consultar mis notificaciones, entonces el sistema responde con error de acceso. |
| Alternativo exitoso | Dado que no tengo notificaciones pendientes, cuando consulto la bandeja, entonces el sistema devuelve la lista vacia sin error. |
| Alternativo error | Dado que el servicio de notificaciones no esta disponible, cuando intento consultar, entonces el sistema devuelve un error de disponibilidad. |

### US26 — Reset password with token
- **Epic**: EP-01
- **Descripcion**: Como usuario, quiero usar el enlace de recuperacion para establecer una nueva contrasena y recuperar el acceso.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo un token de recuperacion valido y vigente, cuando establezco una nueva contrasena valida, entonces el sistema actualiza la credencial y marca el token como usado. |
| Error | Dado que el token ha expirado o ya fue utilizado, cuando intento acceder al formulario de restablecimiento, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que ingreso una contrasena que cumple la politica de seguridad, cuando confirmo el cambio, entonces el sistema guarda la nueva credencial y cierra cualquier sesion activa anterior. |
| Alternativo error | Dado que la nueva contrasena no cumple la politica de seguridad, cuando intento confirmar el cambio, entonces el sistema devuelve un error de validacion. |

### US27 — React to forum content
- **Epic**: EP-05
- **Descripcion**: Como usuario autenticado, quiero reaccionar a un hilo, respuesta o comentario para expresar mi opinion sobre el contenido.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el contenido existe y estoy autenticado, cuando selecciono una reaccion valida, entonces el sistema registra mi reaccion. |
| Error | Dado que el contenido no existe, cuando intento reaccionar, entonces el sistema responde que el recurso no fue encontrado. |
| Alternativo exitoso | Dado que ya registre una reaccion al mismo contenido, cuando selecciono la misma reaccion nuevamente, entonces el sistema elimina mi reaccion anterior. |
| Alternativo error | Dado que no estoy autenticado, cuando intento reaccionar a un contenido, entonces el sistema rechaza el acceso. |

### US28 — Comment on forum answer
- **Epic**: EP-05
- **Descripcion**: Como usuario autenticado, quiero comentar una respuesta del foro para agregar contexto o aclaracion.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que la respuesta existe y mi comentario tiene contenido valido, cuando lo envio, entonces el sistema lo publica. |
| Error | Dado que el comentario esta vacio, cuando intento enviarlo, entonces el sistema rechaza la accion. |
| Alternativo exitoso | Dado que redacto un comentario con informacion adicional valida, cuando confirmo el envio, entonces el sistema lo agrega correctamente a la respuesta. |
| Alternativo error | Dado que la respuesta ya no existe, cuando intento comentarla, entonces el sistema responde que el recurso no fue encontrado. |

### US29 — Follow a user
- **Epic**: EP-05
- **Descripcion**: Como usuario autenticado, quiero seguir a otro usuario para ver su actividad en la comunidad.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el usuario objetivo existe y aun no lo sigo, cuando envio la solicitud de seguimiento, entonces el sistema registra la relacion. |
| Error | Dado que intento seguirme a mi mismo, cuando envio la solicitud, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que ya sigo al usuario, cuando cancelo el seguimiento, entonces el sistema elimina la relacion de seguimiento. |
| Alternativo error | Dado que el usuario objetivo no existe, cuando intento seguirlo, entonces el sistema responde que el recurso no fue encontrado. |

### US30 — Earn experience points
- **Epic**: EP-10
- **Descripcion**: Como usuario autenticado, quiero acumular puntos de experiencia al contribuir en la plataforma para aumentar mi nivel.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que realizo una accion recompensada como publicar un recurso o responder un hilo, cuando el sistema procesa la accion, entonces registra los puntos correspondientes en mi historial. |
| Error | Dado que la accion fuente no existe o fue eliminada, cuando el sistema intenta registrar puntos, entonces descarta la operacion sin generar inconsistencia. |
| Alternativo exitoso | Dado que acumulo puntos suficientes para subir de nivel, cuando el sistema recalcula mi progreso, entonces actualiza mi nivel actual. |
| Alternativo error | Dado que la misma accion ya genero puntos anteriormente, cuando el sistema evalua nuevamente, entonces no registra duplicados. |

### US31 — View personal level and progress
- **Epic**: EP-10
- **Descripcion**: Como usuario autenticado, quiero consultar mi nivel actual y progreso de experiencia para conocer mi avance en la plataforma.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo un registro de progreso activo, cuando consulto mi perfil de gamificacion, entonces el sistema muestra mi nivel, experiencia acumulada y porcentaje de avance. |
| Error | Dado que no estoy autenticado, cuando intento consultar el progreso, entonces el sistema rechaza el acceso. |
| Alternativo exitoso | Dado que soy un usuario nuevo sin experiencia acumulada, cuando consulto mi progreso, entonces el sistema muestra el nivel inicial con cero puntos. |
| Alternativo error | Dado que el registro de progreso aun no existe, cuando lo consulto, entonces el sistema lo inicializa y devuelve el estado base. |

### US32 — Earn and view badges
- **Epic**: EP-10
- **Descripcion**: Como usuario autenticado, quiero ganar insignias por hitos de participacion y visualizarlas en mi perfil.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que alcanzo el hito requerido por una insignia, cuando el sistema evalua mi actividad, entonces acredita la insignia en mi perfil. |
| Error | Dado que una insignia ya fue acreditada a mi cuenta, cuando el sistema intenta asignarla nuevamente, entonces descarta la operacion por duplicidad. |
| Alternativo exitoso | Dado que consulto mis insignias ganadas, cuando reviso el listado, entonces el sistema muestra cada insignia con su nombre y fecha de obtencion. |
| Alternativo error | Dado que el hito requerido aun no fue alcanzado, cuando el sistema evalua mi actividad, entonces no acredita la insignia y no genera error. |

### US34 — Provide academic feedback to student
- **Epic**: EP-11
- **Descripcion**: Como docente, quiero registrar retroalimentacion academica formal sobre el desempeño de un estudiante para que quede documentada en el sistema.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que soy un docente registrado y el estudiante existe, cuando envio retroalimentacion con cuerpo de texto valido, entonces el sistema la registra y la asocia al estudiante receptor. |
| Error | Dado que el cuerpo de retroalimentacion esta vacio, cuando intento enviarla, entonces el sistema rechaza la operacion por validacion. |
| Alternativo exitoso | Dado que incluyo una puntuacion junto al comentario de retroalimentacion, cuando confirmo el envio, entonces el sistema guarda ambos datos correctamente. |
| Alternativo error | Dado que el estudiante objetivo no existe, cuando intento enviar retroalimentacion, entonces el sistema responde que el recurso no fue encontrado. |

### US35 — View received academic feedback
- **Epic**: EP-11
- **Descripcion**: Como estudiante, quiero consultar la retroalimentacion academica que he recibido para conocer la evaluacion de mis docentes.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo retroalimentacion registrada, cuando consulto mi historial de retroalimentacion, entonces el sistema devuelve el listado ordenado por fecha descendente. |
| Error | Dado que no estoy autenticado, cuando intento consultar mi retroalimentacion, entonces el sistema rechaza el acceso. |
| Alternativo exitoso | Dado que no tengo retroalimentacion registrada aun, cuando consulto el historial, entonces el sistema devuelve una lista vacia sin error. |
| Alternativo error | Dado que intento modificar una retroalimentacion recibida, cuando envio el cambio, entonces el sistema rechaza la operacion porque las entradas son inmutables. |

### US33 — Create academy
- **Epic**: EP-03
- **Descripcion**: Como organizacion, quiero registrar mi academia en la plataforma para publicar oferta educativa.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que tengo un perfil de organizacion activo, cuando ingreso nombre, descripcion y datos de contacto de la academia, entonces el sistema crea el registro y lo deja activo. |
| Error | Dado que falta el nombre de la academia, cuando intento registrarla, entonces el sistema rechaza la creacion. |
| Alternativo exitoso | Dado que completo todos los datos requeridos, cuando confirmo el registro, entonces el sistema deja la academia lista para publicar sedes y oferta academica. |
| Alternativo error | Dado que ya existe una academia registrada con ese nombre por la misma organizacion, cuando intento crearla, entonces el sistema la rechaza por duplicidad. |

### US36 — Register campus for academy
- **Epic**: EP-03
- **Descripcion**: Como organizacion, quiero registrar sedes fisicas de mi academia para que los estudiantes conozcan los puntos de atencion disponibles.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que soy la organizacion propietaria de la academia, cuando registro una sede con nombre, direccion y ciudad validos, entonces el sistema la crea y la asocia a la academia. |
| Error | Dado que omito un campo obligatorio como nombre, direccion o ciudad, cuando intento registrar la sede, entonces el sistema rechaza la operacion por validacion. |
| Alternativo exitoso | Dado que ya tengo sedes registradas, cuando agrego una nueva con nombre distinto, entonces el sistema la crea sin afectar las existentes. |
| Alternativo error | Dado que ya existe una sede con ese nombre en la misma academia, cuando intento registrarla, entonces el sistema la rechaza por duplicidad. |

### US37 — Associate teacher to academy
- **Epic**: EP-03
- **Descripcion**: Como organizacion, quiero asociar docentes a mi academia para mostrar el plantel de ensenanza disponible a los estudiantes.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que soy la organizacion propietaria de la academia y el perfil docente indicado existe, cuando registro la asociacion, entonces el sistema la crea correctamente. |
| Error | Dado que el perfil indicado no existe o no es de tipo docente, cuando intento asociarlo, entonces el sistema rechaza la operacion. |
| Alternativo exitoso | Dado que la academia ya tiene docentes asociados, cuando asocio uno nuevo valido, entonces el sistema lo agrega sin afectar las asociaciones existentes. |
| Alternativo error | Dado que el docente ya esta asociado a la misma academia, cuando intento registrar la asociacion nuevamente, entonces el sistema la rechaza por duplicidad. |

### US38 — Submit appeal for moderation decision
- **Epic**: EP-06
- **Descripcion**: Como usuario autenticado, quiero presentar una apelacion sobre una decision de moderacion para que mi caso sea revisado nuevamente.
- **Criterios de aceptacion**:

| Escenario | Dado / Cuando / Entonces |
|---|---|
| Exitoso | Dado que el reporte existe y no tengo una apelacion activa para ese reporte, cuando presento la apelacion con un motivo valido, entonces el sistema la registra con estado OPEN. |
| Error | Dado que omito el motivo de la apelacion, cuando intento presentarla, entonces el sistema rechaza la operacion por validacion. |
| Alternativo exitoso | Dado que presento mi apelacion correctamente, cuando consulto su estado, entonces el sistema la muestra como pendiente de revision. |
| Alternativo error | Dado que ya presente una apelacion para el mismo reporte, cuando intento presentar otra, entonces el sistema la rechaza por duplicidad. |

## 3. Reglas de Negocio

### Auth
- RN-01: No puede existir mas de una cuenta con el mismo correo.
- RN-02: Cada usuario debe tener exactamente un rol activo.
- RN-03: La contrasena debe almacenarse cifrada.
- RN-04: Los enlaces de recuperacion deben expirar.

### Profile
- RN-05: Un usuario solo puede tener un perfil base.
- RN-06: Los campos comunes obligatorios no pueden quedar vacios.
- RN-07: Un cambio de tipo de cuenta debe ser consistente con el perfil creado.

### Academy
- RN-08: Un estudiante solo puede tener un perfil academico principal.
- RN-09: Un docente solo puede tener un perfil profesional.
- RN-10: Una organizacion solo puede tener un perfil institucional activo.
- RN-11: Cada perfil debe registrar solo los campos que le corresponden.

### Library
- RN-12: Todo recurso debe registrar titulo, institucion, curso, anio y categoria.
- RN-13: Solo se aceptan archivos con formatos permitidos.
- RN-14: No se permite publicar un recurso duplicado identico.
- RN-15: La descarga depende del permiso del usuario y del estado del recurso.

### Forum
- RN-16: Un hilo debe estar asociado a un tema concreto.
- RN-17: Un hilo solo puede tener un estado abierto o cerrado.
- RN-18: Solo el autor del hilo puede cerrarlo.
- RN-19: Toda respuesta debe pertenecer a un hilo existente.

### Moderation
- RN-20: Todo reporte debe incluir un motivo.
- RN-21: Solo moderadores autorizados pueden resolver reportes.
- RN-22: Toda accion de moderacion debe generar auditoria.

### Verification
- RN-23: Toda solicitud de verificacion requiere documento valido.
- RN-24: Solo una solicitud de verificacion puede estar activa por entidad.
- RN-25: La verificacion del docente y de la organizacion se resuelven por flujos distintos.

### Billing
- RN-26: Un usuario solo puede tener una suscripcion premium activa a la vez.
- RN-27: El acceso premium depende de una suscripcion vigente o de una compra validada.
- RN-28: Las compras fallidas no deben modificar el saldo ni el estado del plan.

### Notifications
- RN-29: Las notificaciones solo se generan por eventos definidos por el negocio.
- RN-30: Toda notificacion debe tener estado leido o no leido.

### Gamification
- RN-31: Los puntos de experiencia se generan unicamente por acciones definidas por el sistema.
- RN-32: El nivel del usuario se recalcula automaticamente al acumular experiencia suficiente.
- RN-33: Una insignia no puede asignarse mas de una vez al mismo usuario.
- RN-34: El historial de puntos de experiencia es de solo lectura y no puede modificarse manualmente.
- RN-35: Los puntos de experiencia y las monedas virtuales son sistemas independientes.

### Feedback
- RN-36: Solo un usuario con rol TEACHER o ADMIN puede emitir retroalimentacion academica formal hacia un estudiante.
- RN-37: Una entrada de retroalimentacion no puede modificarse ni eliminarse una vez registrada.
- RN-38: El estudiante receptor puede consultar su retroalimentacion recibida pero no editarla ni eliminarla.

### Sedes (EP-03)
- RN-39: El nombre de sede debe ser unico por academia.
- RN-40: Solo la organizacion propietaria de la academia puede registrar sedes en ella.

### Asociacion Docente-Academia (EP-03)
- RN-41: Un docente puede asociarse a multiples academias; una academia puede tener multiples docentes.
- RN-42: Una asociacion docente-academia no puede registrarse mas de una vez.

### Apelaciones (EP-06)
- RN-43: Solo puede existir una apelacion activa por usuario por reporte.
- RN-44: El motivo de la apelacion es obligatorio.

## 4. Bounded Contexts

| Bounded Context | Descripcion |
|---|---|
| auth | Gestiona el registro, inicio de sesion y recuperacion de acceso de los usuarios. |
| profile | Gestiona el perfil base y los perfiles especificos de cada tipo de usuario. |
| academy | Gestiona las academias de preparacion, sus sedes, programas y ciclos de estudio. |
| library | Gestiona la publicacion, clasificacion, busqueda y descarga de recursos academicos. |
| forum | Gestiona los hilos de discusion y las respuestas de la comunidad. |
| moderation | Gestiona reportes, revisiones y resolucion de incidencias de contenido. |
| verification | Gestiona la solicitud y aprobacion de verificacion de docentes y organizaciones. |
| billing | Gestiona suscripciones, pagos y compra de beneficios premium. |
| notifications | Gestiona las notificaciones generadas por eventos del sistema. |
| gamification | Gestiona la acumulacion de puntos de experiencia, niveles, insignias y monedas del usuario. |
| feedback | Gestiona la retroalimentacion academica formal emitida por docentes hacia estudiantes. |

## 5. Relacion Epics → Bounded Contexts

Cada Epic agrupa funcionalidades de una sola capacidad de negocio. Ese mismo limite define el modulo en el codigo y la estructura de la API.

| Epic | Título | Bounded Context | Paquete en código |
|---|---|---|---|
| EP-01 | Auth | auth | `auth/` |
| EP-02 | Profile | profile | `profile/` |
| EP-03 | Academy | academy | `academy/` |
| EP-04 | Library | library | `library/` |
| EP-05 | Forum | forum | `forum/` |
| EP-06 | Moderation | moderation | `moderation/` |
| EP-07 | Verification | verification | `verification/` |
| EP-08 | Billing | billing | `billing/` |
| EP-09 | Notifications | notifications | `notifications/` |
| EP-10 | Gamification | gamification | `gamification/` |
| EP-11 | Feedback | feedback | `feedback/` |
