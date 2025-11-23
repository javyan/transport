package com.tpi.solicitudes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MS-FACTURACION-V2")
public interface FacturacionClient {
    
    @GetMapping("/api/facturas/solicitud/{solicitudId}")
    FacturaDTO obtenerFacturaPorSolicitud(@PathVariable Long solicitudId);
    
    @PostMapping("/api/facturas/generar")
    FacturaDTO generarFactura(@RequestParam Long solicitudId);
}
