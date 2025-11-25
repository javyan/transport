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
            
            // Depósitos estratégicos en provincias argentinas (excluye provincias limítrofes de Buenos Aires)
            // Provincias del NORTE
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 1, 'Depósito Salta', 'Parque Industrial Salta, Salta Capital', -24.7859, -65.4117, 500.0, 1400.0, 'ACTIVO', '5493874001000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 1)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 2, 'Depósito Jujuy', 'Av. Almirante Brown 2500, San Salvador de Jujuy', -24.1858, -65.2995, 400.0, 1300.0, 'ACTIVO', '5493884002000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 2)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 3, 'Depósito Tucumán', 'Ruta 9 km 1295, San Miguel de Tucumán', -26.8083, -65.2176, 450.0, 1350.0, 'ACTIVO', '5493814003000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 3)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 4, 'Depósito Santiago del Estero', 'Parque Industrial La Banda, Santiago del Estero', -27.7833, -64.2642, 400.0, 1200.0, 'ACTIVO', '5493854004000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 4)
            """);
            
            // Provincias del CENTRO
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 5, 'Depósito Córdoba', 'Av. Circunvalación km 10, Córdoba Capital', -31.3713, -64.2478, 600.0, 1500.0, 'ACTIVO', '5493514005000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 5)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 6, 'Depósito La Rioja', 'Ruta Nacional 38 km 5, La Rioja Capital', -29.4131, -66.8558, 350.0, 1250.0, 'ACTIVO', '5493804006000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 6)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 7, 'Depósito Catamarca', 'Av. Güemes 1500, San Fernando del Valle', -28.4696, -65.7795, 350.0, 1200.0, 'ACTIVO', '5493834007000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 7)
            """);
            
            // Provincias de CUYO
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 8, 'Depósito Mendoza', 'Ruta 40 km 15, Mendoza Capital', -32.8500, -68.8200, 550.0, 1600.0, 'ACTIVO', '5492614008000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 8)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 9, 'Depósito San Juan', 'Av. Libertador San Martín 2000, San Juan Capital', -31.5375, -68.5364, 450.0, 1400.0, 'ACTIVO', '5492644009000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 9)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 10, 'Depósito San Luis', 'Ruta 147 km 3, San Luis Capital', -33.3017, -66.3378, 400.0, 1300.0, 'ACTIVO', '5492664010000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 10)
            """);
            
            // Provincias de la PATAGONIA
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 11, 'Depósito Neuquén', 'Parque Industrial Neuquén, Neuquén Capital', -38.9516, -68.0591, 500.0, 1500.0, 'ACTIVO', '5492994011000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 11)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 12, 'Depósito Río Negro', 'Ruta 22 km 1200, Viedma', -40.8135, -62.9967, 450.0, 1400.0, 'ACTIVO', '5492924012000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 12)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 13, 'Depósito Chubut', 'Av. Gales 500, Trelew', -43.2489, -65.3050, 450.0, 1450.0, 'ACTIVO', '5492804013000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 13)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 14, 'Depósito Santa Cruz', 'Ruta 3 km 2500, Río Gallegos', -51.6226, -69.2181, 400.0, 1700.0, 'ACTIVO', '5492964014000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 14)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 15, 'Depósito Tierra del Fuego', 'Av. Maipú 1200, Ushuaia', -54.8019, -68.3029, 350.0, 2000.0, 'ACTIVO', '5492904015000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 15)
            """);
            
            // Provincias del LITORAL (excluye Buenos Aires que es limítrofe)
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 16, 'Depósito Formosa', 'Av. 25 de Mayo 1500, Formosa Capital', -26.1775, -58.1781, 400.0, 1300.0, 'ACTIVO', '5493704016000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 16)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 17, 'Depósito Chaco', 'Av. Las Heras 1200, Resistencia', -27.4514, -58.9867, 450.0, 1350.0, 'ACTIVO', '5493624017000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 17)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 18, 'Depósito Corrientes', 'Ruta 12 km 1050, Corrientes Capital', -27.4692, -58.8306, 450.0, 1350.0, 'ACTIVO', '5493794018000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 18)
            """);
            
            jdbcTemplate.execute("""
                INSERT INTO v2_depositos (id, nombre, direccion, lat, lon, capacidad_maxima_m3, costo_dia, estado, telefono, fecha_registro)
                SELECT 19, 'Depósito Misiones', 'Av. Uruguay 1800, Posadas', -27.3671, -55.8961, 400.0, 1400.0, 'ACTIVO', '5493764019000', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM v2_depositos WHERE id = 19)
            """);
            
            System.out.println("✅ Datos iniciales de Logística cargados: 3 transportistas, 4 camiones, 19 depósitos estratégicos en Argentina");
        } else {
            System.out.println("ℹ️ Datos de Logística ya existen, omitiendo carga inicial");
        }
    }
}
