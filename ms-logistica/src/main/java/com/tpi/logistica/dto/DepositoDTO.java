package com.tpi.logistica.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Double lat;
    private Double lon;
    private Double capacidadMaximaM3;
    private Double costoDia;
    private String estado;
}
