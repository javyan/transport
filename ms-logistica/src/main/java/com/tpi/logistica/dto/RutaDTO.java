package com.tpi.logistica.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RutaDTO {
    private Long id;
    private Long solicitudId;
    private String estado; // TENTATIVA, ASIGNADA, COMPLETADA, CANCELADA
    private Integer cantidadTramos;
    private List<Long> depositosIntermedios; // Lista de IDs de depósitos
    private Double distanciaTotal;
    private Double costoTotalEstimado;
    private Double costoTotalReal;
    private Double tiempoEstimadoHoras;
    private Double tiempoRealHoras;
    private String estrategia; // DIRECTA, UN_DEPOSITO, MULTIPLES_DEPOSITOS
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaCompletada;
    private String observaciones;
    
    // Lista de tramos que componen esta ruta (opcional, para vista detallada)
    private List<TramoDTO> tramos;
}
