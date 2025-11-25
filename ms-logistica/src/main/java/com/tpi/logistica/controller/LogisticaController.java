package com.tpi.logistica.controller;

import com.tpi.logistica.dto.*;
import com.tpi.logistica.service.LogisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Logística", description = "API para gestión de rutas, camiones y tramos de transporte")
public class LogisticaController {
    
    private final LogisticaService logisticaService;
    
    @PostMapping("/rutas/calcular")
    @Operation(summary = "Calcular rutas tentativas", 
               description = "Calcula rutas posibles para una solicitud usando Google Maps API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rutas calculadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<RutaTentativaDTO> calcularRutas(
            @Parameter(description = "ID de la solicitud") @RequestParam Long solicitudId,
            @Parameter(description = "Dirección de origen completa") @RequestParam String origenDireccion,
            @Parameter(description = "Dirección de destino completa") @RequestParam String destinoDireccion) {
        
        RutaTentativaDTO ruta = logisticaService.calcularRutasTentativas(
                solicitudId, origenDireccion, destinoDireccion);
        
        return ResponseEntity.ok(ruta);
    }
    
    @PostMapping("/tramos/crear")
    @Operation(summary = "Crear tramos para una solicitud",
               description = "Crea los tramos en la base de datos para comenzar la planificación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tramos creados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<List<TramoDTO>> crearTramos(
            @Parameter(description = "ID de la solicitud") @RequestParam Long solicitudId,
            @Parameter(description = "Dirección de origen completa") @RequestParam String origenDireccion,
            @Parameter(description = "Dirección de destino completa") @RequestParam String destinoDireccion) {
        
        List<TramoDTO> tramos = logisticaService.crearTramos(
                solicitudId, origenDireccion, destinoDireccion);
        
        return ResponseEntity.status(201).body(tramos);
    }
    
    @GetMapping("/camiones/disponibles")
    @Operation(summary = "Listar camiones disponibles",
               description = "Obtiene camiones con capacidad suficiente para la carga. Rol: OPERADOR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de camiones disponibles"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<List<CamionDTO>> listarCamionesDisponibles(
            @Parameter(description = "Peso requerido en kg") @RequestParam Double pesoKg,
            @Parameter(description = "Volumen requerido en m³") @RequestParam Double volumenM3) {
        
        List<CamionDTO> camiones = logisticaService.listarCamionesDisponibles(pesoKg, volumenM3);
        return ResponseEntity.ok(camiones);
    }
    
    @PostMapping("/tramos/{tramoId}/asignar")
    @Operation(summary = "Asignar camión y transportista a un tramo",
               description = "Asigna un camión y transportista específicos a un tramo de transporte. Obtiene peso y volumen del contenedor automáticamente. Valida capacidad y disponibilidad. Rol: OPERADOR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión y transportista asignados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tramo, camión o transportista no encontrado"),
        @ApiResponse(responseCode = "400", description = "Camión/transportista no disponible o sin capacidad suficiente")
    })
    public ResponseEntity<TramoDTO> asignarCamion(
            @Parameter(description = "ID del tramo") @PathVariable Long tramoId,
            @Parameter(description = "ID del camión") @RequestParam Long camionId,
            @Parameter(description = "ID del transportista") @RequestParam Long transportistaId) {
        TramoDTO tramo = logisticaService.asignarCamion(tramoId, camionId, transportistaId);
        return ResponseEntity.ok(tramo);
    }
    
    @PatchMapping("/tramos/{id}/iniciar")
    @Operation(summary = "Iniciar un tramo",
               description = "El transportista inicia el tramo (comienza el viaje). Cambia estado de ASIGNADO a INICIADO. Rol: TRANSPORTISTA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tramo iniciado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado"),
        @ApiResponse(responseCode = "400", description = "Tramo sin camión asignado o en estado incorrecto")
    })
    public ResponseEntity<TramoDTO> iniciarTramo(
            @Parameter(description = "ID del tramo") @PathVariable Long id) {
        
        TramoDTO tramo = logisticaService.iniciarTramo(id);
        return ResponseEntity.ok(tramo);
    }
    
    @PatchMapping("/tramos/{id}/finalizar")
    @Operation(summary = "Finalizar un tramo",
               description = "El transportista finaliza el tramo (completa el viaje). Cambia estado de INICIADO a FINALIZADO. Rol: TRANSPORTISTA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tramo finalizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado")
    })
    public ResponseEntity<TramoDTO> finalizarTramo(
            @Parameter(description = "ID del tramo") @PathVariable Long id) {
        
        TramoDTO tramo = logisticaService.finalizarTramo(id);
        return ResponseEntity.ok(tramo);
    }
    
    @GetMapping("/tramos/solicitud/{solicitudId}")
    @Operation(summary = "Listar tramos de una solicitud",
               description = "Obtiene todos los tramos de una solicitud ordenados. Rol: OPERADOR, CLIENTE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tramos")
    })
    public ResponseEntity<List<TramoDTO>> listarTramosPorSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long solicitudId) {
        
        List<TramoDTO> tramos = logisticaService.listarTramosPorSolicitud(solicitudId);
        return ResponseEntity.ok(tramos);
    }
    
    @GetMapping("/tramos/transportista/{transportistaId}")
    @Operation(summary = "Listar tramos de un transportista",
               description = "Obtiene todos los tramos asignados a un transportista. Rol: TRANSPORTISTA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tramos del transportista")
    })
    public ResponseEntity<List<TramoDTO>> listarTramosPorTransportista(
            @Parameter(description = "ID del transportista") @PathVariable Long transportistaId) {
        
        List<TramoDTO> tramos = logisticaService.listarTramosPorTransportista(transportistaId);
        return ResponseEntity.ok(tramos);
    }
    
    @GetMapping("/tramos")
    @Operation(summary = "Listar todos los tramos",
               description = "Obtiene todos los tramos registrados en el sistema. Rol: OPERADOR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos los tramos")
    })
    public ResponseEntity<List<TramoDTO>> listarTodosTramos() {
        List<TramoDTO> tramos = logisticaService.listarTodosTramos();
        return ResponseEntity.ok(tramos);
    }
    
    @GetMapping("/camiones")
    @Operation(summary = "Listar todos los camiones",
               description = "Obtiene todos los camiones registrados en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos los camiones")
    })
    public ResponseEntity<List<CamionDTO>> listarTodosCamiones() {
        List<CamionDTO> camiones = logisticaService.listarTodosCamiones();
        return ResponseEntity.ok(camiones);
    }
    
    @GetMapping("/transportistas")
    @Operation(summary = "Listar todos los transportistas",
               description = "Obtiene todos los transportistas registrados en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos los transportistas")
    })
    public ResponseEntity<List<TransportistaDTO>> listarTodosTransportistas() {
        List<TransportistaDTO> transportistas = logisticaService.listarTodosTransportistas();
        return ResponseEntity.ok(transportistas);
    }
    
    @GetMapping("/depositos")
    @Operation(summary = "Listar todos los depósitos",
               description = "Obtiene todos los depósitos registrados en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos los depósitos")
    })
    public ResponseEntity<List<DepositoDTO>> listarTodosDepositos() {
        List<DepositoDTO> depositos = logisticaService.listarTodosDepositos();
        return ResponseEntity.ok(depositos);
    }
}
