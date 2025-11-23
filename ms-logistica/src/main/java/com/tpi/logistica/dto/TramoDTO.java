package com.tpi.logistica.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoDTO {
    private Long id;
    private Long solicitudId;
    private Long camionId;
    private Long transportistaId;
    private String origenTipo;
    private Long origenId;
    private String origenDireccion;
    private String destinoTipo;
    private Long destinoId;
    private String destinoDireccion;
    private String tipoTramo;
    private Double distanciaKm;
    private Integer ordenTramo;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
