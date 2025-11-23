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
public class TramoDTO {
    private Long id;
    private Long solicitudId;
    private Long camionId;
    private String origenTipo;
    private String origenDireccion;
    private String destinoTipo;
    private String destinoDireccion;
    private String tipoTramo;
    private Double distanciaKm;
    private Integer ordenTramo;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
