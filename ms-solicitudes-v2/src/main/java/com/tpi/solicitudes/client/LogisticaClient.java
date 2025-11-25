package com.tpi.solicitudes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "MS-LOGISTICA")
public interface LogisticaClient {
    
    /**
     * Calcula múltiples rutas tentativas para una solicitud.
     * Devuelve lista con estrategias: DIRECTA, UN_DEPOSITO, MULTIPLES_DEPOSITOS
     */
    @GetMapping("/api/rutas/tentativas/{solicitudId}")
    List<RutaTentativaDTO> calcularRutasTentativas(
            @PathVariable Long solicitudId,
            @RequestParam String origenDireccion,
            @RequestParam String destinoDireccion,
            @RequestParam Double pesoKg,
            @RequestParam Double volumenM3
    );
    
    /**
     * Lista todos los tramos de una solicitud
     */
    @GetMapping("/api/tramos/solicitud/{solicitudId}")
    List<TramoDTO> listarTramosPorSolicitud(@PathVariable Long solicitudId);
    
    /**
     * Lista todas las rutas de una solicitud (tentativas, asignadas, canceladas)
     */
    @GetMapping("/api/rutas/solicitud/{solicitudId}")
    List<RutaTentativaDTO> listarRutasPorSolicitud(@PathVariable Long solicitudId);
}
