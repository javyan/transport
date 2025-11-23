package com.tpi.facturacion.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoInfoDTO {
    private Long id;
    private Double distanciaKm;
    private Double consumoCombustibleLtKm;
    private Double costoKm;
}
