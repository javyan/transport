package com.tpi.solicitudes.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
