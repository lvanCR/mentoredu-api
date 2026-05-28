-- Elimina el campo visibility (vestigial del BC billing eliminado en v2.0).
-- Todos los registros existentes tienen 'PUBLIC' (NOT NULL DEFAULT 'PUBLIC').
ALTER TABLE resources DROP COLUMN visibility;
