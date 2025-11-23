package com.tpi.logistica.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MS-SOLICITUDES-V2")
public interface SolicitudClient {
    
    @GetMapping("/api/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable Long id);
    
    @PostMapping("/api/solicitudes/{id}/estado")
    void actualizarEstado(@PathVariable Long id, @RequestParam String estado);
    
    @PostMapping("/api/solicitudes/{id}/finalizar")
    void finalizarSolicitud(@PathVariable Long id, @RequestParam Double costoReal, @RequestParam Double tiempoRealHoras);
}
