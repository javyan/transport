package com.tpi.facturacion.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarEstadiaRequest {
    @NotNull(message = "El ID del contenedor es requerido")
    private Long contenedorId;
    
    @NotNull(message = "El ID del depósito es requerido")
    private Long depositoId;
    
    @NotNull(message = "El costo por día es requerido")
    @Positive(message = "El costo por día debe ser positivo")
    private Double costoDia;
    
    private String observaciones;
}
