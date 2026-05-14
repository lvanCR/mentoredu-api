# Especificación de Requisitos y Diseño — MentorEdu

## 1. Épicas (Epics)

| Épica | Título | Descripción |
|-------|--------|-------------|
| EP01 | Autenticación y gestión de cuenta | Registro, inicio de sesión, recuperación de contraseña y seguridad de cuenta. |
| EP02 | Perfil y progreso personal | Gestión del perfil, visualización de actividad, puntos, niveles y documentos aportados. |
| EP03 | Repositorio de documentos | Subida, búsqueda, visualización, descarga y gestión de exámenes y materiales. |
| EP04 | Foros y colaboración | Preguntas ancladas a exámenes, respuestas, reportes, anonimato y notificaciones. |
| EP05 | Gamificación y recompensas | Puntos, niveles, monedas virtuales y ventajas para incentivar la participación. |
| EP06 | Monetización y modelo freemium | Suscripciones premium, compra de monedas y gestión de límites de descarga. |
| EP07 | Interacción social | Seguir a otros usuarios, ver actividad de colaboradores destacados. |
| EP08 | Moderación y verificación | Orden, verificación de usuarios y contenidos publicados. |

---

## 2. User Stories

### US01 – Registro de usuario
- **Épica**: EP01
- **Descripción**: Como estudiante preuniversitario, quiero registrarme con mi correo electrónico o cuenta de Google de forma sencilla y segura para acceder rápidamente a la plataforma.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que soy un estudiante nuevo, CUANDO ingreso mi correo y contraseña (o acepto usar Google) y completo los campos obligatorios, ENTONCES se crea mi cuenta y se me redirige al dashboard. |
| Error | DADO que intento registrarme, CUANDO ingreso un correo ya registrado o contraseña débil, ENTONCES el sistema muestra un mensaje de error claro y no crea la cuenta. |
| Alternativo | DADO que uso registro con Google, CUANDO otorgo permisos, ENTONCES el sistema crea automáticamente mi perfil con los datos públicos de Google. |

---

### US02 – Inicio de sesión
- **Épica**: EP01
- **Descripción**: Como usuario registrado, quiero iniciar sesión con mis credenciales para acceder a mi cuenta y mis datos.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que tengo una cuenta activa, CUANDO ingreso mi correo y contraseña correctos, ENTONCES el sistema me autentica y muestra mi perfil. |
| Error | DADO que ingreso credenciales incorrectas, CUANDO presiono "Iniciar sesión", ENTONCES el sistema muestra "Correo o contraseña inválidos" sin bloquear la cuenta. |
| Alternativo | DADO que olvidé mi contraseña, CUANDO hago clic en "¿Olvidaste tu contraseña?", ENTONCES el sistema me permite iniciar el flujo de recuperación (US20). |

---

### US03 – Edición del perfil básico
- **Épica**: EP02
- **Descripción**: Como usuario, quiero editar mi perfil (nombre, universidad deseada, carrera, etc.) para personalizar mi experiencia.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que estoy en mi perfil, CUANDO modifico mis datos y guardo cambios, ENTONCES la información se actualiza y se muestra correctamente. |
| Error | DADO que intento guardar un nombre vacío o con caracteres no permitidos, CUANDO presiono guardar, ENTONCES el sistema muestra "Campo inválido" y no actualiza el perfil. |
| Alternativo | DADO que deseo cambiar mi foto de perfil, CUANDO subo una imagen válida (JPG, PNG, <2MB), ENTONCES la imagen se actualiza y se muestra en mi perfil y en mis aportes. |

---

### US04 – Visualización del progreso personal
- **Épica**: EP02
- **Descripción**: Como estudiante, quiero ver en mi perfil los documentos compartidos, puntos acumulados, nivel y participación en foros para hacer seguimiento de mi actividad.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que tengo actividad en la plataforma, CUANDO accedo a "Mi Progreso", ENTONCES veo: número de documentos subidos, puntos totales, nivel actual, lista de preguntas y respuestas recientes. |
| Error | DADO que no tengo actividad, CUANDO accedo a "Mi Progreso", ENTONCES veo un mensaje amigable y todos los contadores en cero. |
| Alternativo | DADO que quiero ver detalle de mis documentos, CUANDO hago clic en el contador, ENTONCES se abre una lista paginada con títulos y fechas. |

---

