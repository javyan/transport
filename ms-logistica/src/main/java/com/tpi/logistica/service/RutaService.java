package com.tpi.logistica.service;

import com.tpi.logistica.dto.*;
import com.tpi.logistica.entity.*;
import com.tpi.logistica.repository.*;
import com.tpi.logistica.client.SolicitudClient;
import com.tpi.logistica.client.SolicitudDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RutaService {
    
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final GoogleMapsService googleMapsService;
    private final SolicitudClient solicitudClient;
    
    /**
     * Calcula múltiples rutas tentativas para que el operador elija
     * Genera 3 opciones: DIRECTA, UN_DEPOSITO, MULTIPLES_DEPOSITOS
     */
    public List<RutaDTO> calcularRutasTentativas(Long solicitudId,
                                                   String origenDireccion,
                                                   String destinoDireccion,
                                                   Double pesoKg,
                                                   Double volumenM3) {
        log.info("🔍 Calculando rutas tentativas para solicitud {}", solicitudId);
        
        List<RutaDTO> rutasTentativas = new ArrayList<>();
        
        // 1. RUTA DIRECTA (sin depósitos)
        RutaDTO rutaDirecta = calcularRutaDirecta(solicitudId, origenDireccion, destinoDireccion);
        if (rutaDirecta != null) {
            rutasTentativas.add(rutaDirecta);
            log.info("✅ Ruta directa: {}km, ${}, {}hs", 
                    rutaDirecta.getDistanciaTotal(), 
                    rutaDirecta.getCostoTotalEstimado(), 
                    rutaDirecta.getTiempoEstimadoHoras());
        }
        
        // 2. RUTA CON UN DEPÓSITO (si la distancia es > 500km)
        if (rutaDirecta != null && rutaDirecta.getDistanciaTotal() > 500) {
            RutaDTO rutaConDeposito = calcularRutaConUnDeposito(solicitudId, origenDireccion, destinoDireccion);
            if (rutaConDeposito != null) {
                rutasTentativas.add(rutaConDeposito);
                log.info("✅ Ruta con 1 depósito: {}km, ${}, {}hs", 
                        rutaConDeposito.getDistanciaTotal(), 
                        rutaConDeposito.getCostoTotalEstimado(), 
                        rutaConDeposito.getTiempoEstimadoHoras());
            }
        }
        
        // 3. RUTA CON MÚLTIPLES DEPÓSITOS (si distancia > 1000km)
        if (rutaDirecta != null && rutaDirecta.getDistanciaTotal() > 1000) {
            RutaDTO rutaMultiple = calcularRutaConMultiplesDepositos(solicitudId, origenDireccion, destinoDireccion);
            if (rutaMultiple != null) {
                rutasTentativas.add(rutaMultiple);
                log.info("✅ Ruta con múltiples depósitos: {}km, ${}, {}hs", 
                        rutaMultiple.getDistanciaTotal(), 
                        rutaMultiple.getCostoTotalEstimado(), 
                        rutaMultiple.getTiempoEstimadoHoras());
            }
        }
        
        log.info("📊 Total de rutas tentativas generadas: {}", rutasTentativas.size());
        return rutasTentativas;
    }
    
    /**
     * Calcula ruta directa sin paradas
     */
    private RutaDTO calcularRutaDirecta(Long solicitudId, String origen, String destino) {
        Double distancia = googleMapsService.calcularDistancia(origen, destino);
        if (distancia == null) {
            distancia = 700.0; // Fallback
        }
        
        Double tiempo = googleMapsService.calcularTiempoEstimado(distancia);
        Double costoEstimado = distancia * 150.0; // $150/km promedio
        
        // Guardar ruta en BD
        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .estado("TENTATIVA")
                .cantidadTramos(1)
                .depositosIntermedios(null)
                .distanciaTotal(distancia)
                .costoTotalEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempo)
                .estrategia("DIRECTA")
                .fechaCreacion(LocalDateTime.now())
                .observaciones("Ruta directa sin paradas")
                .build();
        
        Ruta guardada = rutaRepository.save(ruta);
        
        return convertirARutaDTO(guardada);
    }
    
    /**
     * Calcula ruta con un depósito intermedio
     */
    private RutaDTO calcularRutaConUnDeposito(Long solicitudId, String origen, String destino) {
        // Buscar el depósito más conveniente (por ahora, el primero disponible)
        List<Deposito> depositosActivos = depositoRepository.findByEstado("ACTIVO");
        if (depositosActivos.isEmpty()) {
            log.warn("⚠️ No hay depósitos activos para calcular ruta con depósito");
            return null;
        }
        
        Deposito deposito = depositosActivos.get(0); // Simplificado: tomar el primero
        
        Double distancia1 = googleMapsService.calcularDistancia(origen, deposito.getDireccion());
        Double distancia2 = googleMapsService.calcularDistancia(deposito.getDireccion(), destino);
        
        if (distancia1 == null) distancia1 = 350.0;
        if (distancia2 == null) distancia2 = 350.0;
        
        Double distanciaTotal = distancia1 + distancia2;
        Double tiempoTotal = googleMapsService.calcularTiempoEstimado(distanciaTotal) + 4.0; // +4hs por parada
        Double costoEstimado = (distanciaTotal * 150.0) + (deposito.getCostoDia() * 1); // 1 día de estadía
        
        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .estado("TENTATIVA")
                .cantidadTramos(2)
                .depositosIntermedios(deposito.getId().toString())
                .distanciaTotal(distanciaTotal)
                .costoTotalEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoTotal)
                .estrategia("UN_DEPOSITO")
                .fechaCreacion(LocalDateTime.now())
                .observaciones("Ruta con parada en: " + deposito.getNombre())
                .build();
        
        Ruta guardada = rutaRepository.save(ruta);
        
        return convertirARutaDTO(guardada);
    }
    
    /**
     * Calcula ruta con múltiples depósitos
     */
    private RutaDTO calcularRutaConMultiplesDepositos(Long solicitudId, String origen, String destino) {
        List<Deposito> depositosActivos = depositoRepository.findByEstado("ACTIVO");
        if (depositosActivos.size() < 2) {
            log.warn("⚠️ No hay suficientes depósitos para calcular ruta con múltiples paradas");
            return null;
        }
        
        // Simplificado: tomar los primeros 2 depósitos
        Deposito deposito1 = depositosActivos.get(0);
        Deposito deposito2 = depositosActivos.get(1);
        
        Double dist1 = googleMapsService.calcularDistancia(origen, deposito1.getDireccion());
        Double dist2 = googleMapsService.calcularDistancia(deposito1.getDireccion(), deposito2.getDireccion());
        Double dist3 = googleMapsService.calcularDistancia(deposito2.getDireccion(), destino);
        
        if (dist1 == null) dist1 = 350.0;
        if (dist2 == null) dist2 = 300.0;
        if (dist3 == null) dist3 = 350.0;
        
        Double distanciaTotal = dist1 + dist2 + dist3;
        Double tiempoTotal = googleMapsService.calcularTiempoEstimado(distanciaTotal) + 8.0; // +8hs por 2 paradas
        Double costoEstimado = (distanciaTotal * 150.0) + 
                              ((deposito1.getCostoDia() + deposito2.getCostoDia()) * 1);
        
        String depositosIds = deposito1.getId() + "," + deposito2.getId();
        
        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .estado("TENTATIVA")
                .cantidadTramos(3)
                .depositosIntermedios(depositosIds)
                .distanciaTotal(distanciaTotal)
                .costoTotalEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoTotal)
                .estrategia("MULTIPLES_DEPOSITOS")
                .fechaCreacion(LocalDateTime.now())
                .observaciones("Ruta con paradas en: " + deposito1.getNombre() + ", " + deposito2.getNombre())
                .build();
        
        Ruta guardada = rutaRepository.save(ruta);
        
        return convertirARutaDTO(guardada);
    }
    
    /**
     * Asigna una ruta tentativa seleccionada a la solicitud
     */
    public RutaDTO asignarRutaASolicitud(Long rutaId, Long solicitudId) {
        log.info("📌 Asignando ruta {} a solicitud {}", rutaId, solicitudId);
        
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + rutaId));
        
        if (!ruta.getSolicitudId().equals(solicitudId)) {
            throw new IllegalArgumentException("La ruta no pertenece a la solicitud especificada");
        }
        
        if (!"TENTATIVA".equals(ruta.getEstado())) {
            throw new IllegalStateException("Solo se pueden asignar rutas en estado TENTATIVA");
        }
        
        // Marcar todas las demás rutas tentativas de esta solicitud como CANCELADAS
        List<Ruta> otrasRutas = rutaRepository.findRutasTentativas(solicitudId);
        for (Ruta otra : otrasRutas) {
            if (!otra.getId().equals(rutaId)) {
                otra.setEstado("CANCELADA");
                rutaRepository.save(otra);
            }
        }
        
        // Asignar esta ruta
        ruta.setEstado("ASIGNADA");
        ruta.setFechaAsignacion(LocalDateTime.now());
        Ruta asignada = rutaRepository.save(ruta);
        
        log.info("✅ Ruta {} asignada exitosamente. Estrategia: {}", rutaId, ruta.getEstrategia());
        
        // Crear tramos físicos en v2_tramos basados en la ruta asignada
        crearTramosDesdeRuta(asignada);
        
        return convertirARutaDTO(asignada);
    }
    
    /**
     * Crea los registros de Tramo en la BD basándose en la ruta asignada
     */
    private void crearTramosDesdeRuta(Ruta ruta) {
        log.info("🚛 Creando tramos para ruta ID={} (estrategia: {})", ruta.getId(), ruta.getEstrategia());
        
        // Obtener direcciones de la solicitud
        SolicitudDTO solicitud = solicitudClient.obtenerSolicitud(ruta.getSolicitudId());
        
        if ("DIRECTA".equals(ruta.getEstrategia())) {
            crearTramoDirecto(ruta, solicitud);
        } else if ("UN_DEPOSITO".equals(ruta.getEstrategia())) {
            crearTramosConUnDeposito(ruta, solicitud);
        } else if ("MULTIPLES_DEPOSITOS".equals(ruta.getEstrategia())) {
            crearTramosConMultiplesDepositos(ruta, solicitud);
        }
        
        log.info("✅ {} tramos creados para ruta {}", ruta.getCantidadTramos(), ruta.getId());
    }
    
    /**
     * Crea 1 tramo directo (origen → destino)
     */
    private void crearTramoDirecto(Ruta ruta, SolicitudDTO solicitud) {
        Tramo tramo = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("CLIENTE")
                .origenDireccion(solicitud.getOrigenDireccion())
                .destinoTipo("CLIENTE")
                .destinoDireccion(solicitud.getDestinoDireccion())
                .tipoTramo("DIRECTO")
                .distanciaKm(ruta.getDistanciaTotal())
                .ordenTramo(1)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        tramoRepository.save(tramo);
        log.debug("Tramo directo creado: {} → {}, distancia={}km", 
                solicitud.getOrigenDireccion(), solicitud.getDestinoDireccion(), ruta.getDistanciaTotal());
    }
    
    /**
     * Crea 2 tramos (origen → depósito → destino)
     */
    private void crearTramosConUnDeposito(Ruta ruta, SolicitudDTO solicitud) {
        // Parsear ID del depósito
        Long depositoId = Long.parseLong(ruta.getDepositosIntermedios());
        Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado: " + depositoId));
        
        // Tramo 1: Origen → Depósito
        Tramo tramo1 = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("CLIENTE")
                .origenDireccion(solicitud.getOrigenDireccion())
                .destinoTipo("DEPOSITO")
                .destinoId(depositoId)
                .destinoDireccion(deposito.getDireccion())
                .tipoTramo("DEPOSITO")
                .distanciaKm(ruta.getDistanciaTotal() / 2) // Aproximado
                .ordenTramo(1)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        // Tramo 2: Depósito → Destino
        Tramo tramo2 = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("DEPOSITO")
                .origenId(depositoId)
                .origenDireccion(deposito.getDireccion())
                .destinoTipo("CLIENTE")
                .destinoDireccion(solicitud.getDestinoDireccion())
                .tipoTramo("DEPOSITO")
                .distanciaKm(ruta.getDistanciaTotal() / 2) // Aproximado
                .ordenTramo(2)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        tramoRepository.save(tramo1);
        tramoRepository.save(tramo2);
        log.debug("2 tramos creados: {} → {} → {}", 
                solicitud.getOrigenDireccion(), deposito.getNombre(), solicitud.getDestinoDireccion());
    }
    
    /**
     * Crea 3 tramos (origen → depósito1 → depósito2 → destino)
     */
    private void crearTramosConMultiplesDepositos(Ruta ruta, SolicitudDTO solicitud) {
        // Parsear IDs de los depósitos
        String[] depositosIds = ruta.getDepositosIntermedios().split(",");
        Long depositoId1 = Long.parseLong(depositosIds[0]);
        Long depositoId2 = Long.parseLong(depositosIds[1]);
        
        Deposito deposito1 = depositoRepository.findById(depositoId1)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado: " + depositoId1));
        Deposito deposito2 = depositoRepository.findById(depositoId2)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado: " + depositoId2));
        
        // Tramo 1: Origen → Depósito1
        Tramo tramo1 = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("CLIENTE")
                .origenDireccion(solicitud.getOrigenDireccion())
                .destinoTipo("DEPOSITO")
                .destinoId(depositoId1)
                .destinoDireccion(deposito1.getDireccion())
                .tipoTramo("DEPOSITO")
                .distanciaKm(ruta.getDistanciaTotal() / 3) // Aproximado
                .ordenTramo(1)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        // Tramo 2: Depósito1 → Depósito2
        Tramo tramo2 = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("DEPOSITO")
                .origenId(depositoId1)
                .origenDireccion(deposito1.getDireccion())
                .destinoTipo("DEPOSITO")
                .destinoId(depositoId2)
                .destinoDireccion(deposito2.getDireccion())
                .tipoTramo("DEPOSITO")
                .distanciaKm(ruta.getDistanciaTotal() / 3) // Aproximado
                .ordenTramo(2)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        // Tramo 3: Depósito2 → Destino
        Tramo tramo3 = Tramo.builder()
                .solicitudId(ruta.getSolicitudId())
                .rutaId(ruta.getId())
                .origenTipo("DEPOSITO")
                .origenId(depositoId2)
                .origenDireccion(deposito2.getDireccion())
                .destinoTipo("CLIENTE")
                .destinoDireccion(solicitud.getDestinoDireccion())
                .tipoTramo("DEPOSITO")
                .distanciaKm(ruta.getDistanciaTotal() / 3) // Aproximado
                .ordenTramo(3)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        
        tramoRepository.save(tramo1);
        tramoRepository.save(tramo2);
        tramoRepository.save(tramo3);
        log.debug("3 tramos creados: {} → {} → {} → {}", 
                solicitud.getOrigenDireccion(), deposito1.getNombre(), 
                deposito2.getNombre(), solicitud.getDestinoDireccion());
    }
    
    /**
     * Lista todas las rutas de una solicitud
     */
    @Transactional(readOnly = true)
    public List<RutaDTO> listarRutasPorSolicitud(Long solicitudId) {
        return rutaRepository.findBySolicitudId(solicitudId)
                .stream()
                .map(this::convertirARutaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todas las rutas
     */
    @Transactional(readOnly = true)
    public List<RutaDTO> listarTodasRutas() {
        return rutaRepository.findAll()
                .stream()
                .map(this::convertirARutaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte Ruta entity a RutaDTO
     */
    private RutaDTO convertirARutaDTO(Ruta r) {
        List<Long> depositosIds = null;
        if (r.getDepositosIntermedios() != null && !r.getDepositosIntermedios().isEmpty()) {
            depositosIds = Arrays.stream(r.getDepositosIntermedios().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        
        return RutaDTO.builder()
                .id(r.getId())
                .solicitudId(r.getSolicitudId())
                .estado(r.getEstado())
                .cantidadTramos(r.getCantidadTramos())
                .depositosIntermedios(depositosIds)
                .distanciaTotal(r.getDistanciaTotal())
                .costoTotalEstimado(r.getCostoTotalEstimado())
                .costoTotalReal(r.getCostoTotalReal())
                .tiempoEstimadoHoras(r.getTiempoEstimadoHoras())
                .tiempoRealHoras(r.getTiempoRealHoras())
                .estrategia(r.getEstrategia())
                .fechaCreacion(r.getFechaCreacion())
                .fechaAsignacion(r.getFechaAsignacion())
                .fechaCompletada(r.getFechaCompletada())
                .observaciones(r.getObservaciones())
                .build();
    }
}
