package com.tpi.tracking.repository;

import com.tpi.tracking.entity.TrackingEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingEventoRepository extends JpaRepository<TrackingEvento, Long> {
    
    /**
     * Encuentra todos los eventos de un contenedor específico ordenados por fecha descendente
     */
    List<TrackingEvento> findByContenedorIdOrderByFechaHoraDesc(Long contenedorId);
    
    /**
     * Encuentra todos los eventos de una solicitud específica ordenados por fecha descendente
     */
    List<TrackingEvento> findBySolicitudIdOrderByFechaHoraDesc(Long solicitudId);
    
    /**
     * Encuentra todos los eventos de un tramo específico ordenados por fecha descendente
     */
    List<TrackingEvento> findByTramoIdOrderByFechaHoraDesc(Long tramoId);
    
    /**
     * Encuentra eventos por tipo
     */
    List<TrackingEvento> findByTipoEventoOrderByFechaHoraDesc(String tipoEvento);
    
    /**
     * Obtiene los últimos N eventos de un contenedor
     */
    @Query("SELECT e FROM TrackingEvento e WHERE e.contenedorId = :contenedorId " +
           "ORDER BY e.fechaHora DESC")
    List<TrackingEvento> findTopEventosByContenedor(Long contenedorId);
}
