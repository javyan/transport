package com.tpi.solicitudes.service;

import com.tpi.solicitudes.dto.*;
import com.tpi.solicitudes.entity.*;
import com.tpi.solicitudes.repository.*;
import com.tpi.solicitudes.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolicitudService {
    
    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final LogisticaClient logisticaClient;
    private final FacturacionClient facturacionClient;
    
    /**
     * Crea una nueva solicitud de transporte
     * Incluye creación/validación de cliente y contenedor
     */
    public SolicitudDTO crearSolicitud(CrearSolicitudRequest request) {
        log.info("Creando solicitud de transporte");
        
        // 1. Validar/crear cliente
        Cliente cliente = obtenerOCrearCliente(request.getCliente());
        log.info("Cliente procesado: ID={}", cliente.getId());
        
        // 2. Crear contenedor
        Contenedor contenedor = crearContenedor(cliente.getId(), request.getContenedor(), 
                                                request.getOrigenDireccion());
        log.info("Contenedor creado: ID={}, Identificación={}", 
                contenedor.getId(), contenedor.getNumeroIdentificacion());
        
        // 3. Crear solicitud
        Solicitud solicitud = Solicitud.builder()
            .clienteId(cliente.getId())
            .contenedorId(contenedor.getId())
            .origenDireccion(request.getOrigenDireccion())
            .destinoDireccion(request.getDestinoDireccion())
            .estado("BORRADOR")
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .build();
        
        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con ID: {}", guardada.getId());
        
        // Integración con ms-logistica: calcular rutas automáticamente
        List<RutaTentativaDTO> rutasCalculadas = null;
        try {
            rutasCalculadas = logisticaClient.calcularRutasTentativas(
                guardada.getId(),
                request.getOrigenDireccion(),
                request.getDestinoDireccion(),
                request.getContenedor().getPesoKg(),
                request.getContenedor().getVolumenM3()
            );
            
            // Usar la ruta DIRECTA (más simple) para estimaciones iniciales en la solicitud
            if (rutasCalculadas != null && !rutasCalculadas.isEmpty()) {
                RutaTentativaDTO rutaDirecta = rutasCalculadas.stream()
                        .filter(r -> "DIRECTA".equals(r.getEstrategia()))
                        .findFirst()
                        .orElse(rutasCalculadas.get(0)); // Fallback a primera ruta
                
                guardada.setCostoEstimado(rutaDirecta.getCostoTotalEstimado());
                guardada.setTiempoEstimadoHoras(rutaDirecta.getTiempoEstimadoHoras());
                guardada = solicitudRepository.save(guardada);
                
                log.info("✅ {} rutas calculadas para solicitud {}. Ruta directa: {}km, ${}, {}hs", 
                        rutasCalculadas.size(), guardada.getId(), 
                        rutaDirecta.getDistanciaTotal(), 
                        rutaDirecta.getCostoTotalEstimado(), 
                        rutaDirecta.getTiempoEstimadoHoras());
            }
        } catch (Exception e) {
            log.warn("No se pudo calcular ruta automáticamente para solicitud {}: {}", 
                    guardada.getId(), e.getMessage());
        }
        
        return convertirASolicitudDTO(guardada, rutasCalculadas);
    }
    
    /**
     * Obtiene o crea un cliente según el email
     */
    private Cliente obtenerOCrearCliente(ClienteDTO dto) {
        return clienteRepository.findByEmail(dto.getEmail())
            .orElseGet(() -> {
                log.info("Cliente no existe, creando nuevo con email: {}", dto.getEmail());
                Cliente nuevo = Cliente.builder()
                    .nombreCompleto(dto.getNombreCompleto())
                    .email(dto.getEmail())
                    .telefono(dto.getTelefono())
                    .direccion(dto.getDireccion())
                    .fechaRegistro(LocalDateTime.now())
                    .build();
                return clienteRepository.save(nuevo);
            });
    }
    
    /**
     * Crea un contenedor asociado al cliente
     */
    private Contenedor crearContenedor(Long clienteId, 
                                      CrearSolicitudRequest.ContenedorSimpleDTO dto,
                                      String ubicacionInicial) {
        
        // Validar que no exista otro contenedor con el mismo número
        if (contenedorRepository.existsByNumeroIdentificacion(dto.getNumeroIdentificacion())) {
            throw new IllegalArgumentException(
                "Ya existe un contenedor con la identificación: " + dto.getNumeroIdentificacion()
            );
        }
        
        Contenedor contenedor = Contenedor.builder()
            .clienteId(clienteId)
            .numeroIdentificacion(dto.getNumeroIdentificacion())
            .pesoKg(dto.getPesoKg())
            .volumenM3(dto.getVolumenM3())
            .tipo(dto.getTipo() != null ? dto.getTipo() : "ESTANDAR")
            .estado("CREADO")
            .ubicacionActual(ubicacionInicial)
            .fechaCreacion(LocalDateTime.now())
            .observaciones(dto.getObservaciones())
            .build();
        
        return contenedorRepository.save(contenedor);
    }
    
    /**
     * Obtiene una solicitud por ID
     */
    @Transactional(readOnly = true)
    public SolicitudDTO obtenerSolicitud(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        return convertirASolicitudDTO(solicitud);
    }
    
    /**
     * Confirma una solicitud (transición BORRADOR → PLANIFICADA)
     * Requiere que ya tenga una ruta asignada
     */
    public SolicitudDTO confirmarSolicitud(Long id) {
        log.info("Confirmando solicitud ID: {}", id);
        
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        
        if (!"BORRADOR".equals(solicitud.getEstado())) {
            throw new IllegalStateException(
                "Solo se pueden confirmar solicitudes en estado BORRADOR. Estado actual: " + solicitud.getEstado());
        }
        
        // Validar que tenga costo estimado (indica que se calcularon rutas)
        if (solicitud.getCostoEstimado() == null || solicitud.getCostoEstimado() <= 0) {
            throw new IllegalStateException(
                "La solicitud no tiene rutas calculadas. Primero debe asignar una ruta.");
        }
        
        solicitud.setEstado("PLANIFICADA");
        solicitud.setFechaActualizacion(LocalDateTime.now());
        
        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("✅ Solicitud {} confirmada. Estado: PLANIFICADA", id);
        
        return convertirASolicitudDTO(actualizada);
    }
    
    /**
     * Actualiza el estado de una solicitud (usado internamente por otros microservicios)
     */
    public void actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado de solicitud {} a {}", id, nuevoEstado);
        
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        
        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaActualizacion(LocalDateTime.now());
        
        // Si se finaliza, calcular costos reales y generar factura
        if ("FINALIZADA".equals(nuevoEstado)) {
            finalizarSolicitudConCalculos(solicitud);
        }
        
        solicitudRepository.save(solicitud);
        log.info("✅ Estado de solicitud {} actualizado a {}", id, nuevoEstado);
    }
    
    /**
     * Finaliza solicitud calculando costos reales y generando factura
     */
    private void finalizarSolicitudConCalculos(Solicitud solicitud) {
        log.info("🧮 Calculando costos reales para solicitud {}", solicitud.getId());
        
        try {
            // Obtener todos los tramos de la solicitud
            List<TramoDTO> tramos = logisticaClient.listarTramosPorSolicitud(solicitud.getId());
            
            if (tramos.isEmpty()) {
                log.warn("⚠️ No hay tramos para la solicitud {}", solicitud.getId());
                return;
            }
            
            // Calcular costo real total (sumar distancias * tarifa promedio)
            double costoRealTotal = 0.0;
            double distanciaTotal = 0.0;
            
            for (TramoDTO tramo : tramos) {
                if (tramo.getDistanciaKm() != null) {
                    distanciaTotal += tramo.getDistanciaKm();
                    // Tarifa promedio: $150 por km
                    costoRealTotal += tramo.getDistanciaKm() * 150.0;
                }
            }
            
            // Calcular tiempo real en horas
            Double tiempoRealHoras = null;
            LocalDateTime primeraFechaInicio = tramos.stream()
                    .filter(t -> t.getFechaInicio() != null)
                    .map(TramoDTO::getFechaInicio)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            
            LocalDateTime ultimaFechaFin = tramos.stream()
                    .filter(t -> t.getFechaFin() != null)
                    .map(TramoDTO::getFechaFin)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            
            if (primeraFechaInicio != null && ultimaFechaFin != null) {
                long minutos = java.time.Duration.between(primeraFechaInicio, ultimaFechaFin).toMinutes();
                tiempoRealHoras = minutos / 60.0;
            }
            
            // Actualizar solicitud
            solicitud.setCostoReal(costoRealTotal);
            solicitud.setTiempoRealHoras(tiempoRealHoras);
            solicitud.setFechaEntrega(LocalDateTime.now());
            
            log.info("✅ Costos calculados - Solicitud {}: Distancia={}km, Costo=${}, Tiempo={}h", 
                    solicitud.getId(), distanciaTotal, costoRealTotal, tiempoRealHoras);
            
            // Intentar generar factura (opcional, puede fallar si servicio no disponible)
            try {
                FacturaDTO factura = facturacionClient.generarFactura(solicitud.getId());
                log.info("📄 Factura generada: {} - Total: ${}", 
                        factura.getNumeroFactura(), factura.getTotal());
            } catch (Exception e) {
                log.warn("⚠️ No se pudo generar factura automáticamente: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ Error al calcular costos reales para solicitud {}: {}", 
                    solicitud.getId(), e.getMessage());
        }
    }
    
    /**
     * Lista todas las solicitudes pendientes (PROGRAMADA o EN_TRANSITO)
     */
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarSolicitudesPendientes() {
        return solicitudRepository.findPendientes()
            .stream()
            .map(this::convertirASolicitudDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista solicitudes por estado
     */
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarSolicitudesPorEstado(String estado) {
        return solicitudRepository.findByEstado(estado)
            .stream()
            .map(this::convertirASolicitudDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista solicitudes de un cliente
     */
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarSolicitudesDeCliente(Long clienteId) {
        return solicitudRepository.findByClienteId(clienteId)
            .stream()
            .map(this::convertirASolicitudDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Actualiza costos y tiempos estimados
     */
    public SolicitudDTO actualizarCostoEstimado(Long id, Double costoEstimado, Double tiempoEstimadoHoras) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        
        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimadoHoras(tiempoEstimadoHoras);
        solicitud.setEstado("PROGRAMADA");
        solicitud.setFechaActualizacion(LocalDateTime.now());
        
        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("Solicitud ID={} actualizada con costo=${} y tiempo={}hs", 
                id, costoEstimado, tiempoEstimadoHoras);
        
        return convertirASolicitudDTO(actualizada);
    }
    
    /**
     * Finaliza una solicitud registrando costos y tiempos reales
     */
    public SolicitudDTO finalizarSolicitud(Long id, Double costoReal, Double tiempoRealHoras) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        
        solicitud.setCostoReal(costoReal);
        solicitud.setTiempoRealHoras(tiempoRealHoras);
        solicitud.setEstado("ENTREGADA");
        solicitud.setFechaEntrega(LocalDateTime.now());
        solicitud.setFechaActualizacion(LocalDateTime.now());
        
        // Actualizar estado del contenedor
        Contenedor contenedor = contenedorRepository.findById(solicitud.getContenedorId())
            .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));
        contenedor.setEstado("ENTREGADO");
        contenedor.setUbicacionActual(solicitud.getDestinoDireccion());
        // Lat/Lon eliminados - solo usamos direcciones
        contenedorRepository.save(contenedor);
        
        Solicitud finalizada = solicitudRepository.save(solicitud);
        log.info("Solicitud ID={} finalizada. Costo real=${}, Tiempo real={}hs", 
                id, costoReal, tiempoRealHoras);
        
        return convertirASolicitudDTO(finalizada);
    }
    
    /**
     * Obtiene la factura de una solicitud desde ms-facturacion-v2
     */
    public FacturaDTO obtenerFacturaSolicitud(Long solicitudId) {
        // Verificar que la solicitud existe
        solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));
        
        try {
            FacturaDTO factura = facturacionClient.obtenerFacturaPorSolicitud(solicitudId);
            log.info("Factura obtenida para solicitud {}: {} - ${}", 
                    solicitudId, factura.getNumeroFactura(), factura.getTotal());
            return factura;
        } catch (Exception e) {
            log.error("Error al obtener factura para solicitud {}: {}", solicitudId, e.getMessage());
            throw new RuntimeException("No se pudo obtener la factura: " + e.getMessage());
        }
    }
    
    /**
     * Lista todas las solicitudes
     */
    public List<SolicitudDTO> listarTodasSolicitudes() {
        log.info("Listando todas las solicitudes");
        return solicitudRepository.findAll().stream()
                .map(this::convertirASolicitudDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los clientes
     */
    public List<ClienteDTO> listarTodosClientes() {
        log.info("Listando todos los clientes");
        return clienteRepository.findAll().stream()
                .map(this::convertirAClienteDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los contenedores
     */
    public List<ContenedorDTO> listarTodosContenedores() {
        log.info("Listando todos los contenedores");
        return contenedorRepository.findAll().stream()
                .map(this::convertirAContenedorDTO)
                .collect(Collectors.toList());
    }
    
    // Métodos de conversión
    private SolicitudDTO convertirASolicitudDTO(Solicitud s) {
        return convertirASolicitudDTO(s, null);
    }
    
    private SolicitudDTO convertirASolicitudDTO(Solicitud s, List<RutaTentativaDTO> rutasTentativas) {
        // Obtener datos del contenedor si existe
        ContenedorDTO contenedorDTO = null;
        if (s.getContenedorId() != null) {
            Contenedor contenedor = contenedorRepository.findById(s.getContenedorId()).orElse(null);
            if (contenedor != null) {
                contenedorDTO = convertirAContenedorDTO(contenedor);
            }
        }
        
        return SolicitudDTO.builder()
            .id(s.getId())
            .clienteId(s.getClienteId())
            .contenedorId(s.getContenedorId())
            .origenDireccion(s.getOrigenDireccion())
            .destinoDireccion(s.getDestinoDireccion())
            .estado(s.getEstado())
            .costoEstimado(s.getCostoEstimado())
            .tiempoEstimadoHoras(s.getTiempoEstimadoHoras())
            .costoReal(s.getCostoReal())
            .tiempoRealHoras(s.getTiempoRealHoras())
            .fechaCreacion(s.getFechaCreacion())
            .fechaActualizacion(s.getFechaActualizacion())
            .fechaEntrega(s.getFechaEntrega())
            .contenedor(contenedorDTO)
            .rutasTentativas(rutasTentativas)
            .build();
    }
    
    private ClienteDTO convertirAClienteDTO(Cliente c) {
        return ClienteDTO.builder()
            .id(c.getId())
            .nombreCompleto(c.getNombreCompleto())
            .email(c.getEmail())
            .telefono(c.getTelefono())
            .direccion(c.getDireccion())
            .build();
    }
    
    private ContenedorDTO convertirAContenedorDTO(Contenedor c) {
        return ContenedorDTO.builder()
            .id(c.getId())
            .numeroIdentificacion(c.getNumeroIdentificacion())
            .clienteId(c.getClienteId())
            .pesoKg(c.getPesoKg())
            .volumenM3(c.getVolumenM3())
            .tipo(c.getTipo())
            .estado(c.getEstado())
            .ubicacionActual(c.getUbicacionActual())
            .latActual(c.getLatActual())
            .lonActual(c.getLonActual())
            .observaciones(c.getObservaciones())
            .build();
    }
    
    /**
     * Obtiene todas las rutas calculadas para una solicitud
     */
    public List<RutaTentativaDTO> obtenerRutasSolicitud(Long solicitudId) {
        log.info("Obteniendo rutas para solicitud {}", solicitudId);
        
        // Verificar que la solicitud existe
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));
        
        // Obtener rutas desde ms-logistica
        try {
            return logisticaClient.listarRutasPorSolicitud(solicitudId);
        } catch (Exception e) {
            log.error("Error al obtener rutas de solicitud {}: {}", solicitudId, e.getMessage());
            throw new RuntimeException("No se pudieron obtener las rutas de la solicitud", e);
        }
    }
}

