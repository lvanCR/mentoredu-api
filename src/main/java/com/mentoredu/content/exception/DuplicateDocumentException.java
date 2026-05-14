package com.mentoredu.content.exception;

import com.mentoredu.content.model.Document;
import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateDocumentException extends RuntimeException {
    private final List<Document> duplicates;

    public DuplicateDocumentException(List<Document> duplicates) {
        super("Este documento parece estar duplicado. Confirma si quieres subirlo de todas formas con una nueva version.");
        this.duplicates = duplicates;
    }
}
