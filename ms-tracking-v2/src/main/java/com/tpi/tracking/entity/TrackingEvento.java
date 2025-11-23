package com.tpi.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_tracking_eventos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contenedor_id")
    private Long contenedorId;
    
    @Column(name = "solicitud_id")
    private Long solicitudId;
    
    @Column(name = "tramo_id")
    private Long tramoId;
    
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;
    
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
    
    @Column(name = "lat")
    private Double lat;
    
    @Column(name = "lon")
    private Double lon;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
