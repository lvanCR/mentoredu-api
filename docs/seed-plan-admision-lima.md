# Plan de Seed Data — Catálogo Universitario Lima, Perú (v2.0)

> **Fuente de verdad**: Este archivo documenta el diseño y contenido de `V9__seed_catalog.sql`.  
> Toda modificación a los datos semilla debe registrarse aquí antes de tocar la migración.

---

## Principios de diseño

1. **UUIDs fijos por dominio** — Ningún seed usa `gen_random_uuid()`. Los prefijos son reconocibles para referencias cruzadas entre tablas y ambientes.
2. **Idempotente** — Todos los INSERTs usan `ON CONFLICT (...) DO NOTHING`. Seguros de correr N veces.
3. **Tablas del esquema v2.0** — `universities`, `areas`, `courses`, `area_courses`, `careers`, `career_courses`. Alineadas 100% con el ER y el diagrama de clases.
4. **Solo ADMIN modifica** — Datos de solo lectura para usuarios finales (RN-20). Correcciones post-deploy se hacen con una nueva migración Vxx.
5. **career_courses derivado** — La tabla `career_courses` se genera con un SELECT sobre `careers × area_courses`. No se inserta manualmente fila por fila.

---

## Prefijos de UUID por dominio

| Dominio      | Prefijo                                | Rango  |
|---|---|---|
| universities | `b1000000-0000-0000-0000-0000000000XX` | 01–07  |
| courses      | `b2000000-0000-0000-0000-0000000000XX` | 01–18  |
| areas        | `b3000000-0000-0000-0000-0000000000XX` | 01–15  |
| careers      | `b4000000-0000-0000-0000-0000000000XX` | 01–96  |

---

## Orden de inserción en V9

```
1. universities    → 7 filas
2. courses         → 18 filas
3. areas           → 15 filas   (FK → universities)
4. area_courses    → ~70 filas  (FK → areas + courses)
5. careers         → 96 filas   (FK → universities + areas)
6. career_courses  → ~generado  (SELECT careers × area_courses por area_id)
```

---

## Universidades (7 de Lima)

| UUID | Nombre | Ciudad |
|---|---|---|
| `b1000000-...01` | UNMSM - Universidad Nacional Mayor de San Marcos | Lima |
| `b1000000-...02` | UNI - Universidad Nacional de Ingeniería | Lima |
| `b1000000-...03` | PUCP - Pontificia Universidad Católica del Perú | Lima |
| `b1000000-...04` | ULima - Universidad de Lima | Lima |
| `b1000000-...05` | UPCH - Universidad Peruana Cayetano Heredia | Lima |
| `b1000000-...06` | UNFV - Universidad Nacional Federico Villarreal | Lima |
| `b1000000-...07` | UNALM - Universidad Nacional Agraria La Molina | La Molina |

---

## Cursos (18 materias transversales de los prospectos de admisión)

| UUID | Nombre |
|---|---|
| `b2000000-...01` | Matemática |
| `b2000000-...02` | Física |
| `b2000000-...03` | Química |
| `b2000000-...04` | Biología |
| `b2000000-...05` | Lengua y Literatura |
| `b2000000-...06` | Historia del Perú |
| `b2000000-...07` | Historia Universal |
| `b2000000-...08` | Geografía |
| `b2000000-...09` | Economía |
| `b2000000-...10` | Filosofía y Lógica |
| `b2000000-...11` | Aptitud Académica |
| `b2000000-...12` | Razonamiento Matemático |
| `b2000000-...13` | Razonamiento Verbal |
| `b2000000-...14` | Álgebra |
| `b2000000-...15` | Aritmética |
| `b2000000-...16` | Geometría |
| `b2000000-...17` | Trigonometría |
| `b2000000-...18` | Inglés |

> Los cursos 14–18 (Álgebra, Aritmética, Geometría, Trigonometría, Inglés) están disponibles en el catálogo para que docentes y estudiantes publiquen o busquen recursos de esas materias, aunque no sean parte del examen de ninguna área específica en este seed inicial.

