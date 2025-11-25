package com.tpi.solicitudes.dto;

import com.tpi.solicitudes.client.RutaTentativaDTO;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDTO {
    
    private Long id;
    private Long clienteId;
    private Long contenedorId;
    
    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;
    
    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;
    
    private String estado;
    private Double costoEstimado;
    private Double tiempoEstimadoHoras;
    private Double costoReal;
    private Double tiempoRealHoras;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaEntrega;
    
    // Datos del contenedor (para validaciones de capacidad)
    private ContenedorDTO contenedor;
    
    // Rutas tentativas calculadas (se incluyen al crear la solicitud)
    private List<RutaTentativaDTO> rutasTentativas;
}
