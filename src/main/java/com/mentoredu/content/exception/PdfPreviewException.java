package com.mentoredu.content.exception;

public class PdfPreviewException extends RuntimeException {
    public PdfPreviewException() {
        super("No se puede previsualizar este archivo. Intenta descargarlo.");
    }
}
