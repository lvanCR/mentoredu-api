package com.mentoredu.content.exception;

import com.mentoredu.content.model.AcademicResource;
import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final List<AcademicResource> duplicates;

    public DuplicateResourceException(List<AcademicResource> duplicates) {
        super("Este recurso parece estar duplicado. Confirma si quieres subirlo de todas formas con una nueva versión.");
        this.duplicates = duplicates;
    }
}
