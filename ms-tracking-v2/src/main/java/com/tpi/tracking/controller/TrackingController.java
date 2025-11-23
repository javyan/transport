package com.tpi.tracking.controller;

import com.tpi.tracking.dto.RegistrarEventoRequest;
import com.tpi.tracking.dto.TrackingEventoDTO;
import com.tpi.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Endpoints para registro y consulta de eventos de seguimiento")
public class TrackingController {
    
    private final TrackingService trackingService;
    
    @PostMapping("/registrar")
    @Operation(summary = "Registrar evento", 
               description = "Registra un nuevo evento de tracking para contenedor/solicitud/tramo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<TrackingEventoDTO> registrarEvento(
            @Valid @RequestBody RegistrarEventoRequest request) {
        return ResponseEntity.ok(trackingService.registrarEvento(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", 
               description = "Retorna un evento específico por su identificador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<TrackingEventoDTO> obtenerEvento(@PathVariable Long id) {
        return ResponseEntity.ok(trackingService.obtenerEvento(id));
    }
    
    @GetMapping("/contenedor/{contenedorId}")
    @Operation(summary = "Listar eventos por contenedor", 
               description = "Retorna todos los eventos de un contenedor ordenados por fecha descendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida correctamente")
    })
    public ResponseEntity<List<TrackingEventoDTO>> listarEventosPorContenedor(
            @PathVariable Long contenedorId) {
        return ResponseEntity.ok(trackingService.listarEventosPorContenedor(contenedorId));
    }
    
    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Listar eventos por solicitud", 
               description = "Retorna todos los eventos de una solicitud ordenados por fecha descendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida correctamente")
    })
    public ResponseEntity<List<TrackingEventoDTO>> listarEventosPorSolicitud(
            @PathVariable Long solicitudId) {
        return ResponseEntity.ok(trackingService.listarEventosPorSolicitud(solicitudId));
    }
    
    @GetMapping("/tramo/{tramoId}")
    @Operation(summary = "Listar eventos por tramo", 
               description = "Retorna todos los eventos de un tramo ordenados por fecha descendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida correctamente")
    })
    public ResponseEntity<List<TrackingEventoDTO>> listarEventosPorTramo(
            @PathVariable Long tramoId) {
        return ResponseEntity.ok(trackingService.listarEventosPorTramo(tramoId));
    }
    
    @GetMapping("/tipo/{tipoEvento}")
    @Operation(summary = "Listar eventos por tipo", 
               description = "Retorna todos los eventos de un tipo específico ordenados por fecha descendente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida correctamente")
    })
    public ResponseEntity<List<TrackingEventoDTO>> listarEventosPorTipo(
            @PathVariable String tipoEvento) {
        return ResponseEntity.ok(trackingService.listarEventosPorTipo(tipoEvento));
    }
    
    @GetMapping
    @Operation(summary = "Listar todos los eventos", 
               description = "Retorna todos los eventos de tracking registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de todos los eventos")
    public ResponseEntity<List<TrackingEventoDTO>> listarTodosEventos() {
        return ResponseEntity.ok(trackingService.listarTodosEventos());
    }
}
