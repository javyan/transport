package com.tpi.logistica.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaTentativaDTO {
    private Long solicitudId;
    private List<TramoDTO> tramos;
    private Double distanciaTotal;
    private Double costoEstimadoTransporte;
    private Double tiempoEstimadoHoras;
    private String estrategia; // "DIRECTA" o "CON_DEPOSITOS"
}
