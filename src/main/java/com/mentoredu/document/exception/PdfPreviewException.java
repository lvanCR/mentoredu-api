package com.mentoredu.document.exception;

public class PdfPreviewException extends RuntimeException {

    public PdfPreviewException() {
        super("No se puede previsualizar este archivo. Intenta descargarlo.");
    }
}