---

## Áreas y sus cursos (fuente: prospectos oficiales 2024-2025)

| UUID | Universidad | Nombre del área | Cursos del examen |
|---|---|---|---|
| `b3000000-...01` | UNMSM | Área A — Ciencias de la Salud | Aptitud Académica, Biología, Química, Matemática, Física, Hist. Perú, Geografía, Lengua, Hist. Universal |
| `b3000000-...02` | UNMSM | Área B — Ingenierías y Ciencias Básicas | Matemática, Física, Aptitud Académica, Química, Hist. Perú, Geografía, Biología, Lengua |
| `b3000000-...03` | UNMSM | Área C — Ciencias Económicas y de Gestión | Aptitud Académica, Matemática, Economía, Hist. Perú, Geografía, Lengua, Física, Hist. Universal |
| `b3000000-...04` | UNMSM | Área D — Humanidades, CCSS y Educación | Aptitud Académica, Hist. Perú, Lengua, Geografía, Hist. Universal, Filosofía y Lógica, Matemática, Economía |
| `b3000000-...05` | UNMSM | Área E — Arte y Diseño | Aptitud Académica, Lengua, Hist. Perú |
| `b3000000-...06` | UNI | Ingeniería y Ciencias | Matemática, Física, Química, Razonamiento Verbal, Aptitud Académica |
| `b3000000-...07` | PUCP | Ciencias e Ingeniería | Matemática, Física, Aptitud Académica, Química |
| `b3000000-...08` | PUCP | Humanidades, Gestión y Diseño | Aptitud Académica, Razonamiento Verbal, Hist. Perú, Economía |
| `b3000000-...09` | ULima | Examen General | Razonamiento Matemático, Razonamiento Verbal, Aptitud Académica |
| `b3000000-...10` | UPCH | Ciencias de la Salud | Aptitud Académica, Biología, Química, Matemática, Física |
| `b3000000-...11` | UNFV | Área I — Ciencias de la Salud | Aptitud Académica, Biología, Química, Matemática, Física, Hist. Perú, Geografía, Lengua |
| `b3000000-...12` | UNFV | Área II — Ingenierías y Ciencias Básicas | Matemática, Física, Aptitud Académica, Química, Hist. Perú, Geografía, Lengua |
| `b3000000-...13` | UNFV | Área III — Ciencias Económicas | Aptitud Académica, Matemática, Economía, Hist. Perú, Geografía, Lengua, Física |
| `b3000000-...14` | UNFV | Área IV — Humanidades y CCSS | Aptitud Académica, Hist. Perú, Lengua, Geografía, Hist. Universal, Filosofía y Lógica |
| `b3000000-...15` | UNALM | Ciencias Agrarias y Ambientales | Aptitud Académica, Biología, Química, Matemática, Física |

---

## Carreras (96 carreras — oferta académica oficial 2024)

### UNMSM (29 carreras)

**Área A — Ciencias de la Salud** (`b3000000-...01`)

| UUID | Carrera |
|---|---|
| `b4000000-...01` | Medicina Humana |
| `b4000000-...02` | Odontología |
| `b4000000-...03` | Farmacia y Bioquímica |
| `b4000000-...04` | Enfermería |
| `b4000000-...05` | Nutrición |
| `b4000000-...06` | Obstetricia |
| `b4000000-...07` | Psicología |
| `b4000000-...08` | Tecnología Médica |

**Área B — Ingenierías y Ciencias Básicas** (`b3000000-...02`)

| UUID | Carrera |
|---|---|
| `b4000000-...09` | Ingeniería de Sistemas |
| `b4000000-...10` | Ingeniería Industrial |
| `b4000000-...11` | Ingeniería Civil |
| `b4000000-...12` | Ingeniería Electrónica |
| `b4000000-...13` | Ingeniería Química |
| `b4000000-...14` | Física |
| `b4000000-...15` | Matemática |
| `b4000000-...16` | Estadística e Informática |

