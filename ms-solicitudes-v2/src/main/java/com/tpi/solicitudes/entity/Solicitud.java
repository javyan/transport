package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    
    @Column(name = "contenedor_id", nullable = false)
    private Long contenedorId;
    
    // Origen
    @Column(name = "origen_direccion", nullable = false, length = 500)
    private String origenDireccion;
    
    // Destino
    @Column(name = "destino_direccion", nullable = false, length = 500)
    private String destinoDireccion;
    
    // Estado y costos
    @Column(nullable = false, length = 50)
    private String estado = "BORRADOR"; // BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
    
    @Column(name = "costo_estimado")
    private Double costoEstimado;
    
    @Column(name = "tiempo_estimado_horas")
    private Double tiempoEstimadoHoras;
    
    @Column(name = "costo_real")
    private Double costoReal;
    
    @Column(name = "tiempo_real_horas")
    private Double tiempoRealHoras;
    
    // Auditoría
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