### US05 – Anonimato en foros y documentos
- **Épica**: EP04
- **Descripción**: Como usuario que valora su privacidad, quiero publicar preguntas, respuestas o documentos de forma anónima para evitar sentirme juzgado.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que estoy creando una pregunta, CUANDO marco "Publicar anónimamente", ENTONCES la pregunta aparece como "Usuario Anónimo" para la comunidad. |
| Error | DADO que subo un documento anónimo y es reportado como spam, ENTONCES el sistema asocia el reporte al usuario real internamente, pero para la comunidad sigue siendo anónimo. |
| Alternativo | DADO que publiqué sin anonimato, CUANDO quiero editarla para hacerla anónima, ENTONCES tengo la opción de ocultar mi identidad retroactivamente. |

---

### US06 – Subida de documentos con metadatos
- **Épica**: EP03
- **Descripción**: Como estudiante, quiero subir exámenes o prácticas en PDF con campos obligatorios (universidad, año, área) para mantener el orden en el repositorio.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que tengo un PDF válido (<10MB), CUANDO selecciono el archivo, completo metadatos y presiono "Subir", ENTONCES el documento se guarda y se me notifica que está disponible. |
| Error | DADO que omito un campo obligatorio (ej. año), CUANDO intento subir, ENTONCES el sistema muestra "Completa todos los campos obligatorios" y no permite la subida. |
| Alternativo | DADO que subo un documento duplicado, CUANDO el sistema lo detecta, ENTONCES me advierte y pide confirmar si quiero subirlo igualmente. |

---

### US07 – Límites de descarga para acceso equitativo
- **Épica**: EP03
- **Descripción**: Como usuario gratuito, quiero descargar documentos hasta un límite diario para que todos tengan acceso justo.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que no superé el límite de 5 descargas diarias, CUANDO hago clic en "Descargar", ENTONCES se inicia la descarga y mi contador disminuye en 1. |
| Error | DADO que agotré las 5 descargas, CUANDO intento descargar otro documento, ENTONCES el sistema muestra "Has alcanzado tu límite diario. Espera a mañana o canjea monedas virtuales." |
| Alternativo | DADO que soy usuario premium o canjeo monedas, CUANDO intento descargar, ENTONCES no se aplican los límites diarios. |

---

### US08 – Búsqueda con filtros avanzados
- **Épica**: EP03
- **Descripción**: Como estudiante, quiero buscar documentos usando filtros claros (universidad, año, área) para encontrar rápidamente lo que necesito.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que estoy en el repositorio, CUANDO selecciono universidad "UNMSM", año "2025" y área "Química", ENTONCES se muestran solo los exámenes que coinciden. |
| Error | DADO que no hay resultados para los filtros, CUANDO presiono buscar, ENTONCES el sistema muestra "No se encontraron documentos con esos criterios." |
| Alternativo | DADO que busco una palabra clave, CUANDO escribo en el buscador libre, ENTONCES se muestran documentos que contienen esa palabra en título o metadatos. |

---

### US09 – Visor integrado de PDF (mobile-first)
- **Épica**: EP03
- **Descripción**: Como usuario, quiero visualizar documentos PDF directamente en la web, sin descargarlos, para ahorrar tiempo y datos.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que hago clic en un documento PDF, CUANDO se abre el visor, ENTONCES puedo desplazarme por páginas, hacer zoom y rotar pantalla en móviles. |
| Error | DADO que el archivo está dañado, CUANDO intento visualizarlo, ENTONCES el sistema muestra "No se puede previsualizar este archivo. Intenta descargarlo." |
| Alternativo | DADO que mi conexión es lenta, CUANDO el visor carga el PDF, ENTONCES muestra primero vista previa de baja resolución y carga páginas bajo demanda. |

---

### US10 – Reporte de contenido inapropiado
- **Épica**: EP04
- **Descripción**: Como estudiante o actuador, quiero reportar material o comentarios que infrinjan normas de la comunidad para mantener la calidad.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que visualizo contenido inapropiado, CUANDO selecciono "Reportar", elijo motivo y envío, ENTONCES el sistema confirma y marca el contenido para revisión. |
| Error | DADO que intento enviar sin seleccionar categoría, CUANDO presiono enviar, ENTONCES el sistema resalta el campo obligatorio y no permite el envío. |
| Alternativo | DADO que ya reporté ese contenido antes, CUANDO intento reportarlo de nuevo, ENTONCES el sistema informa que el reporte ya está en proceso. |

---

