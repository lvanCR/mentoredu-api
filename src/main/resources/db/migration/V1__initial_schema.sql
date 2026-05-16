-- =============================================================
-- V1 — Schema inicial completo (todos los bounded contexts)
-- Fuente de verdad: docs/diagrama-er.puml
-- =============================================================

-- ==========================
-- AUTH
-- ==========================
CREATE TABLE roles (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(120)
);

CREATE TABLE users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name    VARCHAR(80) NOT NULL,
    last_name     VARCHAR(80) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    provider      VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    role_id       UUID        NOT NULL REFERENCES roles(id),
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id),
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessions (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id),
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMP   NOT NULL,
    revoked_at    TIMESTAMP,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- PROFILE
-- ==========================
CREATE TABLE profiles (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL UNIQUE REFERENCES users(id),
    display_name VARCHAR(120) NOT NULL,
    avatar_url   TEXT,
    city         VARCHAR(80),
    bio          TEXT,
    profile_type VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_profiles (
    profile_id        UUID        PRIMARY KEY REFERENCES profiles(id),
    school_name       VARCHAR(120),
    grade_level       VARCHAR(20),
    target_university VARCHAR(120),
    target_career     VARCHAR(120),
    study_shift       VARCHAR(30)
);

CREATE TABLE teacher_profiles (
    profile_id       UUID PRIMARY KEY REFERENCES profiles(id),
    specialty        VARCHAR(120),
    institution_name VARCHAR(120),
    bio_professional TEXT
);

CREATE TABLE organization_profiles (
    profile_id        UUID        PRIMARY KEY REFERENCES profiles(id),
    organization_name VARCHAR(120),
    ruc               VARCHAR(20) UNIQUE,
    website           VARCHAR(255),
    contact_email     VARCHAR(120)
);

CREATE TABLE notification_preferences (
    user_id            UUID    PRIMARY KEY REFERENCES users(id),
    email_enabled      BOOLEAN DEFAULT TRUE,
    in_app_enabled     BOOLEAN DEFAULT TRUE,
    forum_enabled      BOOLEAN DEFAULT TRUE,
    moderation_enabled BOOLEAN DEFAULT TRUE
);

-- ==========================
-- ACADEMY
-- ==========================
CREATE TABLE academies (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_profile_id UUID        NOT NULL REFERENCES organization_profiles(profile_id),
    name             VARCHAR(120) NOT NULL,
    description      TEXT,
    website          VARCHAR(255),
    email            VARCHAR(120),
    active           BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campuses (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id UUID        NOT NULL REFERENCES academies(id),
    name       VARCHAR(120) NOT NULL,
    address    VARCHAR(180) NOT NULL,
    city       VARCHAR(80)  NOT NULL,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE programs (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id        UUID        NOT NULL REFERENCES academies(id),
    name              VARCHAR(120) NOT NULL,
    modality          VARCHAR(30)  NOT NULL,
    intensity         VARCHAR(30)  NOT NULL,
    target_university VARCHAR(120) NOT NULL,
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cycles (
    id         UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id UUID       NOT NULL REFERENCES academies(id),
    name       VARCHAR(120) NOT NULL,
    cycle_type VARCHAR(30)  NOT NULL,
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    created_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teacher_academies (
    teacher_profile_id UUID      NOT NULL REFERENCES teacher_profiles(profile_id),
    academy_id         UUID      NOT NULL REFERENCES academies(id),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_profile_id, academy_id)
);

-- ==========================
-- LIBRARY
-- ==========================
CREATE TABLE institutions (
    id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    city VARCHAR(80)
);

CREATE TABLE subjects (
    id   UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE tags (
    id   UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE resource_files (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    file_url   TEXT        NOT NULL,
    file_name  VARCHAR(255) NOT NULL,
    mime_type  VARCHAR(80),
    size_bytes BIGINT,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE academic_resources (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    author_user_id UUID        NOT NULL REFERENCES users(id),
    subject_id     UUID        REFERENCES subjects(id),
    institution_id UUID        REFERENCES institutions(id),
    file_id        UUID        UNIQUE REFERENCES resource_files(id),
    title          VARCHAR(160) NOT NULL,
    description    TEXT,
    resource_type  VARCHAR(30)  NOT NULL,
    visibility     VARCHAR(20)  NOT NULL,
    exam_year      INT,
    exam_cycle     VARCHAR(30),
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resource_tags (
    resource_id UUID      NOT NULL REFERENCES academic_resources(id),
    tag_id      UUID      NOT NULL REFERENCES tags(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resource_id, tag_id)
);

CREATE TABLE download_logs (
    id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID      NOT NULL REFERENCES users(id),
    resource_id   UUID      NOT NULL REFERENCES academic_resources(id),
    downloaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- FORUM
-- ==========================
CREATE TABLE threads (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    author_user_id UUID        NOT NULL REFERENCES users(id),
    subject_id     UUID        NOT NULL REFERENCES subjects(id),
    title          VARCHAR(160) NOT NULL,
    body           TEXT        NOT NULL,
    is_anonymous   BOOLEAN     NOT NULL DEFAULT FALSE,
    status         VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE follow_relations (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_user_id UUID      NOT NULL REFERENCES users(id),
    followed_user_id UUID      NOT NULL REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reactions (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID       NOT NULL REFERENCES users(id),
    thread_id     UUID       REFERENCES threads(id),
    answer_id     UUID       REFERENCES answers(id),
    comment_id    UUID       REFERENCES comments(id),
    reaction_type VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- GAMIFICATION
-- ==========================
CREATE TABLE coin_wallets (
    user_id    UUID      PRIMARY KEY REFERENCES users(id),
    balance    INT       NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE point_transactions (
    id          UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID       NOT NULL REFERENCES users(id),
    source_type VARCHAR(30) NOT NULL,
    source_id   UUID,
    points_delta INT       NOT NULL,
    reason      VARCHAR(120) NOT NULL,
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE badges (
    id          UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(40) NOT NULL UNIQUE,
    name        VARCHAR(80) NOT NULL,
    description TEXT
);

CREATE TABLE user_badges (
    user_id   UUID      NOT NULL REFERENCES users(id),
    badge_id  UUID      NOT NULL REFERENCES badges(id),
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, badge_id)
);

CREATE TABLE level_progress (
    user_id             UUID           PRIMARY KEY REFERENCES users(id),
    current_level       INT            NOT NULL DEFAULT 1,
    experience          INT            NOT NULL DEFAULT 0,
    progress_percentage DECIMAL(5,2)   NOT NULL DEFAULT 0.00,
    updated_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- MODERATION
-- ==========================
CREATE TABLE reports (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_user_id UUID        NOT NULL REFERENCES users(id),
    target_type      VARCHAR(30)  NOT NULL,
    target_id        UUID        NOT NULL,
    reason           VARCHAR(200) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    resolved_by      UUID        REFERENCES users(id),
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP
);

CREATE TABLE moderation_actions (
    id                UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id         UUID       NOT NULL REFERENCES reports(id),
    moderator_user_id UUID       NOT NULL REFERENCES users(id),
    action_type       VARCHAR(30) NOT NULL,
    notes             TEXT,
    created_at        TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID       NOT NULL REFERENCES users(id),
    action_type   VARCHAR(50) NOT NULL,
    entity_type   VARCHAR(50) NOT NULL,
    entity_id     UUID       NOT NULL,
    detail        TEXT,
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appeals (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id   UUID      NOT NULL REFERENCES reports(id),
    user_id     UUID      NOT NULL REFERENCES users(id),
    reason      TEXT      NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
);

-- ==========================
-- VERIFICATION
-- ==========================
CREATE TABLE verification_requests (
    id           UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID       NOT NULL REFERENCES users(id),
    entity_type  VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    reviewed_by  UUID       REFERENCES users(id),
    reviewed_at  TIMESTAMP
);

CREATE TABLE verification_documents (
    id            UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id    UUID       NOT NULL REFERENCES verification_requests(id),
    document_type VARCHAR(50) NOT NULL,
    file_url      TEXT       NOT NULL,
    uploaded_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- BILLING
-- ==========================
CREATE TABLE plans (
    id            UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(40)    NOT NULL UNIQUE,
    price         DECIMAL(10,2)  NOT NULL,
    duration_days INT            NOT NULL,
    description   VARCHAR(200)
);

CREATE TABLE subscriptions (
    id         UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID       NOT NULL REFERENCES users(id),
    plan_id    UUID       NOT NULL REFERENCES plans(id),
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    starts_at  TIMESTAMP,
    ends_at    TIMESTAMP,
    created_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coin_packages (
    id     UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name   VARCHAR(60)    NOT NULL,
    coins  INT            NOT NULL,
    price  DECIMAL(10,2)  NOT NULL,
    active BOOLEAN        DEFAULT TRUE
);

CREATE TABLE coin_purchases (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users(id),
    coin_package_id UUID          NOT NULL REFERENCES coin_packages(id),
    quantity        INT           NOT NULL DEFAULT 1,
    total_amount    DECIMAL(10,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL REFERENCES users(id),
    subscription_id  UUID          REFERENCES subscriptions(id),
    coin_purchase_id UUID          REFERENCES coin_purchases(id),
    amount           DECIMAL(10,2) NOT NULL,
    currency         VARCHAR(10)   NOT NULL DEFAULT 'PEN',
    payment_method   VARCHAR(30)   NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE premium_access (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL REFERENCES users(id),
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP
);

-- ==========================
-- NOTIFICATIONS
-- ==========================
CREATE TABLE notifications (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id),
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(120) NOT NULL,
    message    TEXT        NOT NULL,
    read_at    TIMESTAMP,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);
