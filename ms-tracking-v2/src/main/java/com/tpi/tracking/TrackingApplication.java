package com.tpi.tracking;

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
        title = "MS-Tracking-V2 API",
        version = "1.0.0",
        description = "Microservicio de Tracking - Registro y consulta de eventos de seguimiento"
    )
)
public class TrackingApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TrackingApplication.class, args);
    }
}
