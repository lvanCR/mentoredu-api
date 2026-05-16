-- V2: Restructuración de arquitectura por Bounded Contexts
-- community → forum | content → library | subscription → billing | notification → notifications
-- Perfil: base profiles + subtypes por profile_id | Academy BC nuevo
-- Eliminación de columnas legacy en academic_resources

BEGIN;

-- ===========================
-- LIBRARY BC: universities → institutions
-- ===========================
ALTER TABLE universities RENAME TO institutions;

ALTER TABLE academic_resources DROP CONSTRAINT IF EXISTS fk_resources_university;
ALTER TABLE academic_resources RENAME COLUMN university_id TO institution_id;
ALTER TABLE academic_resources ADD CONSTRAINT fk_resources_institution
    FOREIGN KEY (institution_id) REFERENCES institutions(id);

-- file_id: FK 1:1 a resource_files (reemplaza columnas inline de archivo)
ALTER TABLE academic_resources ADD COLUMN file_id uuid;
ALTER TABLE academic_resources ADD CONSTRAINT fk_resources_file
    FOREIGN KEY (file_id) REFERENCES resource_files(id);

-- Eliminar columnas legacy (archivo inline, versioning, campos de búsqueda redundantes)
ALTER TABLE academic_resources
    DROP COLUMN IF EXISTS file_url,
    DROP COLUMN IF EXISTS file_name,
    DROP COLUMN IF EXISTS content_type,
    DROP COLUMN IF EXISTS file_size,
    DROP COLUMN IF EXISTS file_hash,
    DROP COLUMN IF EXISTS version,
    DROP COLUMN IF EXISTS university,
    DROP COLUMN IF EXISTS area,
    DROP COLUMN IF EXISTS category,
    DROP COLUMN IF EXISTS anonymous;

-- verification_status y verified_by pertenecen al BC verification
ALTER TABLE academic_resources DROP CONSTRAINT IF EXISTS fk_resources_verified_by;
ALTER TABLE academic_resources
    DROP COLUMN IF EXISTS verification_status,
    DROP COLUMN IF EXISTS verified_by;

-- ===========================
-- PROFILE BC: tabla base profiles + restructuración de subtypes
-- ===========================
CREATE TABLE IF NOT EXISTS profiles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL UNIQUE,
    display_name varchar(120) NOT NULL,
    avatar_url text,
    city varchar(80),
    bio text,
    profile_type varchar(20) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Eliminar subtables antiguas (usaban user_id como PK directo)
DROP TABLE IF EXISTS student_profiles CASCADE;
DROP TABLE IF EXISTS teacher_profiles CASCADE;
DROP TABLE IF EXISTS academy_profiles CASCADE;
DROP TABLE IF EXISTS moderator_profiles CASCADE;
DROP TABLE IF EXISTS admin_profiles CASCADE;

-- Recrear subtables con profile_id como PK/FK a profiles
CREATE TABLE IF NOT EXISTS student_profiles (
    profile_id uuid PRIMARY KEY,
    school_name varchar(120),
    grade_level varchar(20),
    target_university varchar(120),
    target_career varchar(120),
    study_shift varchar(30),
    CONSTRAINT fk_student_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_profiles (
    profile_id uuid PRIMARY KEY,
    specialty varchar(120),
    institution_name varchar(120),
    bio_professional text,
    CONSTRAINT fk_teacher_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS organization_profiles (
    profile_id uuid PRIMARY KEY,
    organization_name varchar(120),
    ruc varchar(20) UNIQUE,
    website varchar(255),
    contact_email varchar(120),
    CONSTRAINT fk_organization_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- ===========================
-- ACADEMY BC: tablas nuevas
-- ===========================
CREATE TABLE IF NOT EXISTS academies (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_profile_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    description text,
    website varchar(255),
    email varchar(120),
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_academies_owner FOREIGN KEY (owner_profile_id) REFERENCES organization_profiles(profile_id)
);

CREATE TABLE IF NOT EXISTS campuses (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    address varchar(180) NOT NULL,
    city varchar(80) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_campuses_academy FOREIGN KEY (academy_id) REFERENCES academies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS programs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    modality varchar(30) NOT NULL,
    intensity varchar(30) NOT NULL,
    target_university varchar(120) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_programs_academy FOREIGN KEY (academy_id) REFERENCES academies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cycles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    academy_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    cycle_type varchar(30) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_cycles_academy FOREIGN KEY (academy_id) REFERENCES academies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_academies (
    teacher_profile_id uuid NOT NULL,
    academy_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (teacher_profile_id, academy_id),
    CONSTRAINT fk_ta_teacher FOREIGN KEY (teacher_profile_id) REFERENCES teacher_profiles(profile_id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_academy FOREIGN KEY (academy_id) REFERENCES academies(id) ON DELETE CASCADE
);

-- ===========================
-- BILLING BC: premium_access (faltaba en V1)
-- ===========================
CREATE TABLE IF NOT EXISTS premium_access (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    granted_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    CONSTRAINT fk_premium_access_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMIT;
