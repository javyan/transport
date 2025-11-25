package com.tpi.logistica.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadiaResponseDTO {
    private Long id;
    private Long contenedorId;
    private Long depositoId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private Integer diasEstadia;
    private Double costoDia;
    private Double costoTotal;
    private String estado;
    private String observaciones;
}