### US11 – Reputación de contenido (votos y ordenamiento)
- **Épica**: EP04
- **Descripción**: Como estudiante, quiero votar si un documento o respuesta fue útil y que los más votados aparezcan primero.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que leo una respuesta útil, CUANDO hago clic en 👍, ENTONCES el contador aumenta en 1 y se registra mi voto. |
| Error | DADO que intento votar dos veces, CUANDO ya voté, ENTONCES el sistema no permite duplicar el voto pero sí cambiar de positivo a negativo. |
| Alternativo | DADO que ordeno respuestas por "Más votadas", CUANDO aplico el filtro, ENTONCES las respuestas con mayor puntaje neto (👍 - 👎) aparecen primero. |

---

### US12 – Foro anclado por pregunta de examen
- **Épica**: EP04
- **Descripción**: Como usuario, quiero crear una pregunta referenciada a un examen real para recibir ayuda contextualizada.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que estoy viendo un examen, CUANDO selecciono "Preguntar sobre esta pregunta" e ingreso texto, ENTONCES se crea un hilo etiquetado con la referencia exacta (universidad, año, número de pregunta). |
| Error | DADO que no selecciono ningún examen, CUANDO creo una pregunta general, ENTONCES se permite pero sin referencia automática (se considera pregunta temática). |
| Alternativo | DADO que otro usuario responde y yo marco su respuesta como "Mejor respuesta", ENTONCES esa respuesta se fija al inicio del hilo y el usuario gana puntos extra. |

---

### US13 – Subir imágenes al responder
- **Épica**: EP04
- **Descripción**: Como estudiante, quiero adjuntar imágenes al responder una pregunta para explicar mejor mis soluciones.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que escribo una respuesta, CUANDO adjunto una imagen (JPG, PNG, <5MB), ENTONCES la imagen se muestra embebida en el texto de la respuesta. |
| Error | DADO que subo una imagen >5MB, CUANDO el sistema la rechaza, ENTONCES muestra "La imagen no debe superar los 5MB. Comprímela o usa un formato más liviano." |
| Alternativo | DADO que subo varias imágenes, CUANDO el sistema las procesa, ENTONCES se muestran en orden y se puede hacer clic para ampliarlas. |

---

### US14 – Notificaciones de actividad
- **Épica**: EP04
- **Descripción**: Como usuario, quiero recibir notificaciones (push o email) cuando alguien responda a mi pregunta o haya actividad relevante.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que tengo notificaciones por email activadas, CUANDO alguien responde mi pregunta, ENTONCES recibo un correo con extracto de la respuesta y enlace al hilo. |
| Error | DADO que el servicio de email falla, CUANDO se genera una notificación, ENTONCES el sistema reintenta hasta 3 veces y al iniciar sesión se muestra indicador visual. |
| Alternativo | DADO que no quiero email pero sí notificaciones web, CUANDO alguien responde, ENTONCES aparece un contador en el icono de campana. |

---

### US15 – Sistema de puntos acumulables
- **Épica**: EP05
- **Descripción**: Como estudiante, quiero ganar puntos por subir documentos válidos y por responder preguntas para ver reflejada mi contribución.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que subo un documento válido y no duplicado, CUANDO es aprobado, ENTONCES recibo +10 puntos. |
| Error | DADO que subo un documento reportado como spam y eliminado, CUANDO los moderadores actúan, ENTONCES se restan los puntos ganados y se aplica penalización (-5 puntos) si es reincidente. |
| Alternativo | DADO que mi respuesta recibe 3 votos positivos, CUANDO el sistema actualiza, ENTONCES recibo +2 puntos adicionales por cada voto (hasta un límite diario). |

---

### US16 – Niveles que desbloquean ventajas
- **Épica**: EP05
- **Descripción**: Como usuario activo, quiero subir de nivel (Bronce, Plata, Oro, Platino) según mis puntos y obtener ventajas para motivarme a participar más.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que alcanzo 100 puntos, CUANDO cruzo el umbral, ENTONCES subo a "Bronce" y mi límite de descarga diaria aumenta de 5 a 7. |
| Error | DADO que el sistema falla al actualizar el nivel, CUANDO el usuario sigue acumulando puntos, ENTONCES en el siguiente inicio de sesión se recalcula el nivel y se asignan ventajas retroactivamente. |
| Alternativo | DADO que mi nivel es Plata, CUANDO entro a mi perfil, ENTONCES veo la barra de progreso hacia Oro y las ventajas que obtendré. |

---

