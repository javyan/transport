package com.tpi.solicitudes.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDTO {
    
    private Long id;
    private Long clienteId;
    
    @NotBlank(message = "El número de identificación es obligatorio")
    private String numeroIdentificacion;
    
    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser positivo")
    private Double pesoKg;
    
    @NotNull(message = "El volumen es obligatorio")
    @Positive(message = "El volumen debe ser positivo")
    private Double volumenM3;
    
    private String tipo;
    private String estado;
    private String ubicacionActual;
    private Double latActual;
    private Double lonActual;
    private String observaciones;
}
