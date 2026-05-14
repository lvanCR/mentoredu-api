package com.mentoredu.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublishDocumentRequest {
    @NotBlank private String title;
    @NotBlank private String type;
    @NotBlank private String category;
    @NotBlank private String fileUrl;
    private String fileName;
    private String university;
    private Integer year;
    private String area;
}
