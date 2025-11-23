package com.tpi.gateway.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // === RUTAS PRINCIPALES (sin -v2, rutas limpias) ===
            
            // SOLICITUDES: /api/solicitudes/** → /api/solicitudes/** (sin cambios)
            .route("solicitudes_route", r -> r.path("/api/solicitudes/**")
                .uri("lb://MS-SOLICITUDES-V2"))
            
            // LOGISTICA: /api/logistica/** → /api/** (reescribe quitando /logistica)
            .route("logistica_route", r -> r.path("/api/logistica/**")
                .filters(f -> f.rewritePath("/api/logistica/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://MS-LOGISTICA"))
            
            // TRACKING: /api/tracking/** → /api/** (reescribe quitando /tracking)
            .route("tracking_route", r -> r.path("/api/tracking/**")
                .filters(f -> f.rewritePath("/api/tracking/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://MS-TRACKING-V2"))
            
            // FACTURACION: /api/facturacion/** → /api/** (reescribe quitando /facturacion)
            .route("facturacion_route", r -> r.path("/api/facturacion/**")
                .filters(f -> f.rewritePath("/api/facturacion/(?<segment>.*)", "/api/${segment}"))
                .uri("lb://MS-FACTURACION-V2"))
            
            .build();
    }
}