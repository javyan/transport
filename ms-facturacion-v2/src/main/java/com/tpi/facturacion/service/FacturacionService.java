package com.tpi.facturacion.service;

import com.tpi.facturacion.client.LogisticaClient;
import com.tpi.facturacion.client.SolicitudClient;
import com.tpi.facturacion.client.SolicitudDTO;
import com.tpi.facturacion.client.TramoDTO;
import com.tpi.facturacion.dto.*;
import com.tpi.facturacion.entity.*;
import com.tpi.facturacion.exception.FacturaNotFoundException;
import com.tpi.facturacion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FacturacionService {
    
    private final TarifaRepository tarifaRepository;
    private final FacturaRepository facturaRepository;
    private final EstadiaDepositoRepository estadiaDepositoRepository;
    private final LogisticaClient logisticaClient;
    private final SolicitudClient solicitudClient;
    
    /**
     * Obtiene la tarifa vigente actual
     */
    @Transactional(readOnly = true)
    public TarifaDTO obtenerTarifaVigente() {
        Tarifa tarifa = tarifaRepository.findTarifaVigente(LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No hay tarifa vigente configurada"));
        return convertirATarifaDTO(tarifa);
    }
    
    /**
     * Calcula y genera una factura para una solicitud
     * Fórmula: Cargos Gestión + Costo Transporte + Costo Combustible + Estadías
     */
    public FacturaDTO calcularFactura(Long solicitudId, List<TramoInfoDTO> tramos) {
        log.info("Calculando factura para solicitud ID: {}", solicitudId);
        
        // Verificar que no exista factura previa
        if (facturaRepository.existsBySolicitudId(solicitudId)) {
            throw new IllegalStateException("Ya existe una factura para la solicitud ID: " + solicitudId);
        }
        
        // Obtener tarifa vigente
        Tarifa tarifa = tarifaRepository.findTarifaVigente(LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No hay tarifa vigente"));
        
        // 1. Cargo de Gestión = base + (cantidad_tramos × cargo_por_tramo)
        Double cargoGestion = tarifa.getCargoGestionBase() + 
                             (tramos.size() * tarifa.getCargoGestionPorTramo());
        log.info("Cargo de gestión: ${} (base ${} + {} tramos × ${})", 
                cargoGestion, tarifa.getCargoGestionBase(), tramos.size(), tarifa.getCargoGestionPorTramo());
        
        // 2. Costo de Transporte = Σ(distancia_tramo × costo_km_camion)
        Double costoTransporte = tramos.stream()
                .mapToDouble(t -> t.getDistanciaKm() * t.getCostoKm())
                .sum();
        log.info("Costo de transporte: ${} ({} tramos)", costoTransporte, tramos.size());
        
        // 3. Costo de Combustible = Σ(distancia_tramo × consumo_camion × precio_litro)
        Double costoCombustible = tramos.stream()
                .mapToDouble(t -> t.getDistanciaKm() * t.getConsumoCombustibleLtKm() * tarifa.getPrecioCombustibleLitro())
                .sum();
        log.info("Costo de combustible: ${} (precio/litro: ${})", costoCombustible, tarifa.getPrecioCombustibleLitro());
        
        // 4. Costo de Estadías (si existen estadías finalizadas para esta solicitud)
        // Por ahora 0, se calculará cuando se registren estadías
        Double costoEstadias = 0.0;
        
        // Subtotal
        Double subtotal = cargoGestion + costoTransporte + costoCombustible + costoEstadias;
        
        // Impuestos (21% IVA)
        Double impuestos = subtotal * 0.21;
        
        // Total
        Double total = subtotal + impuestos;
        
        // Generar número de factura
        String numeroFactura = generarNumeroFactura();
        
        // Crear factura
        Factura factura = Factura.builder()
                .solicitudId(solicitudId)
                .tarifaId(tarifa.getId())
                .numeroFactura(numeroFactura)
                .cargoGestion(cargoGestion)
                .costoTransporte(costoTransporte)
                .costoCombustible(costoCombustible)
                .costoEstadias(costoEstadias)
                .subtotal(subtotal)
                .impuestos(impuestos)
                .total(total)
                .estado("PENDIENTE")
                .fechaEmision(LocalDateTime.now())
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .build();
        
        Factura guardada = facturaRepository.save(factura);
        log.info("Factura generada: {} - Total: ${}", numeroFactura, total);
        
        return convertirAFacturaDTO(guardada);
    }
    
    /**
     * Obtiene la factura de una solicitud
     */
    @Transactional(readOnly = true)
    public FacturaDTO obtenerFacturaPorSolicitud(Long solicitudId) {
        Factura factura = facturaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new FacturaNotFoundException("No existe factura para la solicitud ID: " + solicitudId));
        return convertirAFacturaDTO(factura);
    }
    
    /**
     * Lista todas las facturas
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> listarTodasFacturas() {
        return facturaRepository.findAll()
                .stream()
                .map(this::convertirAFacturaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Genera una factura automáticamente consultando solicitud y tramos
     */
    public FacturaDTO generarFactura(Long solicitudId) {
        log.info("📄 Generando factura automática para solicitud ID: {}", solicitudId);
        
        // Verificar que no exista factura previa
        if (facturaRepository.existsBySolicitudId(solicitudId)) {
            throw new IllegalStateException("Ya existe una factura para la solicitud ID: " + solicitudId);
        }
        
        // Consultar solicitud
        SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(solicitudId);
        log.info("Solicitud obtenida: estado={}, costoReal=${}", solicitud.getEstado(), solicitud.getCostoReal());
        
        // Consultar tramos
        List<TramoDTO> tramos = logisticaClient.listarTramosPorSolicitud(solicitudId);
        if (tramos.isEmpty()) {
            throw new RuntimeException("No hay tramos para la solicitud ID: " + solicitudId);
        }
        log.info("Tramos obtenidos: {} tramos", tramos.size());
        
        // Obtener tarifa vigente
        Tarifa tarifa = tarifaRepository.findTarifaVigente(LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No hay tarifa vigente"));
        
        // 1. Cargo de Gestión = 10% del costo real de transporte
        Double cargoGestion = (solicitud.getCostoReal() != null ? solicitud.getCostoReal() : 0.0) * 0.10;
        
        // 2. Costo de Transporte = costo real de la solicitud
        Double costoTransporte = solicitud.getCostoReal() != null ? solicitud.getCostoReal() : 0.0;
        
        // 3. Costo de Combustible = estimado según distancia total
        Double costoCombustible = tramos.stream()
                .mapToDouble(t -> (t.getDistanciaKm() != null ? t.getDistanciaKm() : 0.0) * 
                                  0.08 * // Consumo estimado 0.08 L/km
                                  tarifa.getPrecioCombustibleLitro())
                .sum();
        
        // 4. Costo de Estadías (por ahora 0)
        Double costoEstadias = 0.0;
        
        // Subtotal
        Double subtotal = cargoGestion + costoTransporte + costoCombustible + costoEstadias;
        
        // Impuestos (21% IVA)
        Double impuestos = subtotal * 0.21;
        
        // Total
        Double total = subtotal + impuestos;
        
        // Generar número de factura
        String numeroFactura = generarNumeroFactura();
        
        // Crear factura
        Factura factura = Factura.builder()
                .solicitudId(solicitudId)
                .tarifaId(tarifa.getId())
                .numeroFactura(numeroFactura)
                .cargoGestion(cargoGestion)
                .costoTransporte(costoTransporte)
                .costoCombustible(costoCombustible)
                .costoEstadias(costoEstadias)
                .subtotal(subtotal)
                .impuestos(impuestos)
                .total(total)
                .estado("PENDIENTE")
                .fechaEmision(LocalDateTime.now())
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .build();
        
        Factura guardada = facturaRepository.save(factura);
        log.info("✅ Factura generada: {} - Total: ${}", numeroFactura, total);
        
        return convertirAFacturaDTO(guardada);
    }
    
    /**
     * Registra entrada de contenedor a depósito
     */
    public EstadiaDepositoDTO registrarEntradaDeposito(RegistrarEstadiaRequest request) {
        log.info("Registrando entrada de contenedor {} a depósito {}", 
                request.getContenedorId(), request.getDepositoId());
        
        EstadiaDeposito estadia = EstadiaDeposito.builder()
                .contenedorId(request.getContenedorId())
                .depositoId(request.getDepositoId())
                .fechaEntrada(LocalDateTime.now())
                .costoDia(request.getCostoDia())
                .estado("EN_CURSO")
                .observaciones(request.getObservaciones())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        EstadiaDeposito guardada = estadiaDepositoRepository.save(estadia);
        log.info("Estadía registrada con ID: {}", guardada.getId());
        
        return convertirAEstadiaDTO(guardada);
    }
    
    /**
     * Registra salida de contenedor del depósito y calcula costo
     */
    public EstadiaDepositoDTO registrarSalidaDeposito(Long estadiaId) {
        log.info("Registrando salida de estadía ID: {}", estadiaId);
        
        EstadiaDeposito estadia = estadiaDepositoRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));
        
        if (!"EN_CURSO".equals(estadia.getEstado())) {
            throw new IllegalStateException("La estadía ya fue finalizada");
        }
        
        LocalDateTime salida = LocalDateTime.now();
        long dias = ChronoUnit.DAYS.between(estadia.getFechaEntrada(), salida);
        if (dias < 1) dias = 1; // Mínimo 1 día
        
        Double costoTotal = dias * estadia.getCostoDia();
        
        estadia.setFechaSalida(salida);
        estadia.setDiasEstadia((int) dias);
        estadia.setCostoTotal(costoTotal);
        estadia.setEstado("FINALIZADA");
        estadia.setFechaActualizacion(LocalDateTime.now());
        
        EstadiaDeposito actualizada = estadiaDepositoRepository.save(estadia);
        log.info("Estadía finalizada: {} días × ${} = ${}", dias, estadia.getCostoDia(), costoTotal);
        
        return convertirAEstadiaDTO(actualizada);
    }
    
    /**
     * Lista estadías de un contenedor
     */
    @Transactional(readOnly = true)
    public List<EstadiaDepositoDTO> listarEstadiasPorContenedor(Long contenedorId) {
        return estadiaDepositoRepository.findByContenedorId(contenedorId)
                .stream()
                .map(this::convertirAEstadiaDTO)
                .collect(Collectors.toList());
    }
    
    // Métodos auxiliares
    private String generarNumeroFactura() {
        long count = facturaRepository.count();
        return String.format("FC-%06d-%d", count + 1, LocalDate.now().getYear());
    }
    
    private TarifaDTO convertirATarifaDTO(Tarifa t) {
        return TarifaDTO.builder()
                .id(t.getId())
                .descripcion(t.getDescripcion())
                .cargoGestionBase(t.getCargoGestionBase())
                .cargoGestionPorTramo(t.getCargoGestionPorTramo())
                .precioCombustibleLitro(t.getPrecioCombustibleLitro())
                .factorEstadiaDia(t.getFactorEstadiaDia())
                .fechaVigenciaDesde(t.getFechaVigenciaDesde())
                .fechaVigenciaHasta(t.getFechaVigenciaHasta())
                .estado(t.getEstado())
                .build();
    }
    
    private FacturaDTO convertirAFacturaDTO(Factura f) {
        return FacturaDTO.builder()
                .id(f.getId())
                .solicitudId(f.getSolicitudId())
                .numeroFactura(f.getNumeroFactura())
                .cargoGestion(f.getCargoGestion())
                .costoTransporte(f.getCostoTransporte())
                .costoCombustible(f.getCostoCombustible())
                .costoEstadias(f.getCostoEstadias())
                .subtotal(f.getSubtotal())
                .impuestos(f.getImpuestos())
                .total(f.getTotal())
                .estado(f.getEstado())
                .fechaEmision(f.getFechaEmision())
                .fechaVencimiento(f.getFechaVencimiento())
                .fechaPago(f.getFechaPago())
                .build();
    }
    
    private EstadiaDepositoDTO convertirAEstadiaDTO(EstadiaDeposito e) {
        return EstadiaDepositoDTO.builder()
                .id(e.getId())
                .contenedorId(e.getContenedorId())
                .depositoId(e.getDepositoId())
                .fechaEntrada(e.getFechaEntrada())
                .fechaSalida(e.getFechaSalida())
                .diasEstadia(e.getDiasEstadia())
                .costoDia(e.getCostoDia())
                .costoTotal(e.getCostoTotal())
                .estado(e.getEstado())
                .build();
    }
    
    /**
     * Lista todas las tarifas
     */
    public List<TarifaDTO> listarTodasTarifas() {
        log.info("Listando todas las tarifas");
        return tarifaRepository.findAll().stream()
                .map(this::convertirATarifaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todas las estadías
     */
    public List<EstadiaDepositoDTO> listarTodasEstadias() {
        log.info("Listando todas las estadías");
        return estadiaDepositoRepository.findAll().stream()
                .map(this::convertirAEstadiaDTO)
                .collect(Collectors.toList());
    }
}