**Área C — Ciencias Económicas y de Gestión** (`b3000000-...03`)

| UUID | Carrera |
|---|---|
| `b4000000-...17` | Economía |
| `b4000000-...18` | Administración |
| `b4000000-...19` | Contabilidad |
| `b4000000-...20` | Negocios Internacionales |

**Área D — Humanidades, CCSS y Educación** (`b3000000-...04`)

| UUID | Carrera |
|---|---|
| `b4000000-...21` | Derecho |
| `b4000000-...22` | Literatura |
| `b4000000-...23` | Historia |
| `b4000000-...24` | Filosofía |
| `b4000000-...25` | Sociología |
| `b4000000-...26` | Comunicación Social |
| `b4000000-...27` | Educación |
| `b4000000-...28` | Trabajo Social |

**Área E — Arte y Diseño** (`b3000000-...05`)

| UUID | Carrera |
|---|---|
| `b4000000-...29` | Arte |

---

### UNI (9 carreras — Área: Ingeniería y Ciencias `b3000000-...06`)

| UUID | Carrera |
|---|---|
| `b4000000-...30` | Ingeniería Civil |
| `b4000000-...31` | Ingeniería Mecánica |
| `b4000000-...32` | Ingeniería Eléctrica |
| `b4000000-...33` | Ingeniería de Sistemas |
| `b4000000-...34` | Ingeniería Industrial |
| `b4000000-...35` | Ingeniería Ambiental |
| `b4000000-...36` | Ingeniería Química |
| `b4000000-...37` | Arquitectura |
| `b4000000-...38` | Ingeniería Mecatrónica |

---

### PUCP (16 carreras)

**Ciencias e Ingeniería** (`b3000000-...07`)

| UUID | Carrera |
|---|---|
| `b4000000-...39` | Ingeniería Civil |
| `b4000000-...40` | Ingeniería Industrial |
| `b4000000-...41` | Ingeniería Mecánica |
| `b4000000-...42` | Ingeniería Electrónica |
| `b4000000-...43` | Ingeniería Informática |
| `b4000000-...44` | Física |
| `b4000000-...45` | Matemáticas |

**Humanidades, Gestión y Diseño** (`b3000000-...08`)

| UUID | Carrera |
|---|---|
| `b4000000-...46` | Administración de Empresas |
| `b4000000-...47` | Economía |
| `b4000000-...48` | Contabilidad |
| `b4000000-...49` | Derecho |
| `b4000000-...50` | Ciencias Sociales |
| `b4000000-...51` | Comunicaciones |
| `b4000000-...52` | Psicología |
| `b4000000-...53` | Arte y Diseño Gráfico |
| `b4000000-...54` | Educación |

---

### ULima (9 carreras — Área: Examen General `b3000000-...09`)

| UUID | Carrera |
|---|---|
| `b4000000-...55` | Administración |
| `b4000000-...56` | Economía |
| `b4000000-...57` | Derecho |
| `b4000000-...58` | Ingeniería de Sistemas |
| `b4000000-...59` | Ingeniería Industrial |
| `b4000000-...60` | Comunicaciones |
| `b4000000-...61` | Psicología |
| `b4000000-...62` | Arquitectura |
| `b4000000-...63` | Marketing |

---

### UPCH (6 carreras — Área: Ciencias de la Salud `b3000000-...10`)

| UUID | Carrera |
|---|---|
| `b4000000-...64` | Medicina |
| `b4000000-...65` | Enfermería |
| `b4000000-...66` | Nutrición y Dietética |
| `b4000000-...67` | Psicología |
| `b4000000-...68` | Estomatología |
| `b4000000-...69` | Tecnología Médica |

---

### UNFV (19 carreras)

**Área I — Ciencias de la Salud** (`b3000000-...11`)

| UUID | Carrera |
|---|---|
| `b4000000-...70` | Medicina Humana |
| `b4000000-...71` | Enfermería |
| `b4000000-...72` | Nutrición |
| `b4000000-...73` | Obstetricia |
| `b4000000-...74` | Psicología |

