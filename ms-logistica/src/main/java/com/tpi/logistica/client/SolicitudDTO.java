package com.tpi.logistica.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitudDTO {
    private Long id;
    private String origenDireccion;
    private String destinoDireccion;
    private String estado;
    private ContenedorDTO contenedor;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContenedorDTO {
        private Long id;
        private Double pesoKg;
        private Double volumenM3;
    }
}
