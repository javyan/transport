package com.tpi.logistica.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadiaRequestDTO {
    private Long contenedorId;
    private Long depositoId;
    private Double costoDia;
}
