package com.tpi.facturacion.repository;

import com.tpi.facturacion.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    
    @Query("SELECT t FROM Tarifa t WHERE t.estado = 'ACTIVA' " +
           "AND t.fechaVigenciaDesde <= :fecha " +
           "AND (t.fechaVigenciaHasta IS NULL OR t.fechaVigenciaHasta >= :fecha) " +
           "ORDER BY t.fechaVigenciaDesde DESC")
    List<Tarifa> findTarifasVigentes(LocalDate fecha);
    
    default Optional<Tarifa> findTarifaVigente(LocalDate fecha) {
        List<Tarifa> tarifas = findTarifasVigentes(fecha);
        return tarifas.isEmpty() ? Optional.empty() : Optional.of(tarifas.get(0));
    }
}
