package com.tpi.logistica.repository;

import com.tpi.logistica.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    Optional<Camion> findByPatente(String patente);
    List<Camion> findByTransportistaId(Long transportistaId);
    List<Camion> findByEstado(String estado);
    boolean existsByPatente(String patente);
    
    @Query("SELECT c FROM Camion c WHERE c.estado = 'DISPONIBLE' " +
           "AND c.capacidadKg >= :pesoRequerido AND c.capacidadM3 >= :volumenRequerido")
    List<Camion> findDisponiblesConCapacidad(Double pesoRequerido, Double volumenRequerido);
}
