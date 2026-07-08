package com.mentoredu.ai.tool;

import com.mentoredu.library.model.Resource;
import com.mentoredu.library.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceSearchTool {

    private static final Set<String> STOPWORDS = Set.of(
        "busco", "buscar", "busca", "contenido", "material", "recurso", "recursos",
        "universidad", "universitario", "universitaria", "de", "del", "la", "el", "los", "las",
        "un", "una", "para", "por", "con", "sobre", "pes", "pues", "pe", "en",
        "guia", "guias", "examen", "examenes", "completo", "completos", "apunte", "apuntes",
        "practica", "practicas", "ejercicio", "ejercicios", "disponible", "disponibles",
        "quiero", "necesito", "dame", "solo", "solamente", "algo", "algun", "alguna", "algunos", "algunas"
    );

    private static final Map<String, List<String>> QUERY_EXPANSIONS = Map.ofEntries(
        Map.entry("exmes", List.of("examen", "examenes")),
        Map.entry("exm", List.of("examen")),
        Map.entry("exam", List.of("examen")),
        Map.entry("examnes", List.of("examenes")),
        Map.entry("examenes", List.of("examen")),
        Map.entry("gui", List.of("guia")),
        Map.entry("guias", List.of("guia")),
        Map.entry("contenio", List.of("contenido")),
        Map.entry("conenido", List.of("contenido")),
        Map.entry("contendido", List.of("contenido")),
        Map.entry("fiscia", List.of("fisica")),
        Map.entry("fisca", List.of("fisica")),
        Map.entry("fis", List.of("fisica")),
        Map.entry("quim", List.of("quimica")),
        Map.entry("mate", List.of("matematica")),
        Map.entry("mat", List.of("matematica")),
        Map.entry("uni", List.of("UNI", "Universidad Nacional de Ingenieria")),
        Map.entry("unmsm", List.of("UNMSM", "San Marcos")),
        Map.entry("marcos", List.of("San Marcos", "UNMSM")),
        Map.entry("pucp", List.of("PUCP", "Pontificia Universidad Catolica")),
        Map.entry("catolica", List.of("PUCP", "Pontificia Universidad Catolica")),
        Map.entry("catolico", List.of("PUCP", "Pontificia Universidad Catolica"))
    );

    private final ResourceRepository resourceRepository;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public record ResourceSummary(UUID id, String title, String type, Integer year, String detailUrl) {}

    @Tool(description = "Search academic resources with typo-tolerant keywords. Searches title, description, university, area, career and course. "
        + "Returns up to 5 resources with title, type, year and frontend detail URL. Do not expose raw file URLs.")
    public List<ResourceSummary> searchResources(
        @ToolParam(description = "natural language keyword, even if it contains typos or extra words") String query,
        @ToolParam(description = "resource type filter: EXAMEN_COMPLETO, EXAMEN_SECCION, GUIA, APUNTES, PRACTICA, OTRO. "
            + "Pass null to search all types. If no exact type matches exist, the tool may return nearby resource types.") String type
    ) {
        String requestedType = normalizeType(type, query);
        LinkedHashMap<UUID, Resource> matches = new LinkedHashMap<>();

        collectMatches(matches, queryCandidates(query), requestedType);
        if (matches.isEmpty() && requestedType != null) {
            collectMatches(matches, queryCandidates(query), null);
        }
        // Solo se navega sin ninguna palabra clave si de verdad no hubo ningún match:
        // nunca se usa para "rellenar" resultados ya encontrados con recursos fuera de contexto.
        if (matches.isEmpty()) {
            resourceRepository
                .assistantSearch(null, requestedType, PageRequest.of(0, 5))
                .getContent()
                .forEach(resource -> matches.putIfAbsent(resource.getId(), resource));
        }

        return matches.values().stream()
            .limit(5)
            .map(r -> new ResourceSummary(
                r.getId(),
                r.getTitle(),
                r.getResourceType().name(),
                r.getResourceYear(),
                detailUrl(r.getId())
            ))
            .toList();
    }

    private void collectMatches(Map<UUID, Resource> matches, List<String> candidates, String type) {
        for (String candidate : candidates) {
            if (matches.size() >= 5) return;
            resourceRepository
                .assistantSearch(blankToNull(candidate), type, PageRequest.of(0, 5))
                .getContent()
                .forEach(resource -> matches.putIfAbsent(resource.getId(), resource));
        }
    }

    private List<String> queryCandidates(String query) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String normalized = normalize(query);

        if (query != null && !query.isBlank()) candidates.add(query.trim());
        if (!normalized.isBlank()) candidates.add(normalized);

        if (normalized.contains("catolica") || normalized.contains("catolico") || normalized.contains("pucp")) {
            candidates.add("catolica");
            candidates.add("pucp");
            candidates.add("pontificia");
        }
        if (containsToken(normalized, "uni")) {
            candidates.add("UNI");
            candidates.add("Universidad Nacional de Ingenieria");
        }
        if (normalized.contains("san marcos") || normalized.contains("unmsm")) {
            candidates.add("UNMSM");
            candidates.add("San Marcos");
        }
        if (normalized.contains("fisica") || normalized.contains("fis")) {
            candidates.add("fisica");
            candidates.add("cinematica");
        }
        if (normalized.contains("mate")) {
            candidates.add("matematica");
            candidates.add("razonamiento matematico");
        }

        for (String token : normalized.split("[^a-z0-9]+")) {
            if (token.length() >= 3 && !STOPWORDS.contains(token)) {
                candidates.add(token);
                QUERY_EXPANSIONS.getOrDefault(token, List.of()).forEach(candidates::add);
            }
        }

        if (candidates.isEmpty()) candidates.add(null);
        return new ArrayList<>(candidates);
    }

    private String normalizeType(String type, String query) {
        if (type != null && !type.isBlank() && !"null".equalsIgnoreCase(type)) return type;
        String normalized = normalize(query);
        if (normalized.contains("examen completo") || normalized.contains("simulacro")) return "EXAMEN_COMPLETO";
        if (normalized.contains("examen seccion") || normalized.contains("por seccion")) return "EXAMEN_SECCION";
        if (normalized.contains("guia") || normalized.contains("guias") || normalized.contains("gui ")) return "GUIA";
        if (normalized.contains("apunte") || normalized.contains("nota")) return "APUNTES";
        if (normalized.contains("practica") || normalized.contains("practicas") || normalized.contains("ejercicio")) return "PRACTICA";
        return null;
    }

    private String detailUrl(UUID id) {
        String base = frontendBaseUrl == null ? "http://localhost:4200" : frontendBaseUrl.replaceAll("/+$", "");
        return base + "/library/" + id;
    }

    private boolean containsToken(String text, String token) {
        return Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(token) + "([^a-z0-9]|$)").matcher(text).find();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String normalize(String value) {
        if (value == null) return "";
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }
}