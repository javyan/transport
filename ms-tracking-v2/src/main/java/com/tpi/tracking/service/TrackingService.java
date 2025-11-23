package com.tpi.tracking.service;

import com.tpi.tracking.dto.RegistrarEventoRequest;
import com.tpi.tracking.dto.TrackingEventoDTO;
import com.tpi.tracking.entity.TrackingEvento;
import com.tpi.tracking.repository.TrackingEventoRepository;
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
public class TrackingService {
    
    private final TrackingEventoRepository eventoRepository;
    
    /**
     * Registra un nuevo evento de tracking
     */
    public TrackingEventoDTO registrarEvento(RegistrarEventoRequest request) {
        log.info("Registrando evento de tipo: {} para contenedor={}, solicitud={}, tramo={}", 
                request.getTipoEvento(), request.getContenedorId(), 
                request.getSolicitudId(), request.getTramoId());
        
        TrackingEvento evento = TrackingEvento.builder()
                .contenedorId(request.getContenedorId())
                .solicitudId(request.getSolicitudId())
                .tramoId(request.getTramoId())
                .tipoEvento(request.getTipoEvento())
                .descripcion(request.getDescripcion())
                .fechaHora(request.getFechaHora() != null ? request.getFechaHora() : LocalDateTime.now())
                .lat(request.getLat())
                .lon(request.getLon())
                .observaciones(request.getObservaciones())
                .fechaCreacion(LocalDateTime.now())
                .build();
        
        TrackingEvento guardado = eventoRepository.save(evento);
        log.info("Evento registrado con ID: {}", guardado.getId());
        
        return convertirADTO(guardado);
    }
    
    /**
     * Lista todos los eventos de un contenedor
     */
    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listarEventosPorContenedor(Long contenedorId) {
        log.info("Listando eventos para contenedor ID: {}", contenedorId);
        return eventoRepository.findByContenedorIdOrderByFechaHoraDesc(contenedorId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los eventos de una solicitud
     */
    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listarEventosPorSolicitud(Long solicitudId) {
        log.info("Listando eventos para solicitud ID: {}", solicitudId);
        return eventoRepository.findBySolicitudIdOrderByFechaHoraDesc(solicitudId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los eventos de un tramo
     */
    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listarEventosPorTramo(Long tramoId) {
        log.info("Listando eventos para tramo ID: {}", tramoId);
        return eventoRepository.findByTramoIdOrderByFechaHoraDesc(tramoId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos los eventos por tipo
     */
    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listarEventosPorTipo(String tipoEvento) {
        log.info("Listando eventos de tipo: {}", tipoEvento);
        return eventoRepository.findByTipoEventoOrderByFechaHoraDesc(tipoEvento)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un evento por ID
     */
    @Transactional(readOnly = true)
    public TrackingEventoDTO obtenerEvento(Long id) {
        TrackingEvento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        return convertirADTO(evento);
    }
    
    /**
     * Lista todos los eventos
     */
    @Transactional(readOnly = true)
    public List<TrackingEventoDTO> listarTodosEventos() {
        return eventoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // Método auxiliar
    private TrackingEventoDTO convertirADTO(TrackingEvento e) {
        return TrackingEventoDTO.builder()
                .id(e.getId())
                .contenedorId(e.getContenedorId())
                .solicitudId(e.getSolicitudId())
                .tramoId(e.getTramoId())
                .tipoEvento(e.getTipoEvento())
                .descripcion(e.getDescripcion())
                .fechaHora(e.getFechaHora())
                .lat(e.getLat())
                .lon(e.getLon())
                .observaciones(e.getObservaciones())
                .build();
    }
}
