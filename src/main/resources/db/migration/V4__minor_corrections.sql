-- V4: Correcciones menores de constraints
-- academic_resources: UNIQUE en file_id (refuerza relación 1:1 con resource_files)

BEGIN;

ALTER TABLE academic_resources
    ADD CONSTRAINT uq_resources_file_id UNIQUE (file_id);

COMMIT;
