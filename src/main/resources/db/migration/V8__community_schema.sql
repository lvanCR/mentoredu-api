-- V8 — Schema Community: follows, verification_requests, verification_docs,
--        teacher_academy_links, reports, notifications
-- RN-21: un usuario no puede seguirse a sí mismo (CHECK)
CREATE TABLE follows (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID      NOT NULL REFERENCES users(id),
    followed_id UUID      NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (follower_id, followed_id),
    CHECK (follower_id != followed_id)
);

-- RN-16: al menos 1 documento adjunto (validado en servicio)
-- RN-17: notes almacena la razón de rechazo (obligatoria si status=REJECTED)
CREATE TABLE verification_requests (
    id           UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID       NOT NULL REFERENCES users(id),
    entity_type  VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes        TEXT,
    submitted_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    reviewed_by  UUID       REFERENCES users(id),
    reviewed_at  TIMESTAMP
);

CREATE TABLE verification_docs (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id    UUID       NOT NULL REFERENCES verification_requests(id),
    document_type VARCHAR(50) NOT NULL,
    file_url      TEXT       NOT NULL,
    uploaded_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- RN-18: asociación docente-academia queda en PENDING hasta resolución
CREATE TABLE teacher_academy_links (
    id                 UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_profile_id UUID       NOT NULL REFERENCES teacher_profiles(profile_id),
    academy_profile_id UUID       NOT NULL REFERENCES academy_profiles(profile_id),
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at       TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    resolved_at        TIMESTAMP,
    UNIQUE (teacher_profile_id, academy_profile_id)
);

-- RN-19: toda resolución de reporte genera entrada en log (actorId + acción + timestamp)
CREATE TABLE reports (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_user_id UUID        NOT NULL REFERENCES users(id),
    target_type      VARCHAR(30)  NOT NULL,
    target_id        UUID        NOT NULL,
    reason           VARCHAR(200) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    resolution_note  TEXT,
    resolved_by      UUID        REFERENCES users(id),
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP
);

-- Notificaciones con payload JSON para flexibilidad por tipo (US27)
CREATE TABLE notifications (
    id         UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID       NOT NULL REFERENCES users(id),
    type       VARCHAR(30) NOT NULL,
    payload    JSONB,
    read_at    TIMESTAMP,
    created_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);
