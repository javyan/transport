-- =============================================
-- SCHEMA: Microservicio de Logística (v2)
-- Tablas: transportistas, camiones, depósitos, tramos
-- =============================================

-- Tabla: Transportistas
CREATE TABLE IF NOT EXISTS v2_transportistas (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    licencia_tipo VARCHAR(50) NOT NULL,
    licencia_vencimiento DATE NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: Camiones
CREATE TABLE IF NOT EXISTS v2_camiones (
    id BIGSERIAL PRIMARY KEY,
    transportista_id BIGINT,
    patente VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    capacidad_kg DOUBLE PRECISION NOT NULL,
    capacidad_m3 DOUBLE PRECISION NOT NULL,
    consumo_combustible_lt_km DOUBLE PRECISION NOT NULL,
    costo_km DOUBLE PRECISION NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'DISPONIBLE',
    ubicacion_actual VARCHAR(500),
    lat_actual DOUBLE PRECISION,
    lon_actual DOUBLE PRECISION,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_camion_transportista FOREIGN KEY (transportista_id)
        REFERENCES v2_transportistas(id) ON DELETE SET NULL
);

-- Tabla: Depósitos
CREATE TABLE IF NOT EXISTS v2_depositos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    capacidad_maxima_m3 DOUBLE PRECISION NOT NULL,
    costo_dia DOUBLE PRECISION NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    telefono VARCHAR(50),
    observaciones TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: Rutas (agrupación de tramos)
CREATE TABLE IF NOT EXISTS v2_rutas (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'TENTATIVA', -- 'TENTATIVA', 'ASIGNADA', 'COMPLETADA', 'CANCELADA'
    cantidad_tramos INTEGER NOT NULL,
    depositos_intermedios VARCHAR(500), -- IDs separados por comas
    distancia_total_km DOUBLE PRECISION NOT NULL,
    costo_total_estimado DOUBLE PRECISION NOT NULL,
    costo_total_real DOUBLE PRECISION,
    tiempo_estimado_horas DOUBLE PRECISION NOT NULL,
    tiempo_real_horas DOUBLE PRECISION,
    estrategia VARCHAR(50) NOT NULL, -- 'DIRECTA', 'UN_DEPOSITO', 'MULTIPLES_DEPOSITOS'
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_asignacion TIMESTAMP,
    fecha_completada TIMESTAMP,
    observaciones VARCHAR(1000)
);

-- Tabla: Tramos (segmentos de ruta)
CREATE TABLE IF NOT EXISTS v2_tramos (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    ruta_id BIGINT,
    camion_id BIGINT,
    transportista_id BIGINT,
    origen_tipo VARCHAR(50) NOT NULL, -- 'CLIENTE', 'DEPOSITO'
    origen_id BIGINT, -- ID del depósito si origen_tipo='DEPOSITO', NULL si 'CLIENTE'
    origen_direccion VARCHAR(500) NOT NULL,
    destino_tipo VARCHAR(50) NOT NULL, -- 'CLIENTE', 'DEPOSITO'
    destino_id BIGINT, -- ID del depósito si destino_tipo='DEPOSITO', NULL si 'CLIENTE'
    destino_direccion VARCHAR(500) NOT NULL,
    tipo_tramo VARCHAR(50) NOT NULL, -- 'ORIGEN_DEPOSITO', 'DEPOSITO_DEPOSITO', 'DEPOSITO_DESTINO', 'DIRECTO'
    distancia_km DOUBLE PRECISION NOT NULL,
    orden_tramo INTEGER NOT NULL, -- Orden en la ruta (1, 2, 3...)
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'EN_TRANSITO', 'COMPLETADO', 'CANCELADO'
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tramo_ruta FOREIGN KEY (ruta_id)
        REFERENCES v2_rutas(id) ON DELETE SET NULL,
    CONSTRAINT fk_tramo_camion FOREIGN KEY (camion_id)
        REFERENCES v2_camiones(id) ON DELETE SET NULL,
    CONSTRAINT fk_tramo_transportista FOREIGN KEY (transportista_id)
        REFERENCES v2_transportistas(id) ON DELETE SET NULL,
    CONSTRAINT fk_tramo_deposito_origen FOREIGN KEY (origen_id)
        REFERENCES v2_depositos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tramo_deposito_destino FOREIGN KEY (destino_id)
        REFERENCES v2_depositos(id) ON DELETE RESTRICT
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_v2_camiones_transportista ON v2_camiones(transportista_id);
CREATE INDEX IF NOT EXISTS idx_v2_camiones_estado ON v2_camiones(estado);
CREATE INDEX IF NOT EXISTS idx_v2_rutas_solicitud ON v2_rutas(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_v2_rutas_estado ON v2_rutas(estado);
CREATE INDEX IF NOT EXISTS idx_v2_tramos_solicitud ON v2_tramos(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_v2_tramos_ruta ON v2_tramos(ruta_id);
CREATE INDEX IF NOT EXISTS idx_v2_tramos_camion ON v2_tramos(camion_id);
CREATE INDEX IF NOT EXISTS idx_v2_tramos_estado ON v2_tramos(estado);
CREATE INDEX IF NOT EXISTS idx_v2_tramos_orden ON v2_tramos(solicitud_id, orden_tramo);
