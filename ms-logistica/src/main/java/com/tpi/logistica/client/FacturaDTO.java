package com.tpi.logistica.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDTO {
    private Long id;
    private Long solicitudId;
    private Long tarifaId;
    private String numeroFactura;
    
    // Desglose de costos
    private Double cargoGestion;
    private Double costoTransporte;
    private Double costoCombustible;
    private Double costoEstadias;
    
    // Totales
    private Double subtotal;
    private Double impuestos;
    private Double total;
    
    private String estado;
    private LocalDateTime fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaPago;
    private String observaciones;
}
