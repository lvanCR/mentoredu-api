package com.mentoredu.ai.tool;

import com.mentoredu.library.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceSearchTool {

    private final ResourceRepository resourceRepository;

    public record ResourceSummary(String title, String type, String fileUrl) {}

    @Tool(description = "Search for academic resources (exams, guides, notes, exercises) by keyword and optional type. "
        + "Returns up to 5 matching resources with title, type and download URL.")
    public List<ResourceSummary> searchResources(
        @ToolParam(description = "keyword to search in title or description") String query,
        @ToolParam(description = "resource type filter: EXAMEN_COMPLETO, EXAMEN_SECCION, GUIA, APUNTES, PRACTICA, OTRO. "
            + "Pass null to search all types.") String type
    ) {
        return resourceRepository
            .search(query, type, null, null, null, null, null, null, PageRequest.of(0, 5))
            .getContent()
            .stream()
            .map(r -> new ResourceSummary(r.getTitle(), r.getResourceType().name(), r.getFileUrl()))
            .toList();
    }
}
