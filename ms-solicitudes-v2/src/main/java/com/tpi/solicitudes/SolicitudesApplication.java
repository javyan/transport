package com.tpi.solicitudes;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@OpenAPIDefinition(
    info = @Info(
        title = "API de Solicitudes, Clientes y Contenedores",
        version = "2.0",
        description = "Microservicio para gestión de clientes, solicitudes de transporte y contenedores",
        contact = @Contact(name = "TPI Backend", email = "backend@tpi.com")
    )
)
public class SolicitudesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolicitudesApplication.class, args);
    }
}
