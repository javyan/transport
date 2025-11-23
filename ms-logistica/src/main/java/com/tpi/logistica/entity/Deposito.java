package com.tpi.logistica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_depositos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "direccion", nullable = false, length = 500)
    private String direccion;
    
    @Column(name = "lat", nullable = false)
    private Double lat;
    
    @Column(name = "lon", nullable = false)
    private Double lon;
    
    @Column(name = "capacidad_maxima_m3", nullable = false)
    private Double capacidadMaximaM3;
    
    @Column(name = "costo_dia", nullable = false)
    private Double costoDia;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ACTIVO";
    
    @Column(name = "telefono", length = 50)
    private String telefono;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
