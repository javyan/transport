package com.tpi.logistica.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarCamionRequest {
    @NotNull(message = "El ID del tramo es requerido")
    private Long tramoId;
    
    @NotNull(message = "El ID del camión es requerido")
    private Long camionId;
    
    @NotNull(message = "El ID del transportista es requerido")
    private Long transportistaId;
    
    @NotNull(message = "El peso de la carga es requerido")
    @Positive(message = "El peso debe ser positivo")
    private Double pesoKg;
    
    @NotNull(message = "El volumen de la carga es requerido")
    @Positive(message = "El volumen debe ser positivo")
    private Double volumenM3;
}
