package com.tpi.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventoDTO {
    private Long id;
    private Long contenedorId;
    private Long solicitudId;
    private Long tramoId;
    private String tipoEvento;
    private String descripcion;
    private LocalDateTime fechaHora;
    private Double lat;
    private Double lon;
    private String observaciones;
}
