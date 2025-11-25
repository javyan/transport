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
        
        // 1. RUTA DIRECTA (siempre se calcula)
        RutaDTO rutaDirecta = calcularRutaDirecta(solicitudId, origenDireccion, destinoDireccion);
        if (rutaDirecta != null) {
            rutasTentativas.add(rutaDirecta);
            log.info("✅ Ruta directa: {}km, ${}, {}hs", 
                    rutaDirecta.getDistanciaTotal(), 
                    rutaDirecta.getCostoTotalEstimado(), 
                    rutaDirecta.getTiempoEstimadoHoras());
        }
        
        // 2. DETERMINAR ESTRATEGIA SEGÚN DISTANCIA
        Double distanciaDirecta = rutaDirecta != null ? rutaDirecta.getDistanciaTotal() : 0;
        
        if (distanciaDirecta > 500 && distanciaDirecta <= 1000) {
            // 500-1000km: 1 DEPÓSITO
            log.info("📍 Distancia {}km → Estrategia: 1 DEPÓSITO", distanciaDirecta);
            RutaDTO rutaConDeposito = calcularRutaConDepositos(solicitudId, origenDireccion, destinoDireccion, 1);
            if (rutaConDeposito != null) {
                rutasTentativas.add(rutaConDeposito);
                log.info("✅ Ruta con 1 depósito: {}km, ${}, {}hs", 
                        rutaConDeposito.getDistanciaTotal(), 
                        rutaConDeposito.getCostoTotalEstimado(), 
                        rutaConDeposito.getTiempoEstimadoHoras());
            }
        } else if (distanciaDirecta > 1000 && distanciaDirecta <= 1500) {
            // 1000-1500km: 2 DEPÓSITOS
            log.info("📍 Distancia {}km → Estrategia: 2 DEPÓSITOS", distanciaDirecta);
            RutaDTO rutaCon2Depositos = calcularRutaConDepositos(solicitudId, origenDireccion, destinoDireccion, 2);
            if (rutaCon2Depositos != null) {
                rutasTentativas.add(rutaCon2Depositos);
                log.info("✅ Ruta con 2 depósitos: {}km, ${}, {}hs", 
                        rutaCon2Depositos.getDistanciaTotal(), 
                        rutaCon2Depositos.getCostoTotalEstimado(), 
                        rutaCon2Depositos.getTiempoEstimadoHoras());
            }
        } else if (distanciaDirecta > 1500) {
            // +1500km: 3 DEPÓSITOS
            log.info("📍 Distancia {}km → Estrategia: 3 DEPÓSITOS", distanciaDirecta);
            RutaDTO rutaCon3Depositos = calcularRutaConDepositos(solicitudId, origenDireccion, destinoDireccion, 3);
            if (rutaCon3Depositos != null) {
                rutasTentativas.add(rutaCon3Depositos);
                log.info("✅ Ruta con 3 depósitos: {}km, ${}, {}hs", 
                        rutaCon3Depositos.getDistanciaTotal(), 
                        rutaCon3Depositos.getCostoTotalEstimado(), 
                        rutaCon3Depositos.getTiempoEstimadoHoras());
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
        log.info("Distancia x: {}", distancia);
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
     * Calcula ruta con N depósitos intermedios.
     * 1. Usa Haversine para SELECCIONAR los mejores depósitos (matemática pura)
     * 2. Calcula distancias REALES con Google Maps para cada tramo de la ruta elegida
     */
    private RutaDTO calcularRutaConDepositos(Long solicitudId, String origen, String destino, int cantidadDepositos) {
        List<Deposito> depositosActivos = depositoRepository.findByEstado("ACTIVO");
        if (depositosActivos.size() < cantidadDepositos) {
            log.warn("⚠️ No hay suficientes depósitos activos ({} requeridos, {} disponibles)", 
                    cantidadDepositos, depositosActivos.size());
            return null;
        }
        
        // Obtener coordenadas de origen y destino
        double[] coordOrigen = googleMapsService.obtenerCoordenadas(origen);
        double[] coordDestino = googleMapsService.obtenerCoordenadas(destino);
        
        if (coordOrigen == null || coordDestino == null) {
            log.warn("⚠️ No se pudieron obtener coordenadas de origen/destino");
            return null;
        }
        
        log.info("🧮 FASE 1: Seleccionando {} depósitos óptimos usando Haversine", cantidadDepositos);
        
        // FASE 1: Seleccionar los N mejores depósitos usando SOLO Haversine
        List<Deposito> depositosSeleccionados = seleccionarDepositosOptimos(
            depositosActivos, coordOrigen, coordDestino, cantidadDepositos
        );
        
        if (depositosSeleccionados == null || depositosSeleccionados.size() != cantidadDepositos) {
            log.warn("⚠️ No se pudieron seleccionar {} depósitos óptimos", cantidadDepositos);
            return null;
        }
        
        log.info("📍 FASE 2: Calculando distancias REALES con Google Maps para cada tramo");
        
        // FASE 2: Calcular distancias REALES con Google Maps para los tramos de la ruta elegida
        List<Double> distanciasTramos = new ArrayList<>();
        List<String> nombresDepositos = new ArrayList<>();
        Double distanciaTotal = 0.0;
        Double costoDepositosTotal = 0.0;
        
        // Tramo 1: Origen → Primer depósito
        String puntoActual = origen;
        for (int i = 0; i < depositosSeleccionados.size(); i++) {
            Deposito deposito = depositosSeleccionados.get(i);
            Double distTramo = googleMapsService.calcularDistancia(puntoActual, deposito.getDireccion());
            if (distTramo == null) distTramo = 200.0; // Fallback
            
            distanciasTramos.add(distTramo);
            distanciaTotal += distTramo;
            costoDepositosTotal += deposito.getCostoDia();
            nombresDepositos.add(deposito.getNombre());
            
            log.info("  📍 Tramo {}: {} → {} = {}km (Google Maps)", 
                    i + 1, 
                    i == 0 ? "Origen" : depositosSeleccionados.get(i-1).getNombre(),
                    deposito.getNombre(),
                    distTramo);
            
            puntoActual = deposito.getDireccion();
        }
        
        // Último tramo: Último depósito → Destino
        Double distUltimoTramo = googleMapsService.calcularDistancia(puntoActual, destino);
        if (distUltimoTramo == null) distUltimoTramo = 200.0;
        
        distanciasTramos.add(distUltimoTramo);
        distanciaTotal += distUltimoTramo;
        
        log.info("  📍 Tramo {}: {} → Destino = {}km (Google Maps)", 
                cantidadDepositos + 1,
                depositosSeleccionados.get(depositosSeleccionados.size() - 1).getNombre(),
                distUltimoTramo);
        
        log.info("✅ Distancia total REAL (por carretera): {}km", distanciaTotal);
        
        // Calcular tiempo y costo
        Double tiempoTotal = googleMapsService.calcularTiempoEstimado(distanciaTotal) + (cantidadDepositos * 4.0);
        Double costoEstimado = (distanciaTotal * 150.0) + costoDepositosTotal;
        
        // Construir lista de IDs de depósitos
        String depositosIds = depositosSeleccionados.stream()
                .map(d -> d.getId().toString())
                .collect(Collectors.joining(","));
        
        // Determinar estrategia
        String estrategia = cantidadDepositos == 1 ? "UN_DEPOSITO" : "MULTIPLES_DEPOSITOS";
        
        Ruta ruta = Ruta.builder()
                .solicitudId(solicitudId)
                .estado("TENTATIVA")
                .cantidadTramos(cantidadDepositos + 1)
                .depositosIntermedios(depositosIds)
                .distanciaTotal(distanciaTotal)
                .costoTotalEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoTotal)
                .estrategia(estrategia)
                .fechaCreacion(LocalDateTime.now())
                .observaciones(String.format("Ruta con %d parada(s): %s", 
                        cantidadDepositos, String.join(", ", nombresDepositos)))
                .build();
        
        Ruta guardada = rutaRepository.save(ruta);
        
        return convertirARutaDTO(guardada);
    }
    
    /**
     * Selecciona los N depósitos óptimos usando SOLO cálculo Haversine (matemática pura).
     * Algoritmo greedy: selecciona el depósito más cercano al siguiente punto objetivo.
     */
    private List<Deposito> seleccionarDepositosOptimos(List<Deposito> depositosDisponibles, 
                                                        double[] coordOrigen, 
                                                        double[] coordDestino, 
                                                        int cantidadDepositos) {
        List<Deposito> depositosSeleccionados = new ArrayList<>();
        List<Deposito> depositosRestantes = new ArrayList<>(depositosDisponibles);
        
        // Filtrar depósitos sin coordenadas
        depositosRestantes.removeIf(d -> d.getLat() == null || d.getLon() == null);
        
        if (depositosRestantes.size() < cantidadDepositos) {
            log.warn("⚠️ No hay suficientes depósitos con coordenadas válidas");
            return null;
        }
        
        double[] puntoActual = coordOrigen;
        
        for (int i = 0; i < cantidadDepositos; i++) {
            Deposito mejorDeposito = null;
            Double menorDistancia = Double.MAX_VALUE;
            
            // Encontrar el depósito más cercano al punto actual
            for (Deposito deposito : depositosRestantes) {
                double distancia = calcularDistanciaHaversine(
                    puntoActual[0], puntoActual[1],
                    deposito.getLat(), deposito.getLon()
                );
                
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    mejorDeposito = deposito;
                }
            }
            
            if (mejorDeposito == null) break;
            
            depositosSeleccionados.add(mejorDeposito);
            depositosRestantes.remove(mejorDeposito);
            puntoActual = new double[]{mejorDeposito.getLat(), mejorDeposito.getLon()};
            
            log.info("  ✅ Depósito {} seleccionado: {} (Haversine: {:.0f}km desde punto anterior)", 
                    i + 1, mejorDeposito.getNombre(), menorDistancia);
        }
        
        return depositosSeleccionados.size() == cantidadDepositos ? depositosSeleccionados : null;
    }
    
    /**
     * Calcula distancia Haversine entre dos puntos geográficos (en km).
     * Fórmula: https://en.wikipedia.org/wiki/Haversine_formula
     */
    private Double calcularDistanciaHaversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RADIO_TIERRA_KM * c;
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
