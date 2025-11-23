package com.tpi.logistica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_rutas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private String estado = "TENTATIVA"; // TENTATIVA, ASIGNADA, COMPLETADA, CANCELADA
    
    @Column(name = "cantidad_tramos", nullable = false)
    private Integer cantidadTramos;
    
    @Column(name = "depositos_intermedios")
    private String depositosIntermedios; // IDs separados por comas: "1,3,5"
    
    @Column(name = "distancia_total_km", nullable = false)
    private Double distanciaTotal;
    
    @Column(name = "costo_total_estimado", nullable = false)
    private Double costoTotalEstimado;
    
    @Column(name = "costo_total_real")
    private Double costoTotalReal;
    
    @Column(name = "tiempo_estimado_horas", nullable = false)
    private Double tiempoEstimadoHoras;
    
    @Column(name = "tiempo_real_horas")
    private Double tiempoRealHoras;
    
    @Column(name = "estrategia", nullable = false, length = 50)
    private String estrategia; // DIRECTA, UN_DEPOSITO, MULTIPLES_DEPOSITOS
    
    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;
    
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    
    @PreUpdate
    protected void onUpdate() {
        // No hay campo fechaActualizacion, pero se puede agregar si es necesario
    }
}
