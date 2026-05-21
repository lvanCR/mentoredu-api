-- V5: Seed data for billing — plans and coin packages
-- Required for US23 (activate premium subscription) and US24 (buy coin package)

INSERT INTO plans (id, name, price, duration_days, description)
VALUES
  (gen_random_uuid(), 'MENSUAL',    19.90,  30, 'Acceso premium por 30 días'),
  (gen_random_uuid(), 'TRIMESTRAL', 49.90,  90, 'Acceso premium por 90 días con descuento'),
  (gen_random_uuid(), 'ANUAL',     149.90, 365, 'Acceso premium por 365 días, mejor valor')
ON CONFLICT DO NOTHING;

INSERT INTO coin_packages (id, name, coins, price, active)
VALUES
  (gen_random_uuid(), 'STARTER', 100, 5.00,  true),
  (gen_random_uuid(), 'PRO',     300, 12.00, true),
  (gen_random_uuid(), 'ELITE',   700, 25.00, true)
ON CONFLICT DO NOTHING;
