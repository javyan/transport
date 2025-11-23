-- =============================================
-- DATOS INICIALES PARA TESTING
-- =============================================

-- CLIENTES
INSERT INTO v2_clientes (nombre_completo, email, telefono, direccion, fecha_registro)
VALUES 
    ('Juan Pérez', 'juan.perez@email.com', '+5493511234567', 'Av. Colón 1234, Córdoba', CURRENT_TIMESTAMP),
    ('María González', 'maria.gonzalez@email.com', '+5493517654321', 'Av. Vélez Sársfield 5678, Córdoba', CURRENT_TIMESTAMP),
    ('Carlos Rodríguez', 'carlos.rodriguez@email.com', '+5493519876543', 'Av. Rafael Núñez 910, Córdoba', CURRENT_TIMESTAMP),
    ('Ana Martínez', 'ana.martinez@email.com', '+5491155554444', 'Av. Corrientes 1234, Buenos Aires', CURRENT_TIMESTAMP),
    ('Pedro López', 'pedro.lopez@email.com', '+5493415556666', 'San Martín 567, Rosario', CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- CONTENEDORES (asociados a clientes)
INSERT INTO v2_contenedores (cliente_id, numero_identificacion, peso_kg, volumen_m3, tipo, estado, fecha_creacion)
VALUES 
    (1, 'CONT001', 12000.0, 28.0, 'DRY', 'DISPONIBLE', CURRENT_TIMESTAMP),
    (2, 'CONT002', 8000.0, 20.0, 'REEFER', 'DISPONIBLE', CURRENT_TIMESTAMP),
    (3, 'CONT003', 15000.0, 33.0, 'DRY', 'DISPONIBLE', CURRENT_TIMESTAMP),
    (1, 'CONT004', 6000.0, 18.0, 'DRY', 'DISPONIBLE', CURRENT_TIMESTAMP),
    (2, 'CONT005', 10000.0, 25.0, 'REEFER', 'DISPONIBLE', CURRENT_TIMESTAMP)
ON CONFLICT (numero_identificacion) DO NOTHING;

-- SOLICITUDES DE PRUEBA (2 solicitudes listas para testing)
INSERT INTO v2_solicitudes (cliente_id, contenedor_id, origen_direccion, destino_direccion, estado, fecha_creacion, fecha_actualizacion)
VALUES 
    (1, 1, 'Buenos Aires, Argentina', 'Rosario, Argentina', 'BORRADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 'Rosario, Argentina', 'Córdoba, Argentina', 'BORRADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
