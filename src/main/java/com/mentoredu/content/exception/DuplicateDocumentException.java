package com.mentoredu.content.exception;

import com.mentoredu.content.model.AcademicResource;
import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateDocumentException extends RuntimeException {
    private final List<AcademicResource> duplicates;

    public DuplicateDocumentException(List<AcademicResource> duplicates) {
        super("Este documento parece estar duplicado. Confirma si quieres subirlo de todas formas con una nueva version.");
        this.duplicates = duplicates;
    }
}
