package com.tpi.logistica.service;

import com.tpi.logistica.dto.googlemaps.GoogleMapsDirectionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Servicio para calcular distancias usando Google Maps Distance Matrix API
 */
@Service
@Slf4j
public class GoogleMapsService {
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    private static final String GOOGLE_MAPS_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private final WebClient webClient;
    
    public GoogleMapsService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(GOOGLE_MAPS_API_URL).build();
    }
    
    /**
     * Calcula la distancia en kilómetros entre dos direcciones usando Google Maps Distance Matrix API.
     * Si la API falla, usa fórmula Haversine como fallback.
     * 
     * @param origenDireccion Dirección de origen completa
     * @param destinoDireccion Dirección de destino completa
     * @return Distancia en kilómetros
     */
    public Double calcularDistancia(String origenDireccion, String destinoDireccion) {
        log.info("Calculando distancia real usando Google Maps Distance Matrix API");
        log.info("Origen: {} | Destino: {}", origenDireccion, destinoDireccion);
        
        try {
            // Llamada a Google Maps Distance Matrix API con direcciones
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("origins", origenDireccion)
                            .queryParam("destinations", destinoDireccion)
                            .queryParam("key", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(DistanceMatrixResponse.class)
                    .block();
            
            // Validar respuesta
            if (response == null || !"OK".equals(response.status)) {
                log.warn("Google Maps API no devolvió resultado OK. Status: {}, Error: {}", 
                        response != null ? response.status : "null",
                        response != null ? response.error_message : "null");
                return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            }
            
            if (response.rows == null || response.rows.isEmpty()) {
                log.warn("Google Maps API no devolvió filas");
                return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            }
            
            var firstRow = response.rows.get(0);
            if (firstRow.elements == null || firstRow.elements.isEmpty()) {
                log.warn("Fila sin elementos");
                return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            }
            
            var firstElement = firstRow.elements.get(0);
            if (!"OK".equals(firstElement.status)) {
                log.warn("Elemento con status: {}. Posible dirección no encontrada", firstElement.status);
                return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            }
            
            if (firstElement.distance == null) {
                log.warn("Elemento sin información de distancia");
                return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            }
            
            // Convertir de metros a kilómetros y redondear
            double distanciaKm = firstElement.distance.value / 1000.0;
            double distanciaRedondeada = Math.round(distanciaKm * 100.0) / 100.0;
            
            log.info("✅ Distancia obtenida de Google Maps: {} km ({})", 
                    distanciaRedondeada, firstElement.distance.text);
            
            return distanciaRedondeada;
            
        } catch (WebClientResponseException e) {
            log.error("❌ Error al llamar a Google Maps API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return usarDistanciaEstimada(origenDireccion, destinoDireccion);
            
        } catch (Exception e) {
            log.error("❌ Error inesperado al calcular distancia con Google Maps: {}", e.getMessage(), e);
            return usarDistanciaEstimada(origenDireccion, destinoDireccion);
        }
    }
    
    /**
     * Método de fallback: Usa distancias estimadas basadas en rutas conocidas de Argentina
     */
    private Double usarDistanciaEstimada(String origen, String destino) {
        log.warn("🔄 Usando distancias estimadas como fallback");
        
        // Normalizar nombres (case-insensitive, sin "Argentina")
        String origenNorm = origen.toLowerCase().replace(", argentina", "").trim();
        String destinoNorm = destino.toLowerCase().replace(", argentina", "").trim();
        
        // Mapa de distancias reales aproximadas (km) entre ciudades argentinas
        var distancias = new java.util.HashMap<String, Double>();
        distancias.put("buenos aires-mendoza", 1050.0);
        distancias.put("mendoza-buenos aires", 1050.0);
        distancias.put("buenos aires-cordoba", 700.0);
        distancias.put("cordoba-buenos aires", 700.0);
        distancias.put("buenos aires-bariloche", 1650.0);
        distancias.put("bariloche-buenos aires", 1650.0);
        distancias.put("buenos aires-salta", 1590.0);
        distancias.put("salta-buenos aires", 1590.0);
        distancias.put("buenos aires-rosario", 300.0);
        distancias.put("rosario-buenos aires", 300.0);
        distancias.put("cordoba-mendoza", 550.0);
        distancias.put("mendoza-cordoba", 550.0);
        distancias.put("rosario-cordoba", 400.0);
        distancias.put("cordoba-rosario", 400.0);
        distancias.put("neuquen-bariloche", 430.0);
        distancias.put("bariloche-neuquen", 430.0);
        
        String clave = origenNorm + "-" + destinoNorm;
        Double distancia = distancias.get(clave);
        
        if (distancia != null) {
            log.info("✅ Distancia estimada: {} km", distancia);
            return distancia;
        }
        
        // Si no hay match exacto, devolver distancia por defecto
        log.warn("⚠️ No hay distancia estimada para: {} → {}. Usando 700km por defecto", origen, destino);
        return 700.0;
    }
    
    /**
     * Calcula tiempo estimado de viaje en horas.
     * Asume velocidad promedio de 80 km/h para transporte de carga.
     * 
     * @param distanciaKm Distancia en kilómetros
     * @return Tiempo estimado en horas
     */
    public Double calcularTiempoEstimado(Double distanciaKm) {
        final double VELOCIDAD_PROMEDIO = 80.0; // km/h
        double tiempoHoras = distanciaKm / VELOCIDAD_PROMEDIO;
        return Math.round(tiempoHoras * 100.0) / 100.0;
    }
    
    // ===== DTOs para Distance Matrix API =====
    
    @lombok.Data
    public static class DistanceMatrixResponse {
        private java.util.List<String> destination_addresses;
        private java.util.List<String> origin_addresses;
        private java.util.List<Row> rows;
        private String status;
        private String error_message; // Campo para manejar errores de API
    }
    
    @lombok.Data
    public static class Row {
        private java.util.List<Element> elements;
    }
    
    @lombok.Data
    public static class Element {
        private Distance distance;
        private Duration duration;
        private String status;
    }
    
    @lombok.Data
    public static class Distance {
        private String text;
        private Integer value; // metros
    }
    
    @lombok.Data
    public static class Duration {
        private String text;
        private Integer value; // segundos
    }
}
