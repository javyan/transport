package com.tpi.logistica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_camiones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transportista_id")
    private Long transportistaId;
    
    @Column(name = "patente", nullable = false, unique = true, length = 20)
    private String patente;
    
    @Column(name = "marca", nullable = false, length = 100)
    private String marca;
    
    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;
    
    @Column(name = "anio", nullable = false)
    private Integer anio;
    
    @Column(name = "capacidad_kg", nullable = false)
    private Double capacidadKg;
    
    @Column(name = "capacidad_m3", nullable = false)
    private Double capacidadM3;
    
    @Column(name = "consumo_combustible_lt_km", nullable = false)
    private Double consumoCombustibleLtKm;
    
    @Column(name = "costo_km", nullable = false)
    private Double costoKm;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "DISPONIBLE";
    
    @Column(name = "ubicacion_actual", length = 500)
    private String ubicacionActual;
    
    @Column(name = "lat_actual")
    private Double latActual;
    
    @Column(name = "lon_actual")
    private Double lonActual;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
