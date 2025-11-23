package com.tpi.solicitudes.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RutaTentativaDTO {
    private Long id;
    private Long solicitudId;
    private String estado;
    private Integer cantidadTramos;
    private List<Long> depositosIntermedios;
    private Double distanciaTotal;
    private Double costoTotalEstimado;
    private Double costoTotalReal;
    private Double tiempoEstimadoHoras;
    private Double tiempoRealHoras;
    private String estrategia;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaCompletada;
    private String observaciones;
}
