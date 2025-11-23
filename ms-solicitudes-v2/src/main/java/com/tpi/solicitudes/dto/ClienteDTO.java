package com.tpi.solicitudes.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String nombreCompleto;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "El teléfono debe tener entre 10 y 15 dígitos")
    private String telefono;
    
    @Size(max = 500, message = "La dirección no puede superar 500 caracteres")
    private String direccion;
}
