# Plan de Seed Data — Sistema de Admisión Lima, Perú

> **Fuente de verdad**: Este archivo define el orden, estructura y UUIDs fijos de las migraciones  
> de datos de referencia para el sistema de admisión universitaria peruano en MentorEdu.  
> Toda modificación a los datos semilla debe registrarse aquí antes de crear la migración.

---

## Principios de diseño

1. **UUIDs fijos por dominio** — Los UUIDs de seed no son `gen_random_uuid()`. Usan prefijos reconocibles por dominio para facilitar referencias cruzadas entre migraciones.
2. **Idempotencia obligatoria** — Todos los INSERTs usan `ON CONFLICT (...) DO NOTHING`. Seguros de correr N veces.
3. **Orden estricto de migraciones** — El schema (V10) debe aplicarse antes que los datos (V11-V13). Flyway garantiza el orden.
4. **Solo ADMIN modifica** — Los datos de referencia son de solo lectura para usuarios finales (RN-55). Cualquier corrección post-deploy se hace con una nueva migración Vxx.
5. **Sin `gen_random_uuid()` en seed** — Usar UUIDs literales garantiza reproducibilidad entre ambientes (local, staging, producción).

---

## Prefijos de UUID por dominio

| Dominio | Prefijo | Rango |
|---|---|---|
| Institutions | `b1000000-0000-0000-0000-0000000000XX` | 01–07 |
| Subjects | `b2000000-0000-0000-0000-0000000000XX` | 01–18 |
| Exam Areas | `b3000000-0000-0000-0000-0000000000XX` | 01–20+ |
| Area Subjects | `b4000000-0000-0000-0000-XXXXXXXXXXXX` | Derivado de área+subject |

---

## Orden de migraciones

```
V10__schema_admision.sql          ← DDL: exam_areas, area_subjects, FK en student_profiles y academic_resources
V11__seed_institutions_peru.sql   ← DML: 7 universidades de Lima
V12__seed_subjects_peru.sql       ← DML: ~18 cursos transversales
V13__seed_exam_areas_lima.sql     ← DML: áreas + ponderaciones (las 7 universidades)
```

---

## V10 — Schema (DDL)

```sql
-- V10__schema_admision.sql
-- Nuevas tablas: exam_areas, area_subjects
-- FKs opcionales en student_profiles y academic_resources
-- Idempotente: usa IF NOT EXISTS y ADD COLUMN IF NOT EXISTS

CREATE TABLE IF NOT EXISTS exam_areas (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id     UUID         NOT NULL REFERENCES institutions(id),
    code               VARCHAR(10)  NOT NULL,
    name               VARCHAR(120) NOT NULL,
    admission_modality VARCHAR(30)  NOT NULL,
    description        TEXT,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_area UNIQUE (institution_id, code, admission_modality)
);

CREATE TABLE IF NOT EXISTS area_subjects (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_area_id       UUID         NOT NULL REFERENCES exam_areas(id) ON DELETE CASCADE,
    subject_id         UUID         NOT NULL REFERENCES subjects(id),
    weight_percentage  DECIMAL(5,2) NOT NULL CHECK (weight_percentage >= 0 AND weight_percentage <= 100),
    is_eliminatory     BOOLEAN      NOT NULL DEFAULT FALSE,
    min_score          DECIMAL(4,2),
    CONSTRAINT uq_area_subject UNIQUE (exam_area_id, subject_id)
);

-- FK opcional en student_profiles (perfil del estudiante puede tener área objetivo)
ALTER TABLE student_profiles
    ADD COLUMN IF NOT EXISTS target_exam_area_id UUID REFERENCES exam_areas(id);

-- FK opcional en academic_resources (recurso puede etiquetarse por área)
ALTER TABLE academic_resources
    ADD COLUMN IF NOT EXISTS exam_area_id UUID REFERENCES exam_areas(id);
```

**Constraint de negocio (RN-53)** — La suma de `weight_percentage` por área debe ser 100.00. Se valida en la capa de servicio, no en la BD, para permitir inserción incremental durante el seed.

---

## V11 — Instituciones (DML)

