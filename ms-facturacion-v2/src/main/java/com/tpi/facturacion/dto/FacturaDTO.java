package com.tpi.facturacion.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDTO {
    private Long id;
    private Long solicitudId;
    private String numeroFactura;
    private Double cargoGestion;
    private Double costoTransporte;
    private Double costoCombustible;
    private Double costoEstadias;
    private Double subtotal;
    private Double impuestos;
    private Double total;
    private String estado;
    private LocalDateTime fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaPago;
}
