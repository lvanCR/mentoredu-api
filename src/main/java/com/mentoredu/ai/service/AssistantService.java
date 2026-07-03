package com.mentoredu.ai.service;

import com.mentoredu.ai.dto.SolutionInsight;
import com.mentoredu.ai.dto.SolutionReportResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
    private final ChatModel chatModel;
    private final ISolutionService solutionService;
    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;

    public String chat(String message) {
        return assistantChatClient.prompt()
            .user(message)
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
        List<SolutionResponse> solutions = solutionService.listByResource(resourceId, requesterEmail);

        SolutionInsight insight = ChatClient.create(chatModel)
            .prompt()
            .system("""
                You are a MentorEdu academic analyst. Given the list of student solutions for an exercise,
                write a brief summary (resumen) and a recommendation (recomendacion) for the teacher.
                Use ONLY the provided data. Do not invent numbers or names.
                Write both fields in Spanish.
                """)
            .user(solutions.toString())
            .call()
            .entity(SolutionInsight.class);

        return new SolutionReportResponse(solutions, insight);
    }
}
