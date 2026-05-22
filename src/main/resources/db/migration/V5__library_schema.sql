-- V5 — Schema Library: resources, download_logs
-- career_id nullable: refinamiento vocacional opcional (RN-23: career.area_id == area_id)
-- course_id nullable: EXAMEN_COMPLETO, GUIA y APUNTES pueden omitirlo (RN-07)
CREATE TABLE resources (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    author_user_id      UUID        NOT NULL REFERENCES users(id),
    university_id       UUID        NOT NULL REFERENCES universities(id),
    area_id             UUID        NOT NULL REFERENCES areas(id),
    career_id           UUID        REFERENCES careers(id),
    course_id           UUID        REFERENCES courses(id),
    file_url            TEXT,
    file_name           VARCHAR(255),
    mime_type           VARCHAR(80),
    size_bytes          BIGINT,
    title               VARCHAR(160) NOT NULL,
    description         TEXT,
    resource_type       VARCHAR(30)  NOT NULL,
    visibility          VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    acepta_resoluciones BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_course_nullable
        CHECK (course_id IS NOT NULL
            OR resource_type IN ('EXAMEN_COMPLETO', 'GUIA', 'APUNTES')),
    CONSTRAINT chk_resoluciones_solo_practica
        CHECK (acepta_resoluciones = FALSE OR resource_type = 'PRACTICA')
);

CREATE INDEX idx_resources_student_feed
    ON resources(university_id, area_id, career_id, resource_type);

CREATE TABLE download_logs (
    id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID      NOT NULL REFERENCES users(id),
    resource_id   UUID      NOT NULL REFERENCES resources(id),
    downloaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
