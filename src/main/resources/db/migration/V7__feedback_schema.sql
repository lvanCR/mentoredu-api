-- =============================================================
-- V7 — Bounded Context: Feedback (EP-11)
-- Fuente de verdad: docs/diagrama-er.puml
-- =============================================================

CREATE TABLE feedback_entries (
    id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    author_user_id UUID           NOT NULL REFERENCES users(id),
    target_user_id UUID           NOT NULL REFERENCES users(id),
    program_id     UUID           REFERENCES programs(id),
    cycle_id       UUID           REFERENCES cycles(id),
    body           TEXT           NOT NULL,
    score          DECIMAL(4,2),
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);
