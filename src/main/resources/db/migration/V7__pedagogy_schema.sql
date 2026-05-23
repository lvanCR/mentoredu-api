-- V7 — Schema Pedagogy: solutions, feedback_entries
-- RN-11: 1 resolución por ejercicio por estudiante (UNIQUE)
-- RN-22: score entre 0.0 y 10.0
CREATE TABLE solutions (
    id          UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID       NOT NULL REFERENCES resources(id),
    student_id  UUID       NOT NULL REFERENCES users(id),
    file_url    TEXT,
    content     TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (resource_id, student_id)
);

-- RN-10: feedback inmutable (sin UPDATE/DELETE permitidos por lógica de negocio)
-- RN-11: 1 feedback por solution (UNIQUE)
CREATE TABLE feedback_entries (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    solution_id    UUID          NOT NULL UNIQUE REFERENCES solutions(id),
    author_user_id UUID          NOT NULL REFERENCES users(id),
    score          DECIMAL(4,2)  CHECK (score >= 0.0 AND score <= 10.0),
    body           TEXT          NOT NULL,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);