### US17 – Monedas virtuales (microtransacciones no monetarias)
- **Épica**: EP05
- **Descripción**: Como estudiante, quiero ganar monedas virtuales al participar y canjearlas por descargas adicionales o contenido exclusivo.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que mi respuesta es marcada como "Mejor respuesta", CUANDO el sistema otorga recompensa, ENTONCES recibo 5 monedas virtuales. |
| Error | DADO que intento canjear monedas sin suficiente saldo (cuesta 3 y tengo 2), ENTONCES el sistema muestra "No tienes suficientes monedas. Gana más participando." |
| Alternativo | DADO que tengo 10 monedas, CUANDO las canjeo por 2 descargas extra, ENTONCES se descuentan 6 monedas (3 por descarga) y mi límite diario se incrementa en 2. |

---

### US18 – Modelo freemium (suscripción y compra de monedas)
- **Épica**: EP06
- **Descripción**: Como usuario con alta demanda, quiero pagar una suscripción mensual para tener descargas ilimitadas y sin publicidad, o comprar monedas virtuales directamente.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que elijo la suscripción mensual (S/9.90), CUANDO completo el pago con tarjeta o Yape, ENTONCES mi cuenta se marca como premium por 30 días sin límites de descarga. |
| Error | DADO que ingreso datos de pago incorrectos, CUANDO el procesador rechaza la transacción, ENTONCES el sistema muestra "Error en el pago. Verifica tus datos o intenta con otro método." |
| Alternativo | DADO que no quiero suscribirme, CUANDO compro un paquete de monedas (ej. 50 monedas por S/10), ENTONCES las monedas se acreditan y puedo usarlas para descargas adicionales. |

---

### US19 – Función social: seguir a otros usuarios
- **Épica**: EP07
- **Descripción**: Como estudiante, quiero seguir a otros usuarios colaboradores para ver su actividad reciente, respuestas y documentos.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que visito el perfil de otro usuario, CUANDO hago clic en "Seguir", ENTONCES se agrega a mis seguidos y recibo notificaciones de sus aportes (si lo configuro). |
| Error | DADO que ya sigo a ese usuario, CUANDO vuelvo a su perfil, ENTONCES el botón muestra "Dejar de seguir" y puedo hacer clic para dejar de seguirlo. |
| Alternativo | DADO que entro a mi feed de "Actividad de seguidos", ENTONCES veo los últimos documentos y respuestas de los usuarios que sigo, ordenados por fecha. |

---

### US20 – Recuperación de contraseña
- **Épica**: EP01
- **Descripción**: Como usuario que olvidó su contraseña, quiero restablecerla mediante un enlace enviado a mi correo para recuperar el acceso.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que olvidé mi contraseña, CUANDO ingreso mi correo en "¿Olvidaste tu contraseña?" y solicito el enlace, ENTONCES recibo un correo con un enlace único válido por 1 hora para crear nueva contraseña. |
| Error | DADO que ingreso un correo no registrado, CUANDO solicito recuperación, ENTONCES el sistema muestra "No existe una cuenta con ese correo electrónico." |
| Alternativo | DADO que hago clic en el enlace vencido, CUANDO intento restablecer, ENTONCES el sistema muestra "El enlace ha expirado. Solicita uno nuevo." y no permite el cambio. |

---

### US21 – Verificar identidad (docente o academia)
- **Épica**: EP08
- **Descripción**: Como actuador (docente o academia), quiero verificar mi identidad mediante documentos oficiales para obtener una insignia de confianza.
- **Criterios de Aceptación**:

| Escenario | Dado / Cuando / Entonces |
|-----------|--------------------------|
| Exitoso | DADO que estoy en mi perfil de actuador, CUANDO subo mi DNI o credencial institucional y solicito verificación, ENTONCES mi estado cambia a "En proceso de verificación" y se me notifica al aprobar. |
| Error | DADO que intento subir el documento, CUANDO excede 5MB o formato no válido (ej. .txt), ENTONCES el sistema muestra un error con las restricciones de formato y peso. |
| Alternativo | DADO que mi solicitud es rechazada, CUANDO accedo a la sección de identidad, ENTONCES veo el motivo del rechazo y puedo cargar un nuevo archivo para reintentar. |

---

## 3. Reglas de Negocio

