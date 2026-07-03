-- Habilita unaccent para que la búsqueda ILIKE de resources ignore tildes
-- (ej. "calculo" debe encontrar "Cálculo").

CREATE EXTENSION IF NOT EXISTS unaccent;
