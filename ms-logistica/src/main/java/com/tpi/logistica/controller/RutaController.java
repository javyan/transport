package com.tpi.logistica.controller;

import com.tpi.logistica.dto.RutaDTO;
import com.tpi.logistica.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Tag(name = "Rutas", description = "API para gestión de rutas tentativas y asignadas")
public class RutaController {
    
    private final RutaService rutaService;
    
    @GetMapping("/tentativas/{solicitudId}")
    @Operation(summary = "Calcular rutas tentativas", 
               description = "Genera múltiples opciones de ruta (directa, con 1 depósito, con múltiples depósitos) para que el operador seleccione")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rutas tentativas calculadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<List<RutaDTO>> calcularRutasTentativas(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId,
            @Parameter(description = "Dirección de origen completa") @RequestParam String origenDireccion,
            @Parameter(description = "Dirección de destino completa") @RequestParam String destinoDireccion,
            @Parameter(description = "Peso de la carga en kg") @RequestParam Double pesoKg,
            @Parameter(description = "Volumen de la carga en m³") @RequestParam Double volumenM3) {
        
        List<RutaDTO> rutas = rutaService.calcularRutasTentativas(
                solicitudId, origenDireccion, destinoDireccion, pesoKg, volumenM3);
        
        return ResponseEntity.ok(rutas);
    }
    
    @PostMapping("/{rutaId}/asignar")
    @Operation(summary = "Asignar ruta a solicitud", 
               description = "El operador selecciona una de las rutas tentativas y la asigna a la solicitud. Las demás rutas se cancelan.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruta asignada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada"),
        @ApiResponse(responseCode = "400", description = "Ruta no puede ser asignada (estado incorrecto)")
    })
    public ResponseEntity<RutaDTO> asignarRutaASolicitud(
            @Parameter(description = "ID de la ruta a asignar") @PathVariable Long rutaId,
            @Parameter(description = "ID de la solicitud") @RequestParam Long solicitudId) {
        
        RutaDTO ruta = rutaService.asignarRutaASolicitud(rutaId, solicitudId);
        return ResponseEntity.ok(ruta);
    }
    
    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Listar rutas de una solicitud", 
               description = "Obtiene todas las rutas (tentativas, asignada, canceladas) de una solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de rutas obtenida correctamente")
    })
    public ResponseEntity<List<RutaDTO>> listarRutasPorSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId) {
        
        List<RutaDTO> rutas = rutaService.listarRutasPorSolicitud(solicitudId);
        return ResponseEntity.ok(rutas);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas las rutas", 
               description = "Obtiene todas las rutas registradas en el sistema. Rol: OPERADOR")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de rutas obtenida correctamente")
    })
    public ResponseEntity<List<RutaDTO>> listarTodasRutas() {
        List<RutaDTO> rutas = rutaService.listarTodasRutas();
        return ResponseEntity.ok(rutas);
    }
}