| Código | Regla | Tipo |
|--------|-------|------|
| RN-01 | No puede existir más de un usuario con el mismo correo. | Restricción |
| RN-02 | Todo usuario debe tener exactamente un rol principal activo. | Restricción |
| RN-03 | Un usuario solo puede tener un perfil base por cuenta. | Restricción |
| RN-04 | La contraseña debe almacenarse cifrada (BCrypt); nunca en texto plano. | Seguridad |
| RN-05 | Un usuario puede registrarse con correo o Google, pero ambos canales deben resolver al mismo identificador de cuenta. | Integridad |
| RN-06 | Solo usuarios autenticados pueden crear preguntas, respuestas o subir documentos. | Restricción |
| RN-07 | Las publicaciones anónimas deben conservar trazabilidad interna para moderación, aunque no muestren autor público. | Trazabilidad |
| RN-08 | Un usuario puede seguir a otros usuarios, pero no duplicar la misma relación de seguimiento. | Restricción |
| RN-09 | Un recurso académico debe registrar título, tipo, categoría/curso y autor o entidad responsable. | Validación |
| RN-10 | Los documentos con sello de verificación solo pueden ser publicados por usuarios, docentes o academias verificadas. | Gobernanza |
| RN-11 | Una pregunta puede tener múltiples respuestas, pero solo una puede ser marcada como aceptada. | Estado |
| RN-12 | Las acciones de moderación deben generar un registro de auditoría. | Auditoría |
| RN-13 | Toda suscripción o compra de monedas debe registrar fecha, estado y origen de la transacción. | Trazabilidad |
| RN-14 | El contenido premium solo puede ser consumido por usuarios con suscripción activa o monedas suficientes. | Acceso |
| RN-15 | Un reporte solo puede resolverse por un moderador o administrador autorizado. | Permisos |
| RN-16 | Los usuarios suspendidos no pueden publicar, responder ni seguir usuarios hasta que su estado sea restituido. | Estado |
| RN-17 | Los puntos, niveles, monedas e insignias solo pueden generarse por eventos válidos del sistema. | Integridad |
| RN-18 | Toda verificación de academia o docente debe pasar por revisión y aprobación manual o semimanual. | Control |
| RN-19 | El perfil del usuario debe permitir visualizar documentos compartidos, puntos acumulados, nivel y actividad en foros. | Funcional |
| RN-20 | Las notificaciones deben generarse por eventos relevantes: respuestas, seguimiento, moderación, recuperación de cuenta y verificación. | Evento |

---

## 4. Bounded Contexts

| Bounded Context | Responsabilidad | Entidades principales |
|----------------|-----------------|----------------------|
| Auth | Registro, login, recuperación de contraseña y control de acceso. | User, Credential, Role, Session, PasswordResetToken |
| Profile | Perfil público y académico, estado, datos personales y preferencias. | StudentProfile, ModeratorProfile, AdminProfile, TeacherProfile, AcademyProfile |
| Content Repository | Exámenes, solucionarios, materiales y metadatos de búsqueda. | AcademicResource, University, Subject, Tag, ResourceFile |
| Community | Publicaciones, preguntas, respuestas, comentarios y seguimiento social. | Thread, Answer, Comment, FollowRelation, Reaction |
| Gamification | Puntos, niveles, monedas, insignias y progreso. | PointTransaction, CoinWallet, Badge, UserBadge, LevelProgress |
| Moderation & Governance | Reportes, acciones de moderación, suspensión, apelaciones y auditoría. | Report, ModerationAction, AuditLog, Appeal |
| Verification & Partnerships | Validación de academias y docentes, badge de verificación. | VerificationRequest, VerificationDocument |
| Subscription & Monetization | Suscripciones, monedas virtuales, pagos y acceso premium. | Subscription, Plan, Payment, PremiumAccess, CoinPackage |
| Notification | Notificaciones internas y por correo. | Notification, NotificationPreference |

---

## 5. Relación Épicas → Bounded Contexts

| Épica | Bounded Contexts involucrados |
|-------|-------------------------------|
| EP01 Autenticación y seguridad | Auth, Notification |
| EP02 Perfil y personalización | Profile, Auth, Notification |
| EP03 Repositorio académico | Content Repository, Notification |
| EP04 Comunidad y participación | Community, Profile, Notification |
| EP05 Gamificación y progreso | Gamification, Profile, Notification |
| EP06 Moderación y confianza | Moderation & Governance, Verification & Partnerships, Notification |
| EP07 Monetización y acceso premium | Subscription & Monetization, Content Repository, Auth |
| EP08 Administración del sistema | Moderation & Governance, Auth, Profile, Notification |