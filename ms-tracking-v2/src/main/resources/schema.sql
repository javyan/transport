-- ================================================
-- MS-TRACKING-V2 - Schema de Base de Datos
-- Tabla de eventos de tracking/seguimiento
-- ================================================

-- Tabla de eventos de tracking
CREATE TABLE IF NOT EXISTS v2_tracking_eventos (
    id BIGSERIAL PRIMARY KEY,
    contenedor_id BIGINT,
    solicitud_id BIGINT,
    tramo_id BIGINT,
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    observaciones TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_tracking_contenedor ON v2_tracking_eventos(contenedor_id);
CREATE INDEX IF NOT EXISTS idx_tracking_solicitud ON v2_tracking_eventos(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_tracking_tramo ON v2_tracking_eventos(tramo_id);
CREATE INDEX IF NOT EXISTS idx_tracking_tipo ON v2_tracking_eventos(tipo_evento);
CREATE INDEX IF NOT EXISTS idx_tracking_fecha ON v2_tracking_eventos(fecha_hora DESC);

-- Comentarios
COMMENT ON TABLE v2_tracking_eventos IS 'Eventos de seguimiento de contenedores, solicitudes y tramos';
COMMENT ON COLUMN v2_tracking_eventos.tipo_evento IS 'CREADO, ASIGNADO, EN_TRANSITO, DEPOSITADO, RETIRADO, ENTREGADO, INCIDENCIA';
COMMENT ON COLUMN v2_tracking_eventos.descripcion IS 'Descripción detallada del evento';
COMMENT ON COLUMN v2_tracking_eventos.lat IS 'Latitud donde ocurrió el evento';
COMMENT ON COLUMN v2_tracking_eventos.lon IS 'Longitud donde ocurrió el evento';
