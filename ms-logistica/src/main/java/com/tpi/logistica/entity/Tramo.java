package com.tpi.logistica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_tramos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    
    @Column(name = "ruta_id")
    private Long rutaId;
    
    @Column(name = "camion_id")
    private Long camionId;
    
    @Column(name = "transportista_id")
    private Long transportistaId;
    
    @Column(name = "origen_tipo", nullable = false, length = 50)
    private String origenTipo;
    
    @Column(name = "origen_id")
    private Long origenId;
    
    @Column(name = "origen_direccion", nullable = false, length = 500)
    private String origenDireccion;
    
    @Column(name = "destino_tipo", nullable = false, length = 50)
    private String destinoTipo;
    
    @Column(name = "destino_id")
    private Long destinoId;
    
    @Column(name = "destino_direccion", nullable = false, length = 500)
    private String destinoDireccion;
    
    @Column(name = "tipo_tramo", nullable = false, length = 50)
    private String tipoTramo;
    
    @Column(name = "distancia_km", nullable = false)
    private Double distanciaKm;
    
    @Column(name = "orden_tramo", nullable = false)
    private Integer ordenTramo;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ESTIMADO"; // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
