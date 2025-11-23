package com.tpi.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_estadias_deposito")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadiaDeposito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contenedor_id", nullable = false)
    private Long contenedorId;
    
    @Column(name = "deposito_id", nullable = false)
    private Long depositoId;
    
    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada;
    
    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;
    
    @Column(name = "dias_estadia")
    private Integer diasEstadia;
    
    @Column(name = "costo_dia", nullable = false)
    private Double costoDia;
    
    @Column(name = "costo_total")
    private Double costoTotal;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "EN_CURSO";
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
