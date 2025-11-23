package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    Optional<Contenedor> findByNumeroIdentificacion(String numeroIdentificacion);
    
    List<Contenedor> findByClienteId(Long clienteId);
    
    List<Contenedor> findByEstado(String estado);
    
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
}
