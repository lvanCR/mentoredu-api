-- =============================================================
-- V8 — Ciclo Pedagógico: resource_solutions + allows_solutions
-- Fuente de verdad: docs/diagrama-er.puml
-- Depende de: US39 (submit solution), US40 (feedback on solution)
-- =============================================================

ALTER TABLE academic_resources
    ADD COLUMN allows_solutions BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE resource_solutions (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id  UUID        NOT NULL REFERENCES academic_resources(id),
    student_id   UUID        NOT NULL REFERENCES users(id),
    file_id      UUID        NOT NULL REFERENCES resource_files(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (resource_id, student_id)
);

ALTER TABLE feedback_entries
    ADD COLUMN solution_id UUID REFERENCES resource_solutions(id),
    ADD COLUMN resource_id UUID REFERENCES academic_resources(id);
