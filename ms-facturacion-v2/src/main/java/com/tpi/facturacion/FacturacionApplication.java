package com.tpi.facturacion;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@OpenAPIDefinition(
    info = @Info(
        title = "MS-Facturacion-V2 API",
        version = "1.0.0",
        description = "Microservicio de Facturación - Gestión de tarifas, facturas y estadías en depósito"
    )
)
public class FacturacionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FacturacionApplication.class, args);
    }
}
