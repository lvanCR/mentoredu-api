package com.mentoredu.config;

import com.mentoredu.config.exception.NotAuthenticatedException;
import com.mentoredu.auth.exception.EmailAlreadyExistsException;
import com.mentoredu.auth.exception.EmailNotFoundException;
import com.mentoredu.auth.exception.InvalidCredentialsException;
import com.mentoredu.auth.exception.InvalidTokenException;
import com.mentoredu.auth.exception.UserNotFoundException;
import com.mentoredu.catalog.exception.AreaNotFoundException;
import com.mentoredu.catalog.exception.CareerNotFoundException;
import com.mentoredu.catalog.exception.CourseNotFoundException;
import com.mentoredu.catalog.exception.DuplicateCourseException;
import com.mentoredu.catalog.exception.DuplicateUniversityException;
import com.mentoredu.catalog.exception.UniversityNotFoundException;
import com.mentoredu.community.exception.AcademyNotFoundException;
import com.mentoredu.community.exception.AcademyProfileRequiredException;
import com.mentoredu.community.exception.AlreadyFollowingException;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.exception.DuplicateReportException;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.NotAssociationOwnerException;
import com.mentoredu.community.exception.NotificationNotFoundException;
import com.mentoredu.community.exception.ReportAlreadyResolvedException;
import com.mentoredu.community.exception.ReportNotFoundException;
import com.mentoredu.community.exception.ReportTargetNotFoundException;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.community.exception.TeacherProfileRequiredException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.forum.exception.AnswerNotFoundException;
import com.mentoredu.forum.exception.CommentNotFoundException;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.library.exception.AceptaResolucionesPracticaOnlyException;
import com.mentoredu.library.exception.CareerAreaMismatchException;
import com.mentoredu.library.exception.CourseIdRequiredException;
import com.mentoredu.library.exception.DuplicateResourceException;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import com.mentoredu.library.exception.ResourceAccessDeniedException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.pedagogy.exception.DuplicateSolutionException;
import com.mentoredu.pedagogy.exception.FeedbackAlreadyExistsException;
import com.mentoredu.pedagogy.exception.FeedbackNotFoundException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.profile.exception.AcademyNameAlreadyExistsException;
import com.mentoredu.profile.exception.AcademyProfileAlreadyExistsException;
import com.mentoredu.profile.exception.ProfileAlreadyExistsException;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.profile.exception.StudentProfileAlreadyExistsException;
import com.mentoredu.profile.exception.TeacherProfileAlreadyExistsException;
import com.mentoredu.profile.exception.WrongProfileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> body(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // ── Validation ────────────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String key = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            fieldErrors.put(key, error.getDefaultMessage());
        });
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now().toString());
        b.put("status", HttpStatus.BAD_REQUEST.value());
        b.put("error", "Validation failed");
        b.put("details", fieldErrors);
        return ResponseEntity.badRequest().body(b);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request",
            "Cuerpo de solicitud inválido o tipo de dato incorrecto"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request",
            "Valor inválido para parámetro '" + ex.getName() + "': " + ex.getValue()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(body(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                "El archivo supera el tamaño máximo permitido"));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.error("IllegalStateException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ha ocurrido un error inesperado. Por favor intenta de nuevo."));
    }

    // ── Spring Security ───────────────────────────────────────────────────────
    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<Map<String, Object>> handle(NotAuthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(body(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(body(HttpStatus.FORBIDDEN, "Forbidden", "No tienes permiso para realizar esta acción"));
    }

    // ── Auth ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handle(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(EmailNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handle(InvalidTokenException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    // ── Catalog ───────────────────────────────────────────────────────────────
    @ExceptionHandler(UniversityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(UniversityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(AreaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(AreaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(CourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(CareerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(CareerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUniversityException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateUniversityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCourseException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateCourseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(ProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(ProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(StudentProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(StudentProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(TeacherProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(TeacherProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(AcademyProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(AcademyProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(AcademyNameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(AcademyNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(WrongProfileTypeException.class)
    public ResponseEntity<Map<String, Object>> handle(WrongProfileTypeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    // ── Library ───────────────────────────────────────────────────────────────
    @ExceptionHandler(CourseIdRequiredException.class)
    public ResponseEntity<Map<String, Object>> handle(CourseIdRequiredException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(AceptaResolucionesPracticaOnlyException.class)
    public ResponseEntity<Map<String, Object>> handle(AceptaResolucionesPracticaOnlyException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(CareerAreaMismatchException.class)
    public ResponseEntity<Map<String, Object>> handle(CareerAreaMismatchException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handle(ResourceAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<Map<String, Object>> handle(InvalidFileTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage()));
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handle(FileSizeLimitExceededException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    // ── Forum ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(ThreadNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(ThreadNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(ThreadClosedException.class)
    public ResponseEntity<Map<String, Object>> handle(ThreadClosedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(ThreadNotOwnedException.class)
    public ResponseEntity<Map<String, Object>> handle(ThreadNotOwnedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(AnswerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(CommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    // ── Pedagogy ──────────────────────────────────────────────────────────────
    @ExceptionHandler(SolutionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(SolutionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateSolutionException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateSolutionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(SolutionAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handle(SolutionAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(FeedbackAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handle(FeedbackAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(FeedbackNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(FeedbackNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    // ── Community ─────────────────────────────────────────────────────────────
    @ExceptionHandler(SelfFollowException.class)
    public ResponseEntity<Map<String, Object>> handle(SelfFollowException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(AlreadyFollowingException.class)
    public ResponseEntity<Map<String, Object>> handle(AlreadyFollowingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(ReportNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(ReportTargetNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(ReportTargetNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateReportException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(ReportAlreadyResolvedException.class)
    public ResponseEntity<Map<String, Object>> handle(ReportAlreadyResolvedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(VerificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(VerificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateVerificationException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateVerificationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(AssociationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(AssociationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(AcademyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(AcademyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateAssociationException.class)
    public ResponseEntity<Map<String, Object>> handle(DuplicateAssociationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(TeacherProfileRequiredException.class)
    public ResponseEntity<Map<String, Object>> handle(TeacherProfileRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(AcademyProfileRequiredException.class)
    public ResponseEntity<Map<String, Object>> handle(AcademyProfileRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(NotAssociationOwnerException.class)
    public ResponseEntity<Map<String, Object>> handle(NotAssociationOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    // ── ResponseStatusException (lanzada explícitamente desde servicios/tests) ─
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(body(status, status.getReasonPhrase(), message));
    }

    // ── Fallback ──────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ha ocurrido un error inesperado. Por favor intenta de nuevo."));
    }
}
