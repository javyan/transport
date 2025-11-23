package com.tpi.facturacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "MS-LOGISTICA")
public interface LogisticaClient {
    
    @GetMapping("/api/tramos/solicitud/{solicitudId}")
    List<TramoDTO> listarTramosPorSolicitud(@PathVariable Long solicitudId);
}
