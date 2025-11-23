package com.tpi.logistica.repository;

import com.tpi.logistica.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    /**
     * Busca todas las rutas asociadas a una solicitud
     * @param solicitudId ID de la solicitud
     * @return Lista de rutas (tentativas, asignada, etc.)
     */
    List<Ruta> findBySolicitudId(Long solicitudId);
    
    /**
     * Busca la ruta asignada a una solicitud
     * @param solicitudId ID de la solicitud
     * @return Ruta asignada si existe
     */
    @Query("SELECT r FROM Ruta r WHERE r.solicitudId = ?1 AND r.estado = 'ASIGNADA'")
    Optional<Ruta> findRutaAsignada(Long solicitudId);
    
    /**
     * Busca rutas tentativas de una solicitud
     * @param solicitudId ID de la solicitud
     * @return Lista de rutas tentativas
     */
    @Query("SELECT r FROM Ruta r WHERE r.solicitudId = ?1 AND r.estado = 'TENTATIVA'")
    List<Ruta> findRutasTentativas(Long solicitudId);
    
    /**
     * Busca rutas por estado
     * @param estado Estado de la ruta
     * @return Lista de rutas con ese estado
     */
    List<Ruta> findByEstado(String estado);
}