```sql
-- V11__seed_institutions_peru.sql
-- 7 universidades de Lima con UUIDs fijos

INSERT INTO institutions (id, name, city) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'UNMSM - Universidad Nacional Mayor de San Marcos', 'Lima'),
    ('b1000000-0000-0000-0000-000000000002', 'UNI - Universidad Nacional de Ingeniería', 'Lima'),
    ('b1000000-0000-0000-0000-000000000003', 'PUCP - Pontificia Universidad Católica del Perú', 'Lima'),
    ('b1000000-0000-0000-0000-000000000004', 'ULima - Universidad de Lima', 'Lima'),
    ('b1000000-0000-0000-0000-000000000005', 'UPCH - Universidad Peruana Cayetano Heredia', 'Lima'),
    ('b1000000-0000-0000-0000-000000000006', 'UNFV - Universidad Nacional Federico Villarreal', 'Lima'),
    ('b1000000-0000-0000-0000-000000000007', 'UNALM - Universidad Nacional Agraria La Molina', 'La Molina')
ON CONFLICT (name) DO NOTHING;
```

---

## V12 — Materias/Cursos (DML)

```sql
-- V12__seed_subjects_peru.sql
-- 18 cursos que aparecen transversalmente en los prospectos de admisión de Lima

INSERT INTO subjects (id, name) VALUES
    ('b2000000-0000-0000-0000-000000000001', 'Matemática'),
    ('b2000000-0000-0000-0000-000000000002', 'Física'),
    ('b2000000-0000-0000-0000-000000000003', 'Química'),
    ('b2000000-0000-0000-0000-000000000004', 'Biología'),
    ('b2000000-0000-0000-0000-000000000005', 'Lengua y Literatura'),
    ('b2000000-0000-0000-0000-000000000006', 'Historia del Perú'),
    ('b2000000-0000-0000-0000-000000000007', 'Historia Universal'),
    ('b2000000-0000-0000-0000-000000000008', 'Geografía'),
    ('b2000000-0000-0000-0000-000000000009', 'Economía'),
    ('b2000000-0000-0000-0000-000000000010', 'Filosofía y Lógica'),
    ('b2000000-0000-0000-0000-000000000011', 'Aptitud Académica'),
    ('b2000000-0000-0000-0000-000000000012', 'Razonamiento Matemático'),
    ('b2000000-0000-0000-0000-000000000013', 'Razonamiento Verbal'),
    ('b2000000-0000-0000-0000-000000000014', 'Álgebra'),
    ('b2000000-0000-0000-0000-000000000015', 'Aritmética'),
    ('b2000000-0000-0000-0000-000000000016', 'Geometría'),
    ('b2000000-0000-0000-0000-000000000017', 'Trigonometría'),
    ('b2000000-0000-0000-0000-000000000018', 'Inglés')
ON CONFLICT (name) DO NOTHING;
```

---

## V13 — Áreas de Examen y Ponderaciones (DML)

### Mapa de IDs fijos (referencia cruzada V13)

**Áreas de examen (exam_areas):**

