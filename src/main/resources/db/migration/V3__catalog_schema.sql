-- V3 — Schema Catalog: universities, areas, courses, careers y tablas de relación
CREATE TABLE universities (
    id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    city VARCHAR(80)
);

CREATE TABLE areas (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    university_id UUID         NOT NULL REFERENCES universities(id),
    name          VARCHAR(120) NOT NULL,
    description   TEXT,
    UNIQUE (university_id, name)
);

CREATE TABLE courses (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE area_courses (
    area_id   UUID NOT NULL REFERENCES areas(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    PRIMARY KEY (area_id, course_id)
);

CREATE TABLE careers (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    university_id UUID         NOT NULL REFERENCES universities(id),
    area_id       UUID         NOT NULL REFERENCES areas(id),
    name          VARCHAR(120) NOT NULL,
    description   TEXT,
    UNIQUE (university_id, area_id, name)
);

CREATE TABLE career_courses (
    career_id UUID NOT NULL REFERENCES careers(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    PRIMARY KEY (career_id, course_id)
);
