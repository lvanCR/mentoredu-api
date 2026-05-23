-- V4 — Schema Profile: profiles, student_profiles, teacher_profiles, academy_profiles
-- teacher_specialties eliminada en v2.1 (sobre-diseño — reemplazada por bio_professional)
CREATE TABLE profiles (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL UNIQUE REFERENCES users(id),
    display_name VARCHAR(120) NOT NULL,
    avatar_url   TEXT,
    city         VARCHAR(80),
    bio          TEXT,
    profile_type VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_profiles (
    profile_id           UUID        PRIMARY KEY REFERENCES profiles(id),
    school_name          VARCHAR(120),
    grade_level          VARCHAR(20),
    study_shift          VARCHAR(30),
    target_university_id UUID        REFERENCES universities(id),
    target_area_id       UUID        REFERENCES areas(id),
    target_career_id     UUID        REFERENCES careers(id)
);

CREATE TABLE teacher_profiles (
    profile_id       UUID PRIMARY KEY REFERENCES profiles(id),
    bio_professional TEXT
);

CREATE TABLE academy_profiles (
    profile_id    UUID        PRIMARY KEY REFERENCES profiles(id),
    academy_name  VARCHAR(120) NOT NULL UNIQUE,
    ruc           VARCHAR(20)  UNIQUE,
    website       VARCHAR(255),
    contact_email VARCHAR(120)
);
