package com.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportistaDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String licencia;
    private String telefono;
    private String estado;
}
