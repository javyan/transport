package com.tpi.solicitudes.dto;

import lombok.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSolicitudRequest {
    
    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private ClienteDTO cliente;
    
    @Valid
    @NotNull(message = "Los datos del contenedor son obligatorios")
    private ContenedorSimpleDTO contenedor;
    
    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;
    
    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContenedorSimpleDTO {
        
        @NotBlank(message = "El número de identificación del contenedor es obligatorio")
        private String numeroIdentificacion;
        
        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser positivo")
        private Double pesoKg;
        
        @NotNull(message = "El volumen es obligatorio")
        @Positive(message = "El volumen debe ser positivo")
        private Double volumenM3;
        
        private String tipo;
        private String observaciones;
    }
}
