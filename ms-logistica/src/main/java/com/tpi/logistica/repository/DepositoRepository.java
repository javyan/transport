package com.tpi.logistica.repository;

import com.tpi.logistica.entity.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    List<Deposito> findByEstado(String estado);
}
