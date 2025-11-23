package com.tpi.facturacion.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDTO {
    private Long id;
    private String descripcion;
    private Double cargoGestionBase;
    private Double cargoGestionPorTramo;
    private Double precioCombustibleLitro;
    private Double factorEstadiaDia;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
    private String estado;
}