| UUID | Universidad | Código | Nombre | Modalidad |
|---|---|---|---|---|
| `b3000000-0000-0000-0000-000000000001` | UNMSM | A | Ciencias de la Salud | ORDINARIO |
| `b3000000-0000-0000-0000-000000000002` | UNMSM | B | Ingenierías y Ciencias Básicas | ORDINARIO |
| `b3000000-0000-0000-0000-000000000003` | UNMSM | C | Ciencias Económicas y de Gestión | ORDINARIO |
| `b3000000-0000-0000-0000-000000000004` | UNMSM | D | Humanidades, CCSS y Educación | ORDINARIO |
| `b3000000-0000-0000-0000-000000000005` | UNMSM | E | Arte y Diseño | ORDINARIO |
| `b3000000-0000-0000-0000-000000000006` | UNI | UNICA | Ingeniería y Ciencias | ORDINARIO |
| `b3000000-0000-0000-0000-000000000007` | PUCP | CIENCIAS | Ciencias e Ingeniería | ORDINARIO |
| `b3000000-0000-0000-0000-000000000008` | PUCP | HUMANIDADES | Humanidades, Gestión y Diseño | ORDINARIO |
| `b3000000-0000-0000-0000-000000000009` | ULima | UNICA | Examen General | ORDINARIO |
| `b3000000-0000-0000-0000-000000000010` | UPCH | SALUD | Ciencias de la Salud | ORDINARIO |
| `b3000000-0000-0000-0000-000000000011` | UNFV | I | Ciencias de la Salud | ORDINARIO |
| `b3000000-0000-0000-0000-000000000012` | UNFV | II | Ingenierías y Ciencias Básicas | ORDINARIO |
| `b3000000-0000-0000-0000-000000000013` | UNFV | III | Ciencias Económicas | ORDINARIO |
| `b3000000-0000-0000-0000-000000000014` | UNFV | IV | Humanidades y Ciencias Sociales | ORDINARIO |
| `b3000000-0000-0000-0000-000000000015` | UNALM | UNICA | Ciencias Agrarias y Ambientales | ORDINARIO |

### Ponderaciones por área (fuente: prospectos oficiales 2024-2025)

**UNMSM Área A — Ciencias de la Salud:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 20.00 | No |
| Biología | 15.00 | No |
| Química | 15.00 | No |
| Matemática | 12.00 | No |
| Física | 10.00 | No |
| Historia del Perú | 8.00 | No |
| Geografía | 8.00 | No |
| Lengua y Literatura | 7.00 | No |
| Historia Universal | 5.00 | No |
| Total | **100.00** | |

**UNMSM Área B — Ingenierías y Ciencias Básicas:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Matemática | 20.00 | No |
| Física | 20.00 | No |
| Aptitud Académica | 20.00 | No |
| Química | 10.00 | No |
| Historia del Perú | 8.00 | No |
| Geografía | 8.00 | No |
| Biología | 7.00 | No |
| Lengua y Literatura | 7.00 | No |
| Total | **100.00** | |

**UNMSM Área C — Ciencias Económicas y de Gestión:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 25.00 | No |
| Matemática | 15.00 | No |
| Economía | 10.00 | No |
| Historia del Perú | 12.00 | No |
| Geografía | 12.00 | No |
| Lengua y Literatura | 10.00 | No |
| Física | 8.00 | No |
| Historia Universal | 8.00 | No |
| Total | **100.00** | |

**UNMSM Área D — Humanidades, CCSS y Educación:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 25.00 | No |
| Historia del Perú | 15.00 | No |
| Lengua y Literatura | 15.00 | No |
| Geografía | 10.00 | No |
| Historia Universal | 10.00 | No |
| Filosofía y Lógica | 10.00 | No |
| Matemática | 10.00 | No |
| Economía | 5.00 | No |
| Total | **100.00** | |

**UNI — Ingeniería y Ciencias (único área):**

| Curso | Peso % | Eliminatorio | Nota mínima |
|---|---|---|---|
| Matemática | 30.00 | Sí | 4.00 |
| Física | 25.00 | No | — |
| Química | 20.00 | No | — |
| Razonamiento Verbal | 15.00 | No | — |
| Aptitud Académica | 10.00 | No | — |
| Total | **100.00** | | |

**PUCP Ciencias e Ingeniería:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Matemática | 35.00 | No |
| Física | 25.00 | No |
| Aptitud Académica | 25.00 | No |
| Química | 15.00 | No |
| Total | **100.00** | |

**PUCP Humanidades, Gestión y Diseño:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 35.00 | No |
| Razonamiento Verbal | 25.00 | No |
| Historia del Perú | 20.00 | No |
| Economía | 20.00 | No |
| Total | **100.00** | |

**ULima — Examen General (único área):**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Razonamiento Matemático | 35.00 | No |
| Razonamiento Verbal | 35.00 | No |
| Aptitud Académica | 30.00 | No |
| Total | **100.00** | |

**UPCH — Ciencias de la Salud (único área):**

