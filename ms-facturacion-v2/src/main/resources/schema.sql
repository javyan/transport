-- =============================================
-- SCHEMA: Microservicio de Facturación (v2)
-- Tablas: tarifas, facturas, estadias_deposito
-- =============================================

-- Tabla: Tarifas (precios vigentes del sistema)
CREATE TABLE IF NOT EXISTS v2_tarifas (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    cargo_gestion_base DOUBLE PRECISION NOT NULL, -- Cargo fijo base
    cargo_gestion_por_tramo DOUBLE PRECISION NOT NULL, -- Adicional por cada tramo
    precio_combustible_litro DOUBLE PRECISION NOT NULL, -- Precio actual del combustible
    factor_estadia_dia DOUBLE PRECISION NOT NULL, -- Multiplicador para estadías en depósito
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVA', -- 'ACTIVA', 'VENCIDA'
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vigencia CHECK (fecha_vigencia_hasta IS NULL OR fecha_vigencia_hasta >= fecha_vigencia_desde)
);

-- Tabla: Facturas
CREATE TABLE IF NOT EXISTS v2_facturas (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL, -- FK lógica a v2_solicitudes
    tarifa_id BIGINT NOT NULL,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    
    -- Desglose de costos
    cargo_gestion DOUBLE PRECISION NOT NULL,
    costo_transporte DOUBLE PRECISION NOT NULL, -- Suma de (distancia × costo_km) de todos los tramos
    costo_combustible DOUBLE PRECISION NOT NULL, -- Suma de (consumo × precio_litro) de todos los tramos
    costo_estadias DOUBLE PRECISION NOT NULL DEFAULT 0.0, -- Suma de estadías en depósitos
    
    -- Totales
    subtotal DOUBLE PRECISION NOT NULL,
    impuestos DOUBLE PRECISION NOT NULL DEFAULT 0.0, -- 21% IVA
    total DOUBLE PRECISION NOT NULL,
    
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'PAGADA', 'CANCELADA'
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago TIMESTAMP,
    observaciones TEXT,
    
    CONSTRAINT fk_factura_tarifa FOREIGN KEY (tarifa_id)
        REFERENCES v2_tarifas(id) ON DELETE RESTRICT
);

-- Tabla: Estadías en Depósito
CREATE TABLE IF NOT EXISTS v2_estadias_deposito (
    id BIGSERIAL PRIMARY KEY,
    contenedor_id BIGINT NOT NULL, -- FK lógica a v2_contenedores
    deposito_id BIGINT NOT NULL, -- FK lógica a v2_depositos
    fecha_entrada TIMESTAMP NOT NULL,
    fecha_salida TIMESTAMP,
    dias_estadia INTEGER, -- Se calcula al momento de salida
    costo_dia DOUBLE PRECISION NOT NULL, -- Costo del depósito por día (copiado en el momento)
    costo_total DOUBLE PRECISION, -- dias_estadia × costo_dia
    estado VARCHAR(50) NOT NULL DEFAULT 'EN_CURSO', -- 'EN_CURSO', 'FINALIZADA'
    observaciones TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_v2_tarifas_vigencia ON v2_tarifas(fecha_vigencia_desde, fecha_vigencia_hasta, estado);
CREATE INDEX IF NOT EXISTS idx_v2_facturas_solicitud ON v2_facturas(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_v2_facturas_estado ON v2_facturas(estado);
CREATE INDEX IF NOT EXISTS idx_v2_facturas_numero ON v2_facturas(numero_factura);
CREATE INDEX IF NOT EXISTS idx_v2_estadias_contenedor ON v2_estadias_deposito(contenedor_id);
CREATE INDEX IF NOT EXISTS idx_v2_estadias_deposito ON v2_estadias_deposito(deposito_id);
CREATE INDEX IF NOT EXISTS idx_v2_estadias_estado ON v2_estadias_deposito(estado);
