package com.tpi.facturacion.controller;

import com.tpi.facturacion.dto.*;
import com.tpi.facturacion.service.FacturacionService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Facturación", description = "Endpoints para gestionar tarifas, facturas y estadías")
public class FacturacionController {
    
    private final FacturacionService facturacionService;
    
    @GetMapping("/tarifas/vigente")
    @Operation(summary = "Obtener tarifa vigente", 
               description = "Retorna la tarifa actualmente vigente para cálculo de facturas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarifa vigente obtenida correctamente"),
        @ApiResponse(responseCode = "404", description = "No hay tarifa vigente configurada")
    })
    public ResponseEntity<TarifaDTO> obtenerTarifaVigente() {
        return ResponseEntity.ok(facturacionService.obtenerTarifaVigente());
    }
    
    @PostMapping("/facturas/calcular")
    @Operation(summary = "Calcular factura", 
               description = "Calcula y genera una factura para una solicitud basándose en los tramos realizados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura calculada y generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Ya existe factura para esta solicitud"),
        @ApiResponse(responseCode = "404", description = "No hay tarifa vigente o solicitud no encontrada")
    })
    public ResponseEntity<FacturaDTO> calcularFactura(
            @RequestParam Long solicitudId,
            @RequestBody List<TramoInfoDTO> tramos) {
        return ResponseEntity.ok(facturacionService.calcularFactura(solicitudId, tramos));
    }
    
    @GetMapping("/facturas/solicitud/{solicitudId}")
    @Operation(summary = "Obtener factura por solicitud", 
               description = "Retorna la factura asociada a una solicitud específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura encontrada"),
        @ApiResponse(responseCode = "404", description = "No existe factura para esta solicitud")
    })
    public ResponseEntity<FacturaDTO> obtenerFacturaPorSolicitud(@PathVariable Long solicitudId) {
        return ResponseEntity.ok(facturacionService.obtenerFacturaPorSolicitud(solicitudId));
    }
    
    @GetMapping("/facturas")
    @Operation(summary = "Listar todas las facturas", 
               description = "Retorna todas las facturas registradas en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de facturas obtenida correctamente")
    })
    public ResponseEntity<List<FacturaDTO>> listarFacturas() {
        return ResponseEntity.ok(facturacionService.listarTodasFacturas());
    }
    
    @PostMapping("/estadias/registrar-entrada")
    @Operation(summary = "Registrar entrada a depósito", 
               description = "Registra la entrada de un contenedor a un depósito, iniciando el cómputo de estadía")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entrada registrada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<EstadiaDepositoDTO> registrarEntradaDeposito(
            @Valid @RequestBody RegistrarEstadiaRequest request) {
        return ResponseEntity.ok(facturacionService.registrarEntradaDeposito(request));
    }
    
    @PostMapping("/estadias/{id}/registrar-salida")
    @Operation(summary = "Registrar salida de depósito", 
               description = "Registra la salida de un contenedor del depósito y calcula el costo total de estadía")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Salida registrada y costo calculado correctamente"),
        @ApiResponse(responseCode = "400", description = "La estadía ya fue finalizada"),
        @ApiResponse(responseCode = "404", description = "Estadía no encontrada")
    })
    public ResponseEntity<EstadiaDepositoDTO> registrarSalidaDeposito(@PathVariable Long id) {
        return ResponseEntity.ok(facturacionService.registrarSalidaDeposito(id));
    }
    
    @GetMapping("/estadias/contenedor/{contenedorId}")
    @Operation(summary = "Listar estadías por contenedor", 
               description = "Retorna todas las estadías (en curso y finalizadas) de un contenedor específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de estadías obtenida correctamente")
    })
    public ResponseEntity<List<EstadiaDepositoDTO>> listarEstadiasPorContenedor(
            @PathVariable Long contenedorId) {
        return ResponseEntity.ok(facturacionService.listarEstadiasPorContenedor(contenedorId));
    }
    
    @PostMapping("/facturas/generar")
    @Operation(summary = "Generar factura automáticamente", 
               description = "Genera una factura consultando automáticamente los tramos de la solicitud desde ms-logistica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Ya existe factura para esta solicitud"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada o sin tramos")
    })
    public ResponseEntity<FacturaDTO> generarFactura(@RequestParam Long solicitudId) {
        return ResponseEntity.ok(facturacionService.generarFactura(solicitudId));
    }
    
    @GetMapping("/tarifas")
    @Operation(summary = "Listar todas las tarifas", 
               description = "Retorna todas las tarifas registradas en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de tarifas obtenida correctamente")
    public ResponseEntity<List<TarifaDTO>> listarTodasTarifas() {
        return ResponseEntity.ok(facturacionService.listarTodasTarifas());
    }
    
    @GetMapping("/estadias")
    @Operation(summary = "Listar todas las estadías", 
               description = "Retorna todas las estadías registradas en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de estadías obtenida correctamente")
    public ResponseEntity<List<EstadiaDepositoDTO>> listarTodasEstadias() {
        return ResponseEntity.ok(facturacionService.listarTodasEstadias());
    }
}
