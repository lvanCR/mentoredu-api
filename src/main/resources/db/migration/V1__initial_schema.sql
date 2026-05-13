-- V1__initial_schema.sql - Initial schema for MentorEdu
-- Generated from ER diagram (bounded contexts: auth, profile, content, community, gamification, moderation, verification, subscription, notification)
-- PostgreSQL dialect

BEGIN;

-- Roles
CREATE TABLE IF NOT EXISTS roles (
    id uuid PRIMARY KEY,
    name varchar(30) NOT NULL UNIQUE,
    description varchar(120)
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY,
    first_name varchar(80) NOT NULL,
    last_name varchar(80) NOT NULL,
    email varchar(120) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    provider varchar(20) NOT NULL DEFAULT 'EMAIL',
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',
    role_id uuid NOT NULL,
    points integer NOT NULL DEFAULT 0,
    coins integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    token varchar(255) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sessions / Refresh tokens
CREATE TABLE IF NOT EXISTS sessions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    refresh_token varchar(255) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Profiles
CREATE TABLE IF NOT EXISTS student_profiles (
    user_id uuid PRIMARY KEY,
    school_name varchar(120),
    grade_level varchar(20),
    target_university varchar(120),
    target_career varchar(120),
    study_shift varchar(30),
    is_anonymous_allowed boolean DEFAULT true,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS moderator_profiles (
    user_id uuid PRIMARY KEY,
    permission_level varchar(30),
    active boolean DEFAULT true,
    CONSTRAINT fk_moderator_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admin_profiles (
    user_id uuid PRIMARY KEY,
    department varchar(80),
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_profiles (
    user_id uuid PRIMARY KEY,
    specialty varchar(120),
    verification_status varchar(20),
    bio text,
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS academy_profiles (
    user_id uuid PRIMARY KEY,
    academy_name varchar(120),
    ruc varchar(20) UNIQUE,
    verification_status varchar(20),
    website varchar(255),
    CONSTRAINT fk_academy_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id uuid PRIMARY KEY,
    email_enabled boolean DEFAULT true,
    in_app_enabled boolean DEFAULT true,
    community_enabled boolean DEFAULT true,
    moderation_enabled boolean DEFAULT true,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Content domain (universities, subjects, tags, files, documents)
CREATE TABLE IF NOT EXISTS universities (
    id uuid PRIMARY KEY,
    name varchar(120) NOT NULL UNIQUE,
    city varchar(80)
);

CREATE TABLE IF NOT EXISTS subjects (
    id uuid PRIMARY KEY,
    name varchar(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tags (
    id uuid PRIMARY KEY,
    name varchar(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS resource_files (
    id uuid PRIMARY KEY,
    file_url text NOT NULL,
    file_name varchar(255) NOT NULL,
    mime_type varchar(80),
    size_bytes bigint,
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Documents (maps to academic_resources in the ER diagram)
CREATE TABLE IF NOT EXISTS documents (
    id uuid PRIMARY KEY,
    author_user_id uuid NOT NULL,
    subject_id uuid,
    university_id uuid,
    title varchar(160) NOT NULL,
    description text,
    resource_type varchar(30) NOT NULL,
    visibility varchar(20) NOT NULL,
    verification_status varchar(20),
    verified_by uuid,
    exam_year integer,
    exam_cycle varchar(30),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_documents_author FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_documents_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_documents_university FOREIGN KEY (university_id) REFERENCES universities(id),
    CONSTRAINT fk_documents_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS resource_tags (
    resource_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (resource_id, tag_id),
    CONSTRAINT fk_resource_tags_resource FOREIGN KEY (resource_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Community (threads, answers, comments, follows, reactions)
CREATE TABLE IF NOT EXISTS threads (
    id uuid PRIMARY KEY,
    author_id uuid NOT NULL,
    subject_id uuid,
    title varchar(160) NOT NULL,
    body text NOT NULL,
    is_anonymous boolean DEFAULT false,
    status varchar(20) NOT NULL DEFAULT 'OPEN',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_threads_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_threads_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE IF NOT EXISTS answers (
    id uuid PRIMARY KEY,
    thread_id uuid NOT NULL,
    author_id uuid NOT NULL,
    body text NOT NULL,
    is_accepted boolean DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_answers_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id uuid PRIMARY KEY,
    thread_id uuid,
    answer_id uuid,
    author_id uuid NOT NULL,
    body text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_comments_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_answer FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS follow_relations (
    id uuid PRIMARY KEY,
    follower_user_id uuid NOT NULL,
    followed_user_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_follow UNIQUE (follower_user_id, followed_user_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_followed FOREIGN KEY (followed_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reactions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    thread_id uuid,
    answer_id uuid,
    comment_id uuid,
    reaction_type varchar(20) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_answer FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- Gamification
CREATE TABLE IF NOT EXISTS coin_wallets (
    user_id uuid PRIMARY KEY,
    balance integer NOT NULL DEFAULT 0,
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_coin_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS point_transactions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    source_type varchar(30),
    source_id uuid,
    points_delta integer NOT NULL,
    reason varchar(120),
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_point_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS badges (
    id uuid PRIMARY KEY,
    code varchar(40) NOT NULL UNIQUE,
    name varchar(80) NOT NULL,
    description text
);

CREATE TABLE IF NOT EXISTS user_badges (
    user_id uuid NOT NULL,
    badge_id uuid NOT NULL,
    earned_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, badge_id),
    CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS level_progress (
    user_id uuid PRIMARY KEY,
    current_level integer NOT NULL DEFAULT 1,
    experience integer NOT NULL DEFAULT 0,
    progress_percentage numeric(5,2) NOT NULL DEFAULT 0,
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_level_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Moderation
CREATE TABLE IF NOT EXISTS reports (
    id uuid PRIMARY KEY,
    reporter_user_id uuid NOT NULL,
    target_type varchar(30) NOT NULL,
    target_id uuid NOT NULL,
    reason varchar(200) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'OPEN',
    resolved_by uuid,
    created_at timestamptz NOT NULL DEFAULT now(),
    resolved_at timestamptz,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_reports_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS moderation_actions (
    id uuid PRIMARY KEY,
    report_id uuid NOT NULL,
    moderator_user_id uuid NOT NULL,
    action_type varchar(30) NOT NULL,
    notes text,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_moderation_report FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_moderation_moderator FOREIGN KEY (moderator_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id uuid PRIMARY KEY,
    actor_user_id uuid,
    action_type varchar(50) NOT NULL,
    entity_type varchar(50) NOT NULL,
    entity_id uuid,
    detail text,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS appeals (
    id uuid PRIMARY KEY,
    report_id uuid NOT NULL,
    user_id uuid NOT NULL,
    reason text,
    status varchar(20) NOT NULL DEFAULT 'OPEN',
    created_at timestamptz NOT NULL DEFAULT now(),
    reviewed_at timestamptz,
    CONSTRAINT fk_appeals_report FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_appeals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Verification
CREATE TABLE IF NOT EXISTS verification_requests (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    entity_type varchar(20),
    status varchar(20) NOT NULL DEFAULT 'PENDING',
    submitted_at timestamptz NOT NULL DEFAULT now(),
    reviewed_by uuid,
    reviewed_at timestamptz,
    CONSTRAINT fk_verreq_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_verreq_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS verification_documents (
    id uuid PRIMARY KEY,
    request_id uuid NOT NULL,
    document_type varchar(50),
    file_url text,
    uploaded_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_verdoc_request FOREIGN KEY (request_id) REFERENCES verification_requests(id) ON DELETE CASCADE
);

-- Subscription / Monetization
CREATE TABLE IF NOT EXISTS plans (
    id uuid PRIMARY KEY,
    name varchar(40) NOT NULL UNIQUE,
    price numeric(10,2),
    duration_days integer,
    description varchar(200)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    plan_id uuid NOT NULL,
    status varchar(20) NOT NULL,
    starts_at timestamptz,
    ends_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id uuid PRIMARY KEY,
    user_id uuid,
    subscription_id uuid,
    coin_purchase_id uuid,
    amount numeric(10,2),
    currency varchar(10),
    payment_method varchar(30),
    status varchar(20),
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS coin_packages (
    id uuid PRIMARY KEY,
    name varchar(60) NOT NULL,
    coins integer NOT NULL,
    price numeric(10,2),
    active boolean DEFAULT true
);

CREATE TABLE IF NOT EXISTS coin_purchases (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    coin_package_id uuid NOT NULL,
    quantity integer NOT NULL,
    total_amount numeric(10,2),
    status varchar(20),
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_coin_purchases_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_coin_purchases_package FOREIGN KEY (coin_package_id) REFERENCES coin_packages(id) ON DELETE SET NULL
);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    type varchar(30),
    title varchar(120),
    message text,
    read_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Download logs (existing in code)
CREATE TABLE IF NOT EXISTS download_logs (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    document_id uuid NOT NULL,
    downloaded_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_download_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_download_logs_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

COMMIT;