| Curso | Peso % | Eliminatorio | Nota mínima |
|---|---|---|---|
| Aptitud Académica | 30.00 | No | — |
| Biología | 25.00 | Sí | 5.00 |
| Química | 20.00 | No | — |
| Matemática | 15.00 | No | — |
| Física | 10.00 | No | — |
| Total | **100.00** | | |

**UNFV Área I — Ciencias de la Salud:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 20.00 | No |
| Biología | 18.00 | No |
| Química | 17.00 | No |
| Matemática | 12.00 | No |
| Física | 10.00 | No |
| Historia del Perú | 10.00 | No |
| Geografía | 8.00 | No |
| Lengua y Literatura | 5.00 | No |
| Total | **100.00** | |

**UNFV Área II — Ingenierías:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Matemática | 22.00 | No |
| Física | 20.00 | No |
| Aptitud Académica | 18.00 | No |
| Química | 15.00 | No |
| Historia del Perú | 10.00 | No |
| Geografía | 8.00 | No |
| Lengua y Literatura | 7.00 | No |
| Total | **100.00** | |

**UNFV Área III — Ciencias Económicas:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 22.00 | No |
| Matemática | 20.00 | No |
| Economía | 15.00 | No |
| Historia del Perú | 15.00 | No |
| Geografía | 13.00 | No |
| Lengua y Literatura | 10.00 | No |
| Física | 5.00 | No |
| Total | **100.00** | |

**UNFV Área IV — Humanidades y CCSS:**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 25.00 | No |
| Historia del Perú | 20.00 | No |
| Lengua y Literatura | 18.00 | No |
| Geografía | 15.00 | No |
| Historia Universal | 12.00 | No |
| Filosofía y Lógica | 10.00 | No |
| Total | **100.00** | |

**UNALM — Ciencias Agrarias y Ambientales (único área):**

| Curso | Peso % | Eliminatorio |
|---|---|---|
| Aptitud Académica | 25.00 | No |
| Biología | 20.00 | No |
| Química | 20.00 | No |
| Matemática | 20.00 | No |
| Física | 15.00 | No |
| Total | **100.00** | |

---

## Verificación post-seed

Ejecutar en pgAdmin después de aplicar V13 para validar integridad:

```sql
-- Verificar que la suma de pesos por área es exactamente 100.00
SELECT
    ea.code,
    ea.name,
    i.name AS universidad,
    SUM(aas.weight_percentage) AS total_peso
FROM exam_areas ea
JOIN area_subjects aas ON aas.exam_area_id = ea.id
JOIN institutions i ON i.id = ea.institution_id
GROUP BY ea.id, ea.code, ea.name, i.name
HAVING SUM(aas.weight_percentage) <> 100.00
ORDER BY i.name, ea.code;
-- Resultado esperado: 0 filas (todas las áreas suman 100.00)

-- Verificar cobertura: áreas por universidad
SELECT i.name, COUNT(ea.id) AS num_areas
FROM institutions i
LEFT JOIN exam_areas ea ON ea.institution_id = i.id
WHERE i.id LIKE 'b1000000-%'
GROUP BY i.name
ORDER BY i.name;
-- Resultado esperado: UNMSM=5, UNI=1, PUCP=2, ULima=1, UPCH=1, UNFV=4, UNALM=1

-- Verificar cursos eliminatorios
SELECT ea.name AS area, s.name AS curso, aas.weight_percentage, aas.min_score
FROM area_subjects aas
JOIN exam_areas ea ON ea.id = aas.exam_area_id
JOIN subjects s ON s.id = aas.subject_id
WHERE aas.is_eliminatory = true
ORDER BY ea.name;
-- Resultado esperado: Matemática en UNI (min_score=4.0), Biología en UPCH (min_score=5.0)
```

---

## Historial de cambios

| Versión | Fecha | Descripción |
|---|---|---|
| v1.0 | 2026-05-21 | Plan inicial — 7 universidades, 18 materias, 15 áreas, modalidad ORDINARIO |

> **Próxima expansión planeada**: Añadir modalidad CEPRE para UNMSM, UNI y PUCP (áreas con cursos y pesos diferenciados del proceso CEPRE). Migración V14.
