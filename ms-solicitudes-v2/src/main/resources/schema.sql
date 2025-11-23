-- Tabla v2_clientes
CREATE TABLE IF NOT EXISTS v2_clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    direccion VARCHAR(500),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla v2_contenedores
CREATE TABLE IF NOT EXISTS v2_contenedores (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    numero_identificacion VARCHAR(100) UNIQUE NOT NULL,
    peso_kg DOUBLE PRECISION NOT NULL,
    volumen_m3 DOUBLE PRECISION NOT NULL,
    tipo VARCHAR(100) NOT NULL DEFAULT 'ESTANDAR',
    estado VARCHAR(50) NOT NULL DEFAULT 'CREADO',
    ubicacion_actual VARCHAR(500),
    lat_actual DOUBLE PRECISION,
    lon_actual DOUBLE PRECISION,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT fk_contenedor_cliente FOREIGN KEY (cliente_id) 
        REFERENCES v2_clientes(id) ON DELETE CASCADE
);

-- Tabla v2_solicitudes
CREATE TABLE IF NOT EXISTS v2_solicitudes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    contenedor_id BIGINT NOT NULL,
    
    -- Origen
    origen_direccion VARCHAR(500) NOT NULL,
    
    -- Destino
    destino_direccion VARCHAR(500) NOT NULL,
    
    -- Estado y costos
    estado VARCHAR(50) NOT NULL DEFAULT 'BORRADOR',
    costo_estimado DOUBLE PRECISION,
    tiempo_estimado_horas DOUBLE PRECISION,
    costo_real DOUBLE PRECISION,
    tiempo_real_horas DOUBLE PRECISION,
    
    -- Auditoría
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega TIMESTAMP,
    
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (cliente_id) 
        REFERENCES v2_clientes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_contenedor FOREIGN KEY (contenedor_id) 
        REFERENCES v2_contenedores(id) ON DELETE RESTRICT
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_v2_contenedores_cliente ON v2_contenedores(cliente_id);
CREATE INDEX IF NOT EXISTS idx_v2_contenedores_estado ON v2_contenedores(estado);
CREATE INDEX IF NOT EXISTS idx_v2_solicitudes_cliente ON v2_solicitudes(cliente_id);
CREATE INDEX IF NOT EXISTS idx_v2_solicitudes_contenedor ON v2_solicitudes(contenedor_id);
CREATE INDEX IF NOT EXISTS idx_v2_solicitudes_estado ON v2_solicitudes(estado);
CREATE INDEX IF NOT EXISTS idx_v2_solicitudes_fecha ON v2_solicitudes(fecha_creacion DESC);
