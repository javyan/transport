package com.tpi.logistica.service;

import com.tpi.logistica.client.FacturacionClient;
import com.tpi.logistica.client.FacturaDTO;
import com.tpi.logistica.client.SolicitudClient;
import com.tpi.logistica.client.SolicitudDTO;
import com.tpi.logistica.dto.*;
import com.tpi.logistica.entity.*;
import com.tpi.logistica.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogisticaService {
    
    private final TramoRepository tramoRepository;
    private final CamionRepository camionRepository;
    private final DepositoRepository depositoRepository;
    private final TransportistaRepository transportistaRepository;
    private final GoogleMapsService googleMapsService;
    private final SolicitudClient solicitudClient;
    private final FacturacionClient facturacionClient;
    
    // Mapa para rastrear estadías activas por contenedor
    private final Map<Long, Long> estadiasActivas = new HashMap<>();
    
    /**
     * Calcula rutas tentativas para una solicitud
     * Por ahora genera ruta DIRECTA (sin depósitos intermedios)
     * TODO: Implementar lógica para rutas con depósitos según distancia
     */
    public RutaTentativaDTO calcularRutasTentativas(Long solicitudId, 
                                                     String origenDireccion, 
                                                     String destinoDireccion) {
        log.info("Calculando rutas tentativas para solicitud ID: {}", solicitudId);
        log.info("Origen: {} | Destino: {}", origenDireccion, destinoDireccion);
        
        // Calcular distancia directa usando direcciones
        Double distancia = googleMapsService.calcularDistancia(origenDireccion, destinoDireccion);
        
        if (distancia == null) {
            log.warn("No se pudo calcular la distancia real. Usando estimación aproximada de 700km");
            distancia = 700.0; // Distancia promedio aproximada para rutas largas en Argentina
        }
        
        Double tiempo = googleMapsService.calcularTiempoEstimado(distancia);
        
        // Crear tramo directo (sin guardar aún)
        TramoDTO tramoDirecto = TramoDTO.builder()
                .solicitudId(solicitudId)
                .origenTipo("CLIENTE")
                .origenDireccion(origenDireccion)
                .destinoTipo("CLIENTE")
                .destinoDireccion(destinoDireccion)
                .tipoTramo("DIRECTO")
                .distanciaKm(distancia)
                .ordenTramo(1)
                .estado("ESTIMADO")
                .build();
        
        List<TramoDTO> tramos = new ArrayList<>();
        tramos.add(tramoDirecto);
        
        // Estimar costo (se refinará con camión específico)
        Double costoEstimado = distancia * 150.0; // Costo promedio por km
        
        return RutaTentativaDTO.builder()
                .solicitudId(solicitudId)
                .tramos(tramos)
                .distanciaTotal(distancia)
                .costoEstimadoTransporte(costoEstimado)
                .tiempoEstimadoHoras(tiempo)
                .estrategia("DIRECTA")
                .build();
    }
    
    /**
     * Crea los tramos en la BD para una solicitud
     */
    public List<TramoDTO> crearTramos(Long solicitudId,
                                      String origenDireccion, 
                                      String destinoDireccion) {
        log.info("Creando tramos para solicitud ID: {}", solicitudId);
        log.info("Origen: {} | Destino: {}", origenDireccion, destinoDireccion);
        
        Double distancia = googleMapsService.calcularDistancia(origenDireccion, destinoDireccion);
        
        if (distancia == null) {
            log.warn("No se pudo calcular la distancia real. Usando estimación aproximada de 700km");
            distancia = 700.0; // Distancia promedio aproximada para rutas largas en Argentina
        }
        
        Tramo tramo = Tramo.builder()
                .solicitudId(solicitudId)
                .origenTipo("CLIENTE")
                .origenDireccion(origenDireccion)
                .destinoTipo("CLIENTE")
                .destinoDireccion(destinoDireccion)
                .tipoTramo("DIRECTO")
                .distanciaKm(distancia)
                .ordenTramo(1)
                .estado("ESTIMADO")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        Tramo guardado = tramoRepository.save(tramo);
        log.info("Tramo creado con ID: {} ({}km)", guardado.getId(), distancia);
        
        List<TramoDTO> result = new ArrayList<>();
        result.add(convertirATramoDTO(guardado));
        return result;
    }
    
    /**
     * Lista camiones disponibles con capacidad suficiente
     */
    @Transactional(readOnly = true)
    public List<CamionDTO> listarCamionesDisponibles(Double pesoRequerido, Double volumenRequerido) {
        log.info("Buscando camiones disponibles: peso>={}kg, volumen>={}m3", pesoRequerido, volumenRequerido);
        return camionRepository.findDisponiblesConCapacidad(pesoRequerido, volumenRequerido)
                .stream()
                .map(this::convertirACamionDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Asigna un camión y transportista a un tramo.
     * Obtiene el peso y volumen del contenedor de la solicitud automáticamente.
     */
    public TramoDTO asignarCamion(Long tramoId, Long camionId, Long transportistaId) {
        log.info("Asignando camión ID={} y transportista ID={} a tramo ID={}", 
                camionId, transportistaId, tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));
        
        // Obtener datos del contenedor de la solicitud
        SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(tramo.getSolicitudId());
        if (solicitud.getContenedor() == null) {
            throw new RuntimeException("La solicitud no tiene contenedor asociado");
        }
        
        Double pesoKg = solicitud.getContenedor().getPesoKg();
        Double volumenM3 = solicitud.getContenedor().getVolumenM3();
        
        log.info("Datos del contenedor obtenidos: {}kg, {}m3", pesoKg, volumenM3);
        
        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con ID: " + camionId));
        
        Transportista transportista = transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado con ID: " + transportistaId));
        
        // Validar estado del camión
        if (!"DISPONIBLE".equals(camion.getEstado())) {
            throw new IllegalStateException("El camión no está disponible. Estado actual: " + camion.getEstado());
        }
        
        // Validar estado del transportista
        if (!"DISPONIBLE".equals(transportista.getEstado())) {
            throw new IllegalStateException("El transportista no está disponible. Estado actual: " + transportista.getEstado());
        }
        
        // Validar capacidad de peso
        if (pesoKg > camion.getCapacidadKg()) {
            throw new IllegalArgumentException(
                String.format("El camión no tiene capacidad de peso suficiente. Requerido: %.2f kg, Disponible: %.2f kg",
                    pesoKg, camion.getCapacidadKg()));
        }
        
        // Validar capacidad de volumen
        if (volumenM3 > camion.getCapacidadM3()) {
            throw new IllegalArgumentException(
                String.format("El camión no tiene capacidad de volumen suficiente. Requerido: %.2f m³, Disponible: %.2f m³",
                    volumenM3, camion.getCapacidadM3()));
        }
        
        log.info("✅ Camión y transportista validados: capacidad {}kg/{}m3 vs carga {}kg/{}m3",
                camion.getCapacidadKg(), camion.getCapacidadM3(), pesoKg, volumenM3);
        
        // Asignar camión y transportista al tramo
        tramo.setCamionId(camionId);
        tramo.setTransportistaId(transportistaId);
        tramo.setEstado("ASIGNADO");
        tramo.setFechaActualizacion(LocalDateTime.now());
        
        // Actualizar estados
        camion.setEstado("ASIGNADO");
        camionRepository.save(camion);
        
        transportista.setEstado("EN_USO");
        transportistaRepository.save(transportista);
        
        Tramo actualizado = tramoRepository.save(tramo);
        log.info("✅ Camión y transportista asignados exitosamente al tramo");
        
        return convertirATramoDTO(actualizado);
    }
    
    /**
     * Inicia un tramo (transportista comienza el viaje)
     */
    public TramoDTO iniciarTramo(Long tramoId) {
        log.info("Iniciando tramo ID: {}", tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));
        
        if (tramo.getCamionId() == null) {
            throw new IllegalStateException("No se puede iniciar un tramo sin camión asignado");
        }
        
        if (tramo.getTransportistaId() == null) {
            throw new IllegalStateException("No se puede iniciar un tramo sin transportista asignado");
        }
        
        if (!"ASIGNADO".equals(tramo.getEstado())) {
            throw new IllegalStateException("Solo se pueden iniciar tramos en estado ASIGNADO. Estado actual: " + tramo.getEstado());
        }
        
        tramo.setEstado("INICIADO");
        tramo.setFechaInicio(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());
        
        // SI EL ORIGEN ES DEPOSITO → Registrar SALIDA de estadía
        if ("DEPOSITO".equals(tramo.getOrigenTipo()) && tramo.getOrigenId() != null) {
            try {
                // Obtener contenedorId de la solicitud
                SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(tramo.getSolicitudId());
                Long contenedorId = solicitud.getContenedor() != null ? solicitud.getContenedor().getId() : null;
                
                if (contenedorId != null) {
                    // Buscar estadía activa y registrar salida
                    Long estadiaId = estadiasActivas.get(contenedorId);
                    if (estadiaId != null) {
                        log.info("📤 Registrando SALIDA de depósito {} para contenedor {}", tramo.getOrigenId(), contenedorId);
                        EstadiaResponseDTO estadia = facturacionClient.registrarSalidaDeposito(estadiaId);
                        log.info("✅ Estadía registrada: {} días | Costo: ${}", estadia.getDiasEstadia(), estadia.getCostoTotal());
                        estadiasActivas.remove(contenedorId);
                    } else {
                        log.warn("⚠️ No se encontró estadía activa para contenedor {}", contenedorId);
                    }
                }
            } catch (Exception e) {
                log.error("❌ Error al registrar salida de depósito: {}", e.getMessage());
            }
        }
        
        // Actualizar estado del camión
        Camion camion = camionRepository.findById(tramo.getCamionId())
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
        camion.setEstado("EN_USO");
        camionRepository.save(camion);
        
        Tramo actualizado = tramoRepository.save(tramo);
        
        // Si es el primer tramo de la solicitud, cambiar solicitud a EN_TRANSITO
        List<Tramo> tramosIniciados = tramoRepository.findBySolicitudIdAndEstado(
                tramo.getSolicitudId(), "INICIADO");
        
        if (tramosIniciados.size() == 1) { // Solo este tramo está iniciado
            log.info("✅ Primer tramo iniciado. Cambiando solicitud {} a EN_TRANSITO", tramo.getSolicitudId());
            try {
                solicitudClient.actualizarEstado(tramo.getSolicitudId(), "EN_TRANSITO");
            } catch (Exception e) {
                log.error("Error al actualizar estado de solicitud: {}", e.getMessage());
            }
        }
        
        log.info("✅ Tramo iniciado. Estado: INICIADO");
        
        return convertirATramoDTO(actualizado);
    }
    
    /**
     * Finaliza un tramo (transportista completa el viaje)
     */
    public TramoDTO finalizarTramo(Long tramoId) {
        log.info("Finalizando tramo ID: {}", tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));
        
        if (!"INICIADO".equals(tramo.getEstado())) {
            throw new IllegalStateException("Solo se pueden finalizar tramos en estado INICIADO. Estado actual: " + tramo.getEstado());
        }
        
        tramo.setEstado("FINALIZADO");
        tramo.setFechaFin(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());
        
        // Liberar camión
        if (tramo.getCamionId() != null) {
            Camion camion = camionRepository.findById(tramo.getCamionId())
                    .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
            camion.setEstado("DISPONIBLE");
            camion.setUbicacionActual(tramo.getDestinoDireccion());
            camionRepository.save(camion);
            log.info("✅ Camión {} liberado y marcado como DISPONIBLE", tramo.getCamionId());
        }
        
        // Liberar transportista
        if (tramo.getTransportistaId() != null) {
            Transportista transportista = transportistaRepository.findById(tramo.getTransportistaId())
                    .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));
            transportista.setEstado("DISPONIBLE");
            transportistaRepository.save(transportista);
            log.info("✅ Transportista {} liberado y marcado como DISPONIBLE", tramo.getTransportistaId());
        }
        
        Tramo actualizado = tramoRepository.save(tramo);
        log.info("✅ Tramo finalizado. Estado: FINALIZADO");
        
        // SI EL DESTINO ES DEPOSITO → Registrar ENTRADA de estadía
        if ("DEPOSITO".equals(tramo.getDestinoTipo()) && tramo.getDestinoId() != null) {
            try {
                // Obtener contenedorId de la solicitud
                SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(tramo.getSolicitudId());
                Long contenedorId = solicitud.getContenedor() != null ? solicitud.getContenedor().getId() : null;
                
                if (contenedorId != null) {
                    // Obtener costo diario del depósito
                    Deposito deposito = depositoRepository.findById(tramo.getDestinoId())
                            .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));
                    
                    log.info("📥 Registrando ENTRADA a depósito {} para contenedor {}", tramo.getDestinoId(), contenedorId);
                    EstadiaRequestDTO request = EstadiaRequestDTO.builder()
                            .contenedorId(contenedorId)
                            .depositoId(tramo.getDestinoId())
                            .costoDia(deposito.getCostoDia())
                            .build();
                    
                    EstadiaResponseDTO estadia = facturacionClient.registrarEntradaDeposito(request);
                    estadiasActivas.put(contenedorId, estadia.getId());
                    log.info("✅ Estadía registrada con ID: {} | Costo por día: ${}", estadia.getId(), estadia.getCostoDia());
                }
            } catch (Exception e) {
                log.error("❌ Error al registrar entrada a depósito: {}", e.getMessage());
            }
        }
        
        // Verificar si todos los tramos de la solicitud están finalizados
        List<Tramo> todosLosTramos = tramoRepository.findBySolicitudId(tramo.getSolicitudId());
        boolean todosFinalizados = todosLosTramos.stream()
                .allMatch(t -> "FINALIZADO".equals(t.getEstado()));
        
        if (todosFinalizados) {
            log.info("✅ Todos los tramos de la solicitud {} están FINALIZADOS. Finalizando solicitud...", 
                    tramo.getSolicitudId());
            try {
                // Calcular costo real total basado en distancia (ejemplo: $10000 por km)
                double costoRealTotal = todosLosTramos.stream()
                        .mapToDouble(t -> t.getDistanciaKm() != null ? t.getDistanciaKm() * 10000.0 : 0.0)
                        .sum();
                
                // Calcular tiempo real total
                double tiempoRealTotal = todosLosTramos.stream()
                        .mapToDouble(t -> {
                            if (t.getFechaInicio() != null && t.getFechaFin() != null) {
                                return java.time.Duration.between(t.getFechaInicio(), t.getFechaFin()).toMinutes() / 60.0;
                            }
                            return 0.0;
                        })
                        .sum();
                
                solicitudClient.finalizarSolicitud(tramo.getSolicitudId(), costoRealTotal, tiempoRealTotal);
                log.info("✅ Solicitud {} finalizada exitosamente. Costo real: ${}, Tiempo real: {} horas", 
                        tramo.getSolicitudId(), costoRealTotal, tiempoRealTotal);
                
                // Generar factura automáticamente
                try {
                    log.info("💰 Generando factura automáticamente para solicitud {}...", tramo.getSolicitudId());
                    FacturaDTO factura = facturacionClient.generarFactura(tramo.getSolicitudId());
                    log.info("✅ Factura generada exitosamente: {} | Total: ${}", 
                            factura.getNumeroFactura(), factura.getTotal());
                } catch (Exception e) {
                    log.error("❌ Error al generar factura para solicitud {}: {}", 
                            tramo.getSolicitudId(), e.getMessage());
                    // No lanzamos la excepción para que el tramo se marque como finalizado de todas formas
                }
            } catch (Exception e) {
                log.error("❌ Error al finalizar solicitud {}: {}", tramo.getSolicitudId(), e.getMessage());
            }
        } else {
            long finalizados = todosLosTramos.stream()
                    .filter(t -> "FINALIZADO".equals(t.getEstado())).count();
            log.info("📊 Tramos finalizados: {}/{}", finalizados, todosLosTramos.size());
        }
        
        return convertirATramoDTO(actualizado);
    }
    
    /**
     * Lista tramos de una solicitud
     */
    @Transactional(readOnly = true)
    public List<TramoDTO> listarTramosPorSolicitud(Long solicitudId) {
        return tramoRepository.findBySolicitudIdOrderByOrdenTramo(solicitudId)
                .stream()
                .map(this::convertirATramoDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista tramos asignados a un transportista
     */
    @Transactional(readOnly = true)
    public List<TramoDTO> listarTramosPorTransportista(Long transportistaId) {
        return tramoRepository.findByTransportistaId(transportistaId)
                .stream()
                .map(this::convertirATramoDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los tramos del sistema
     */
    @Transactional(readOnly = true)
    public List<TramoDTO> listarTodosTramos() {
        return tramoRepository.findAll()
                .stream()
                .map(this::convertirATramoDTO)
                .collect(Collectors.toList());
    }
    
    // Métodos de conversión
    private TramoDTO convertirATramoDTO(Tramo t) {
        return TramoDTO.builder()
                .id(t.getId())
                .solicitudId(t.getSolicitudId())
                .camionId(t.getCamionId())
                .transportistaId(t.getTransportistaId())
                .origenTipo(t.getOrigenTipo())
                .origenId(t.getOrigenId())
                .origenDireccion(t.getOrigenDireccion())
                .destinoTipo(t.getDestinoTipo())
                .destinoId(t.getDestinoId())
                .destinoDireccion(t.getDestinoDireccion())
                .tipoTramo(t.getTipoTramo())
                .distanciaKm(t.getDistanciaKm())
                .ordenTramo(t.getOrdenTramo())
                .estado(t.getEstado())
                .fechaInicio(t.getFechaInicio())
                .fechaFin(t.getFechaFin())
                .build();
    }
    
    private CamionDTO convertirACamionDTO(Camion c) {
        return CamionDTO.builder()
                .id(c.getId())
                .transportistaId(c.getTransportistaId())
                .patente(c.getPatente())
                .marca(c.getMarca())
                .modelo(c.getModelo())
                .anio(c.getAnio())
                .capacidadKg(c.getCapacidadKg())
                .capacidadM3(c.getCapacidadM3())
                .consumoCombustibleLtKm(c.getConsumoCombustibleLtKm())
                .costoKm(c.getCostoKm())
                .estado(c.getEstado())
                .ubicacionActual(c.getUbicacionActual())
                .latActual(c.getLatActual())
                .lonActual(c.getLonActual())
                .build();
    }
    
    /**
     * Lista todos los camiones
     */
    public List<CamionDTO> listarTodosCamiones() {
        log.info("Listando todos los camiones");
        return camionRepository.findAll().stream()
                .map(this::convertirACamionDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los transportistas
     */
    public List<TransportistaDTO> listarTodosTransportistas() {
        log.info("Listando todos los transportistas");
        return transportistaRepository.findAll().stream()
                .map(this::convertirATransportistaDTO)
                .collect(Collectors.toList());
    }
    
    private TransportistaDTO convertirATransportistaDTO(Transportista t) {
        return TransportistaDTO.builder()
                .id(t.getId())
                .nombre(t.getNombreCompleto())
                .apellido(null)  // No hay campo apellido separado
                .licencia(t.getLicenciaTipo())
                .telefono(t.getTelefono())
                .estado(t.getEstado())
                .build();
    }
    
    /**
     * Lista todos los depósitos
     */
    public List<DepositoDTO> listarTodosDepositos() {
        log.info("Listando todos los depósitos");
        return depositoRepository.findAll().stream()
                .map(this::convertirADepositoDTO)
                .collect(Collectors.toList());
    }
    
    private DepositoDTO convertirADepositoDTO(Deposito d) {
        return DepositoDTO.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .direccion(d.getDireccion())
                .capacidadMaximaM3(d.getCapacidadMaximaM3())
                .costoDia(d.getCostoDia())
                .estado(d.getEstado())
                .lat(d.getLat())
                .lon(d.getLon())
                .build();
    }
}

