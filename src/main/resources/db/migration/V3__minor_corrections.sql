-- V3: Correcciones menores de nomenclatura y constraints
-- 1. notification_preferences: community_enabled → forum_enabled (alineado al BC forum)
-- 2. academic_resources: UNIQUE en file_id (refuerza relación 1:1 con resource_files)

BEGIN;

ALTER TABLE notification_preferences RENAME COLUMN community_enabled TO forum_enabled;

ALTER TABLE academic_resources
    ADD CONSTRAINT uq_resources_file_id UNIQUE (file_id);

COMMIT;
