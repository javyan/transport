package com.tpi.facturacion.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitudDTO {
    private Long id;
    private Long clienteId;
    private Long contenedorId;
    private String origenDireccion;
    private String destinoDireccion;
    private String estado;
    private Double costoEstimado;
    private Double tiempoEstimadoHoras;
    private Double costoReal;
    private Double tiempoRealHoras;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaEntrega;
}
