package com.tpi.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarEventoRequest {
    
    private Long contenedorId;
    
    private Long solicitudId;
    
    private Long tramoId;
    
    @NotBlank(message = "El tipo de evento es obligatorio")
    private String tipoEvento;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    private LocalDateTime fechaHora;
    
    private Double lat;
    
    private Double lon;
    
    private String observaciones;
}
