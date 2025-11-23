package com.tpi.logistica;

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
        title = "MS-Logistica API",
        version = "1.0.0",
        description = "Microservicio de Logística - Gestión de transportistas, camiones, depósitos y tramos de ruta"
    )
)
public class LogisticaApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LogisticaApplication.class, args);
    }
}
