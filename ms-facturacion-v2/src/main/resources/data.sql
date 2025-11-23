-- =============================================
-- DATA: Datos iniciales para desarrollo/testing
-- =============================================

-- Tarifa vigente actual
INSERT INTO v2_tarifas (descripcion, cargo_gestion_base, cargo_gestion_por_tramo, precio_combustible_litro, factor_estadia_dia, fecha_vigencia_desde, fecha_vigencia_hasta, estado, fecha_creacion)
VALUES 
    ('Tarifa Estándar 2025', 5000.0, 2000.0, 850.0, 1.0, '2025-01-01', NULL, 'ACTIVA', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Nota: Las facturas y estadías se crean dinámicamente vía API
