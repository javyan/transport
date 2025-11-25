package com.tpi.facturacion.repository;

import com.tpi.facturacion.entity.EstadiaDeposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EstadiaDepositoRepository extends JpaRepository<EstadiaDeposito, Long> {
    List<EstadiaDeposito> findByContenedorId(Long contenedorId);
    List<EstadiaDeposito> findByDepositoId(Long depositoId);
    List<EstadiaDeposito> findByEstado(String estado);
    List<EstadiaDeposito> findByContenedorIdAndEstado(Long contenedorId, String estado);
    
    @Query("SELECT e FROM EstadiaDeposito e WHERE e.contenedorId = :contenedorId AND e.estado = 'EN_CURSO'")
    List<EstadiaDeposito> findEstadiasEnCursoPorContenedor(Long contenedorId);
}
