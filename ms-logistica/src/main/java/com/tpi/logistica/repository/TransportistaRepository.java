package com.tpi.logistica.repository;

import com.tpi.logistica.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
    Optional<Transportista> findByDni(String dni);
    Optional<Transportista> findByEmail(String email);
    List<Transportista> findByEstado(String estado);
    boolean existsByDni(String dni);
    boolean existsByEmail(String email);
}
