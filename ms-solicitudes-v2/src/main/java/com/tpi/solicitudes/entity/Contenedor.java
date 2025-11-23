package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_contenedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    
    @Column(name = "numero_identificacion", nullable = false, unique = true, length = 100)
    private String numeroIdentificacion;
    
    @Column(name = "peso_kg", nullable = false)
    private Double pesoKg;
    
    @Column(name = "volumen_m3", nullable = false)
    private Double volumenM3;
    
    @Column(nullable = false, length = 100)
    private String tipo = "ESTANDAR";
    
    @Column(nullable = false, length = 50)
    private String estado = "CREADO"; // CREADO, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
    
    @Column(name = "ubicacion_actual", length = 500)
    private String ubicacionActual;
    
    @Column(name = "lat_actual")
    private Double latActual;
    
    @Column(name = "lon_actual")
    private Double lonActual;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