**Área II — Ingenierías** (`b3000000-...12`)

| UUID | Carrera |
|---|---|
| `b4000000-...75` | Ingeniería Civil |
| `b4000000-...76` | Ingeniería Electrónica |
| `b4000000-...77` | Ingeniería de Sistemas |
| `b4000000-...78` | Arquitectura |
| `b4000000-...79` | Ingeniería Agroindustrial |

**Área III — Ciencias Económicas** (`b3000000-...13`)

| UUID | Carrera |
|---|---|
| `b4000000-...80` | Administración |
| `b4000000-...81` | Contabilidad |
| `b4000000-...82` | Economía |
| `b4000000-...83` | Turismo y Hotelería |

**Área IV — Humanidades y CCSS** (`b3000000-...14`)

| UUID | Carrera |
|---|---|
| `b4000000-...84` | Derecho |
| `b4000000-...85` | Educación |
| `b4000000-...86` | Trabajo Social |
| `b4000000-...87` | Historia |
| `b4000000-...88` | Sociología |

---

### UNALM (8 carreras — Área: Ciencias Agrarias y Ambientales `b3000000-...15`)

| UUID | Carrera |
|---|---|
| `b4000000-...89` | Agronomía |
| `b4000000-...90` | Ingeniería Forestal |
| `b4000000-...91` | Biología |
| `b4000000-...92` | Economía y Planificación |
| `b4000000-...93` | Ingeniería Ambiental |
| `b4000000-...94` | Zootecnia |
| `b4000000-...95` | Ingeniería Alimentaria |
| `b4000000-...96` | Ingeniería Agrícola |

---

## Relación career_courses

Cada carrera hereda automáticamente los cursos del área a la que pertenece. La migración V9 lo genera con:

```sql
INSERT INTO career_courses (career_id, course_id)
SELECT c.id, ac.course_id
FROM careers c
JOIN area_courses ac ON ac.area_id = c.area_id
ON CONFLICT (career_id, course_id) DO NOTHING;
```

Esto produce ~490 filas sin duplicar lógica de inserción.

---

## Verificación post-seed

```sql
-- Resumen por universidad
SELECT u.name,
       COUNT(DISTINCT a.id)  AS areas,
       COUNT(DISTINCT c.id)  AS carreras
FROM universities u
LEFT JOIN areas a    ON a.university_id = u.id
LEFT JOIN careers c  ON c.university_id = u.id
WHERE u.id LIKE 'b1000000-%'
GROUP BY u.name
ORDER BY u.name;
-- Esperado: UNMSM=5á/29c, UNI=1á/9c, PUCP=2á/16c, ULima=1á/9c, UPCH=1á/6c, UNFV=4á/19c, UNALM=1á/8c

-- Verificar que todas las carreras tienen cursos asociados
SELECT c.name AS carrera, COUNT(cc.course_id) AS num_cursos
FROM careers c
LEFT JOIN career_courses cc ON cc.career_id = c.id
GROUP BY c.name
HAVING COUNT(cc.course_id) = 0;
-- Resultado esperado: 0 filas (ninguna carrera sin cursos)

-- Cursos por área
SELECT u.name AS universidad, a.name AS area, COUNT(ac.course_id) AS num_cursos
FROM universities u
JOIN areas a        ON a.university_id = u.id
JOIN area_courses ac ON ac.area_id = a.id
GROUP BY u.name, a.name
ORDER BY u.name, a.name;
```

---

## Historial de cambios

| Versión | Fecha      | Descripción |
|---|---|---|
| v1.0 | 2026-05-21 | Plan inicial — esquema admisiones legacy (institutions, exam_areas, subjects, ponderaciones) |
| v2.0 | 2026-05-22 | Reescrito completo: esquema v2.0, sin ponderaciones, agregadas careers y career_courses (96 carreras, 7 universidades, 15 áreas, 18 cursos) |
