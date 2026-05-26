-- OBS-M3: índice GIN con pg_trgm para búsqueda LIKE '%query%' eficiente en resources
-- Sin este índice, la búsqueda por título hace full table scan en PostgreSQL.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_resources_title_trgm
    ON resources USING GIN (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_resources_description_trgm
    ON resources USING GIN (description gin_trgm_ops);
