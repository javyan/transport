package com.tpi.facturacion.repository;

import com.tpi.facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findBySolicitudId(Long solicitudId);
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    List<Factura> findByEstado(String estado);
    boolean existsBySolicitudId(Long solicitudId);
}
