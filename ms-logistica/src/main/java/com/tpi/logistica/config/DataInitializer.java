package com.tpi.logistica.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya hay datos
        Integer transportistasCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v2_transportistas", Integer.class);
        
        if (transportistasCount != null && transportistasCount == 0) {
            System.out.println("🔄 Cargando datos iniciales de Logística...");
            
            // Transportistas
            jdbcTemplate.execute("""
                INSERT INTO v2_transportistas (nombre_completo, dni, licencia_tipo, licencia_vencimiento, telefono, email, estado, fecha_registro)
                VALUES 
                    ('Roberto Gomez', '20123456', 'PROFESIONAL_C1', '2026-12-31', '5493515551111', 'roberto.gomez@transportes.com', 'DISPONIBLE', CURRENT_TIMESTAMP),
                    ('Laura Fernandez', '27654321', 'PROFESIONAL_C2', '2027-06-30', '5491144442222', 'laura.fernandez@transportes.com', 'DISPONIBLE', CURRENT_TIMESTAMP),
                    ('Carlos Martinez', '33987654', 'PROFESIONAL_C1', '2025-12-31', '5493415553333', 'carlos.martinez@transportes.com', 'DISPONIBLE', CURRENT_TIMESTAMP)
                ON CONFLICT (dni) DO NOTHING
            """);
            
            // Camiones
            jdbcTemplate.execute("""
                INSERT INTO v2_camiones (transportista_id, patente, marca, modelo, anio, capacidad_kg, capacidad_m3, consumo_combustible_lt_km, costo_km, estado, ubicacion_actual, lat_actual, lon_actual, fecha_registro)
                VALUES 
                    (1, 'AB123CD', 'Mercedes-Benz', 'Atego 1726', 2022, 8000.0, 45.0, 0.35, 150.0, 'DISPONIBLE', 'Córdoba, Argentina', -31.4201, -64.1888, CURRENT_TIMESTAMP),
                    (1, 'EF456GH', 'Iveco', 'Tector 170E28', 2021, 10000.0, 55.0, 0.40, 180.0, 'DISPONIBLE', 'Rosario, Argentina', -32.9442, -60.6505, CURRENT_TIMESTAMP),
                    (2, 'IJ789KL', 'Scania', 'P320', 2023, 15000.0, 75.0, 0.38, 200.0, 'DISPONIBLE', 'Buenos Aires, Argentina', -34.6037, -58.3816, CURRENT_TIMESTAMP),
                    (3, 'MN012OP', 'Volkswagen', 'Constellation 17.280', 2020, 12000.0, 60.0, 0.42, 170.0, 'EN_USO', 'Mendoza, Argentina', -32.8895, -68.8458, CURRENT_TIMESTAMP)
                ON CONFLICT (patente) DO NOTHING
            """);
            
            // Depósitos
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 1, 'Depósito Central Córdoba', 'Av. Circunvalación km 10, Córdoba', -31.3713, -64.2478, 500.0, 1500.0, 'ACTIVO', '5493514001000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 1)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 2, 'Depósito Rosario Norte', 'Parque Industrial Alvear, Rosario', -32.9200, -60.6800, 400.0, 1300.0, 'ACTIVO', '5493414002000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 2)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 3, 'Depósito Buenos Aires Sur', 'Av. Gral. Paz km 12, Buenos Aires', -34.7000, -58.5000, 600.0, 2000.0, 'ACTIVO', '5491144003000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 3)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 4, 'Depósito Mendoza Centro', 'Ruta 40 km 15, Mendoza', -32.8500, -68.8200, 450.0, 1400.0, 'ACTIVO', '5492614004000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 4)
            """);
            
            System.out.println("✅ Datos iniciales de Logística cargados: 3 transportistas, 4 camiones, 4 depósitos");
        } else {
            System.out.println("ℹ️ Datos de Logística ya existen, omitiendo carga inicial");
        }
    }
}
