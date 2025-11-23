package com.tpi.facturacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-SOLICITUDES-V2")
public interface SolicitudClient {
    
    @GetMapping("/api/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable Long id);
}
