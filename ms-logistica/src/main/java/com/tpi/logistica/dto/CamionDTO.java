package com.tpi.logistica.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    private Long id;
    private Long transportistaId;
    private String patente;
    private String marca;
    private String modelo;
    private Integer anio;
    private Double capacidadKg;
    private Double capacidadM3;
    private Double consumoCombustibleLtKm;
    private Double costoKm;
    private String estado;
    private String ubicacionActual;
    private Double latActual;
    private Double lonActual;
}
