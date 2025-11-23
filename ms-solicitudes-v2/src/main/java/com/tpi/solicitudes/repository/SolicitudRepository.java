package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
    List<Solicitud> findByClienteId(Long clienteId);
    
    List<Solicitud> findByEstado(String estado);
    
    List<Solicitud> findByContenedorId(Long contenedorId);
    
    @Query("SELECT s FROM Solicitud s WHERE s.estado IN ('PROGRAMADA', 'EN_TRANSITO') ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findPendientes();
}
