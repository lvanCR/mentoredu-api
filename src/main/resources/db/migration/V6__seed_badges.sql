-- V6: Seed de insignias base del sistema (US32 — EP-10 Gamification)
-- Las insignias son concedidas automáticamente por el sistema al alcanzar hitos definidos (RN-31, RN-33).

INSERT INTO badges (id, code, name, description) VALUES
  (gen_random_uuid(), 'FIRST_ANSWER',    'Primera Respuesta',  'Otorgada al publicar tu primera respuesta en el foro.'),
  (gen_random_uuid(), 'FIRST_RESOURCE',  'Primer Recurso',     'Otorgada al publicar tu primer recurso académico.'),
  (gen_random_uuid(), 'LEVEL_5',         'Nivel 5 Alcanzado',  'Otorgada al alcanzar el nivel 5 de experiencia.'),
  (gen_random_uuid(), 'LEVEL_10',        'Nivel 10 Alcanzado', 'Otorgada al alcanzar el nivel 10 de experiencia.')
ON CONFLICT DO NOTHING;
