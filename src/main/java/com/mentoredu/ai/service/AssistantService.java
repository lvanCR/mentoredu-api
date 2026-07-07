package com.mentoredu.ai.service;

import com.mentoredu.ai.dto.SolutionInsight;
import com.mentoredu.ai.dto.SolutionReportResponse;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.catalog.model.Course;
import com.mentoredu.catalog.repository.CourseRepository;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.model.ResourceType;
import com.mentoredu.library.repository.ResourceRepository;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.service.ISolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final int MAX_SUGGESTIONS = 6;
    private static final int RECENT_RESOURCES_SAMPLE = 30;
    private static final int MAX_PDF_TEXT_CHARS = 4_000;
    private static final HttpClient PDF_HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(6))
        .build();

    private static final List<String> FALLBACK_SUGGESTIONS = List.of(
        "¿Qué tipos de recursos hay disponibles?",
        "Busca exámenes recientes",
        "Busca guías de estudio"
    );

    private static final Map<ResourceType, String> TYPE_LABELS = new EnumMap<>(ResourceType.class);
    static {
        TYPE_LABELS.put(ResourceType.EXAMEN_COMPLETO, "exámenes completos");
        TYPE_LABELS.put(ResourceType.EXAMEN_SECCION, "exámenes por sección");
        TYPE_LABELS.put(ResourceType.GUIA, "guías");
        TYPE_LABELS.put(ResourceType.APUNTES, "apuntes");
        TYPE_LABELS.put(ResourceType.PRACTICA, "prácticas");
        TYPE_LABELS.put(ResourceType.OTRO, "recursos");
    }

    private final ChatClient assistantChatClient;
    private final UserService userService;
    private final ChatModel chatModel;
    private final ISolutionService solutionService;
    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;

    @Value("${app.file-service.base-url:http://localhost:8081}")
    private String fileServiceBaseUrl;

    public String chat(String message, String email) {
        User user = userService.findByEmailOrThrow(email);
        String roleContext = "Rol de la cuenta autenticada: " + user.getRole().getName() + ". "
            + "Adapta la respuesta a ese rol; no indiques acciones que ese rol no puede realizar.";

        return assistantChatClient.prompt()
            .user(roleContext + "\n\nConsulta: " + message)
            .call()
            .content();
    }

    public List<String> suggestions() {
        List<Resource> recentResources = resourceRepository
            .search(null, null, null, null, null, null, null, null, PageRequest.of(0, RECENT_RESOURCES_SAMPLE))
            .getContent();

        if (recentResources.isEmpty()) {
            return FALLBACK_SUGGESTIONS;
        }

        Map<String, Resource> distinctByTypeAndCourse = new LinkedHashMap<>();
        for (Resource resource : recentResources) {
            String key = resource.getResourceType() + "|" + resource.getCourseId();
            distinctByTypeAndCourse.putIfAbsent(key, resource);
        }

        List<UUID> courseIds = distinctByTypeAndCourse.values().stream()
            .map(Resource::getCourseId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        Map<UUID, String> courseNamesById = courseRepository.findAllById(courseIds).stream()
            .collect(Collectors.toMap(Course::getId, Course::getName));

        return distinctByTypeAndCourse.values().stream()
            .limit(MAX_SUGGESTIONS)
            .map(resource -> toSuggestion(resource, courseNamesById))
            .toList();
    }

    private String toSuggestion(Resource resource, Map<UUID, String> courseNamesById) {
        String typeLabel = TYPE_LABELS.get(resource.getResourceType());
        String courseName = resource.getCourseId() != null ? courseNamesById.get(resource.getCourseId()) : null;

        // La búsqueda del asistente es texto libre sobre title/description (no filtra por course_id),
        // así que solo se puede prometer un curso en la sugerencia si su nombre aparece literalmente
        // en ese recurso — si no, la frase generaría cero resultados.
        boolean courseNameIsSearchable = courseName != null
            && (containsNormalized(resource.getTitle(), courseName) || containsNormalized(resource.getDescription(), courseName));

        return courseNameIsSearchable
            ? "Busca " + typeLabel + " de " + courseName
            : "Busca " + typeLabel + " disponibles";
    }

    private boolean containsNormalized(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        return normalize(haystack).contains(normalize(needle));
    }

    private String normalize(String text) {
        String stripped = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return stripped.toLowerCase();
    }

    public SolutionReportResponse report(UUID resourceId, String requesterEmail) {
        return report(resourceId, requesterEmail, null);
    }

    public SolutionReportResponse report(UUID resourceId, String requesterEmail, List<UUID> solutionIds) {
        List<SolutionResponse> solutions = solutionService.listByResource(resourceId, requesterEmail);
        if (solutionIds != null && !solutionIds.isEmpty()) {
            var selected = new java.util.HashSet<>(solutionIds);
            solutions = solutions.stream()
                .filter(solution -> selected.contains(solution.id()))
                .toList();
        }

        SolutionInsight insight = ChatClient.create(chatModel)
            .prompt()
            .system("""
                You are a MentorEdu academic analyst. Given the selected list of student solutions for an exercise,
                write a brief summary (resumen) and a recommendation (recomendacion) for the teacher.
                Use ONLY the provided data. Do not invent numbers or names.
                If extracted PDF text is available, evaluate the student's actual answer using that text.
                If a PDF has no extractable text, say that the teacher should review that file manually.
                Write both fields in Spanish.
                """)
            .user(solutions.stream().map(this::solutionForReport).collect(Collectors.joining("\n\n")))
            .call()
            .entity(SolutionInsight.class);

        return new SolutionReportResponse(solutions, insight);
    }

    private String solutionForReport(SolutionResponse solution) {
        StringBuilder builder = new StringBuilder();
        builder.append("Alumno: ").append(solution.studentName()).append('\n');
        builder.append("Estado: ").append(solution.status()).append('\n');
        builder.append("Enviado: ").append(solution.submittedAt()).append('\n');
        if (solution.content() != null && !solution.content().isBlank()) {
            builder.append("Respuesta escrita: ").append(solution.content()).append('\n');
        } else {
            builder.append("Respuesta escrita: sin texto escrito en formulario.\n");
        }

        if (solution.fileUrl() != null && !solution.fileUrl().isBlank()) {
            builder.append("Texto extraido del PDF:\n")
                .append(extractPdfText(solution.fileUrl()))
                .append('\n');
        } else {
            builder.append("PDF adjunto: no enviado.\n");
        }

        return builder.toString();
    }

    private String extractPdfText(String fileUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(resolveFileUrl(fileUrl)))
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();
            byte[] bytes = PDF_HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                String text = new PDFTextStripper().getText(document).trim();
                if (text.isBlank()) {
                    return "[PDF enviado, pero no contiene texto extraible. Puede ser escaneado o imagen.]";
                }
                if (text.length() > MAX_PDF_TEXT_CHARS) {
                    return text.substring(0, MAX_PDF_TEXT_CHARS) + "\n[Texto truncado para el analisis.]";
                }
                return text;
            }
        } catch (Exception ex) {
            return "[No se pudo extraer texto del PDF: " + ex.getClass().getSimpleName() + "]";
        }
    }

    private String resolveFileUrl(String fileUrl) {
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            return fileUrl;
        }
        String path = fileUrl.startsWith("/") ? fileUrl : "/" + fileUrl;
        return fileServiceBaseUrl + path;
    }
}
