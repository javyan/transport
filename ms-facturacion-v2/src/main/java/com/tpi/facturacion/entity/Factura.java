package com.tpi.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_facturas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    
    @Column(name = "tarifa_id", nullable = false)
    private Long tarifaId;
    
    @Column(name = "numero_factura", nullable = false, unique = true, length = 50)
    private String numeroFactura;
    
    // Desglose de costos
    @Column(name = "cargo_gestion", nullable = false)
    private Double cargoGestion;
    
    @Column(name = "costo_transporte", nullable = false)
    private Double costoTransporte;
    
    @Column(name = "costo_combustible", nullable = false)
    private Double costoCombustible;
    
    @Column(name = "costo_estadias", nullable = false)
    private Double costoEstadias = 0.0;
    
    // Totales
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;
    
    @Column(name = "impuestos", nullable = false)
    private Double impuestos = 0.0;
    
    @Column(name = "total", nullable = false)
    private Double total;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "PENDIENTE";
    
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();
    
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
