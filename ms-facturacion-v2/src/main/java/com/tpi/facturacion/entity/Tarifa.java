package com.tpi.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_tarifas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "descripcion", nullable = false)
    private String descripcion;
    
    @Column(name = "cargo_gestion_base", nullable = false)
    private Double cargoGestionBase;
    
    @Column(name = "cargo_gestion_por_tramo", nullable = false)
    private Double cargoGestionPorTramo;
    
    @Column(name = "precio_combustible_litro", nullable = false)
    private Double precioCombustibleLitro;
    
    @Column(name = "factor_estadia_dia", nullable = false)
    private Double factorEstadiaDia;
    
    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fechaVigenciaDesde;
    
    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ACTIVA";
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
