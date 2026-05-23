-- V6 — Schema Forum: threads, answers, comments, reactions
-- threads: clasificación multi-modal con 4 FKs opcionales (ver RN-12)
-- Combinaciones prohibidas por CHECK:
--   (1) area_id sin university_id
--   (2) career_id + course_id simultáneos (contextos semánticamente incongruentes)
--   (3) ninguna clasificación presente
CREATE TABLE threads (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    author_user_id UUID        NOT NULL REFERENCES users(id),
    title          VARCHAR(160) NOT NULL,
    body           TEXT        NOT NULL,
    is_anonymous   BOOLEAN     NOT NULL DEFAULT FALSE,
    status         VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    university_id  UUID        REFERENCES universities(id),
    area_id        UUID        REFERENCES areas(id),
    course_id      UUID        REFERENCES courses(id),
    career_id      UUID        REFERENCES careers(id),
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_area_requires_university
        CHECK (area_id IS NULL OR university_id IS NOT NULL),
    CONSTRAINT chk_career_course_exclusive
        CHECK (career_id IS NULL OR course_id IS NULL),
    CONSTRAINT chk_at_least_one_classification
        CHECK (university_id IS NOT NULL OR course_id IS NOT NULL OR career_id IS NOT NULL)
);

CREATE INDEX idx_threads_feed
    ON threads(university_id, area_id, course_id, career_id, status);

CREATE TABLE answers (
    id             UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id      UUID      NOT NULL REFERENCES threads(id),
    author_user_id UUID      NOT NULL REFERENCES users(id),
    body           TEXT      NOT NULL,
    is_accepted    BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comments (
    id             UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id      UUID      NOT NULL REFERENCES threads(id),
    answer_id      UUID      REFERENCES answers(id),
    author_user_id UUID      NOT NULL REFERENCES users(id),
    body           TEXT      NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reacciones polimórficas: target_type IN ('THREAD','ANSWER','COMMENT','RESOURCE')
CREATE TABLE reactions (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID       NOT NULL REFERENCES users(id),
    target_type   VARCHAR(20) NOT NULL,
    target_id     UUID       NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, target_type, target_id)
);
