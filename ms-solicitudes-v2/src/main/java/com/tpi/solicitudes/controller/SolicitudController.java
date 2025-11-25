package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.*;
import com.tpi.solicitudes.client.FacturaDTO;
import com.tpi.solicitudes.client.RutaTentativaDTO;
import com.tpi.solicitudes.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte")
public class SolicitudController {
    
    private final SolicitudService solicitudService;
    
    @PostMapping
    @Operation(
        summary = "Crear nueva solicitud de transporte",
        description = "Registra una solicitud de transporte. Crea el contenedor y registra al cliente si no existe. Rol: CLIENTE"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "El contenedor ya existe")
    })
    public ResponseEntity<SolicitudDTO> crearSolicitud(
            @Valid @RequestBody CrearSolicitudRequest request) {
        SolicitudDTO solicitud = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener solicitud por ID",
        description = "Consulta los detalles de una solicitud específica. Rol: CLIENTE, OPERADOR"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudDTO> obtenerSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        SolicitudDTO solicitud = solicitudService.obtenerSolicitud(id);
        return ResponseEntity.ok(solicitud);
    }
    
    @PostMapping("/{id}/confirmar")
    @Operation(
        summary = "Confirmar solicitud",
        description = "Cambia estado de BORRADOR a PLANIFICADA. Requiere que ya tenga una ruta asignada. Rol: CLIENTE, OPERADOR"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solicitud confirmada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estado inválido o falta ruta asignada"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudDTO> confirmarSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        SolicitudDTO solicitud = solicitudService.confirmarSolicitud(id);
        return ResponseEntity.ok(solicitud);
    }
    
    @PostMapping("/{id}/estado")
    @Operation(
        summary = "Actualizar estado de solicitud",
        description = "Actualiza el estado de una solicitud (uso interno entre microservicios)"
    )
    @ApiResponse(responseCode = "204", description = "Estado actualizado exitosamente")
    public ResponseEntity<Void> actualizarEstadoPost(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam String estado) {
        solicitudService.actualizarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/finalizar")
    @Operation(
        summary = "Finalizar solicitud con costo y tiempo real",
        description = "Finaliza una solicitud estableciendo el costo real y tiempo real (uso interno entre microservicios)"
    )
    @ApiResponse(responseCode = "204", description = "Solicitud finalizada exitosamente")
    public ResponseEntity<Void> finalizarSolicitudPost(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id,
            @Parameter(description = "Costo real") @RequestParam Double costoReal,
            @Parameter(description = "Tiempo real en horas") @RequestParam Double tiempoRealHoras) {
        solicitudService.finalizarSolicitud(id, costoReal, tiempoRealHoras);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/pendientes")
    @Operation(
        summary = "Listar solicitudes pendientes",
        description = "Obtiene todas las solicitudes en estado PROGRAMADA o EN_TRANSITO. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes pendientes")
    public ResponseEntity<List<SolicitudDTO>> listarPendientes() {
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesPendientes();
        return ResponseEntity.ok(solicitudes);
    }
    
    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Listar solicitudes por estado",
        description = "Filtra solicitudes por estado: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes filtradas")
    public ResponseEntity<List<SolicitudDTO>> listarPorEstado(
            @Parameter(description = "Estado de la solicitud") @PathVariable String estado) {
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todas las solicitudes",
        description = "Obtiene todas las solicitudes del sistema. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de todas las solicitudes")
    public ResponseEntity<List<SolicitudDTO>> listarTodasSolicitudes() {
        List<SolicitudDTO> solicitudes = solicitudService.listarTodasSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Listar solicitudes de un cliente",
        description = "Obtiene todas las solicitudes de un cliente específico. Rol: CLIENTE, OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes del cliente")
    public ResponseEntity<List<SolicitudDTO>> listarPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesDeCliente(clienteId);
        return ResponseEntity.ok(solicitudes);
    }
    
    @PatchMapping("/{id}/estado")
    @Operation(
        summary = "Actualizar estado de solicitud",
        description = "Cambia el estado de la solicitud. Rol: OPERADOR"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<SolicitudDTO> actualizarEstadoPatch(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam String estado) {
        solicitudService.actualizarEstado(id, estado);
        SolicitudDTO solicitud = solicitudService.obtenerSolicitud(id);
        return ResponseEntity.ok(solicitud);
    }
    
    @PatchMapping("/{id}/costo-estimado")
    @Operation(
        summary = "Actualizar costo y tiempo estimados",
        description = "Registra el costo y tiempo estimados calculados. Cambia estado a PROGRAMADA. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Estimaciones actualizadas")
    public ResponseEntity<SolicitudDTO> actualizarCostoEstimado(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id,
            @Parameter(description = "Costo estimado en pesos") @RequestParam Double costoEstimado,
            @Parameter(description = "Tiempo estimado en horas") @RequestParam Double tiempoEstimadoHoras) {
        SolicitudDTO solicitud = solicitudService.actualizarCostoEstimado(id, costoEstimado, tiempoEstimadoHoras);
        return ResponseEntity.ok(solicitud);
    }
    
    @PatchMapping("/{id}/finalizar")
    @Operation(
        summary = "Finalizar solicitud",
        description = "Registra el costo y tiempo reales. Marca la solicitud y contenedor como ENTREGADA. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Solicitud finalizada")
    public ResponseEntity<SolicitudDTO> finalizarSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id,
            @Parameter(description = "Costo real total") @RequestParam Double costoReal,
            @Parameter(description = "Tiempo real en horas") @RequestParam Double tiempoRealHoras) {
        SolicitudDTO solicitud = solicitudService.finalizarSolicitud(id, costoReal, tiempoRealHoras);
        return ResponseEntity.ok(solicitud);
    }
    
    @GetMapping("/{id}/factura")
    @Operation(
        summary = "Obtener factura de la solicitud",
        description = "Consulta la factura generada para esta solicitud en ms-facturacion-v2. Rol: CLIENTE/OPERADOR"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura obtenida correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    public ResponseEntity<FacturaDTO> obtenerFacturaSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        FacturaDTO factura = solicitudService.obtenerFacturaSolicitud(id);
        return ResponseEntity.ok(factura);
    }
    
    @GetMapping("/clientes")
    @Operation(
        summary = "Listar todos los clientes",
        description = "Obtiene todos los clientes registrados. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    public ResponseEntity<List<ClienteDTO>> listarTodosClientes() {
        List<ClienteDTO> clientes = solicitudService.listarTodosClientes();
        return ResponseEntity.ok(clientes);
    }
    
    @GetMapping("/contenedores")
    @Operation(
        summary = "Listar todos los contenedores",
        description = "Obtiene todos los contenedores registrados. Rol: OPERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Lista de contenedores")
    public ResponseEntity<List<ContenedorDTO>> listarTodosContenedores() {
        List<ContenedorDTO> contenedores = solicitudService.listarTodosContenedores();
        return ResponseEntity.ok(contenedores);
    }
    
    @GetMapping("/{id}/rutas")
    @Operation(
        summary = "Obtener todas las rutas tentativas de una solicitud",
        description = "Devuelve todas las rutas calculadas (DIRECTA, UN_DEPOSITO, MULTIPLES_DEPOSITOS) para que el operador elija. Rol: OPERADOR"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rutas encontradas"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<List<RutaTentativaDTO>> obtenerRutasSolicitud(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        List<RutaTentativaDTO> rutas = solicitudService.obtenerRutasSolicitud(id);
        return ResponseEntity.ok(rutas);
    }
}
