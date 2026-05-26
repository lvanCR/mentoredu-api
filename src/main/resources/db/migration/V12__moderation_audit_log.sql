-- V12 — OBS-M1: tabla de auditoría para resoluciones de reportes (RN-19)
-- actor_id + action + target_report_id + timestamp

CREATE TABLE moderation_audit_log (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id  UUID        NOT NULL REFERENCES reports(id),
    actor_id   UUID        NOT NULL REFERENCES users(id),
    action     VARCHAR(30) NOT NULL,
    note       TEXT,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_report_id ON moderation_audit_log(report_id);
CREATE INDEX idx_audit_actor_id  ON moderation_audit_log(actor_id);
