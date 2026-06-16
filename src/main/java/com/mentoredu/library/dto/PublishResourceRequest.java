package com.mentoredu.library.dto;

import com.mentoredu.library.model.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PublishResourceRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotNull(message = "universityId is required")
    private UUID universityId;

    @NotNull(message = "areaId is required")
    private UUID areaId;

    /** Opcional — si presente, career.area_id debe coincidir con areaId (RN-23). */
    private UUID careerId;

    /** Opcional para EXAMEN_COMPLETO, GUIA y APUNTES; obligatorio para el resto (RN-07). */
    private UUID courseId;

    @NotNull(message = "resourceType is required")
    private ResourceType resourceType;

    /** Obligatorio para examenes; opcional para el resto. Null representa ano desconocido. */
    private Integer resourceYear;

    private String description;

    private Boolean aceptaResoluciones;

    // Metadatos de archivo — obtenidos desde POST /resources/files
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long   sizeBytes;
}
