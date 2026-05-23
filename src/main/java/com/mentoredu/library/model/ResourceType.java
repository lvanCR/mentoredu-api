package com.mentoredu.library.model;

public enum ResourceType {
    EXAMEN_COMPLETO,
    EXAMEN_SECCION,
    GUIA,
    APUNTES,
    PRACTICA,
    OTRO;

    /** RN-07: solo estos tres tipos pueden omitir course_id. */
    public boolean courseIdOptional() {
        return this == EXAMEN_COMPLETO || this == GUIA || this == APUNTES;
    }
}
