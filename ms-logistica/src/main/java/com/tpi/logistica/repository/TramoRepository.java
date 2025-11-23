package com.tpi.logistica.repository;

import com.tpi.logistica.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findBySolicitudId(Long solicitudId);
    List<Tramo> findBySolicitudIdOrderByOrdenTramo(Long solicitudId);
    List<Tramo> findBySolicitudIdAndEstado(Long solicitudId, String estado);
    List<Tramo> findByCamionId(Long camionId);
    List<Tramo> findByEstado(String estado);
    
    @Query("SELECT t FROM Tramo t WHERE t.camionId = :camionId AND t.estado IN ('ASIGNADO', 'INICIADO')")
    List<Tramo> findActivosPorCamion(Long camionId);
    
    @Query("SELECT t FROM Tramo t JOIN Camion c ON t.camionId = c.id " +
           "WHERE c.transportistaId = :transportistaId ORDER BY t.fechaCreacion DESC")
    List<Tramo> findByTransportistaId(Long transportistaId);
}
