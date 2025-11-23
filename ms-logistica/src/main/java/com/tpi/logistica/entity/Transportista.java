package com.tpi.logistica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "v2_transportistas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transportista {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;
    
    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;
    
    @Column(name = "licencia_tipo", nullable = false, length = 50)
    private String licenciaTipo;
    
    @Column(name = "licencia_vencimiento", nullable = false)
    private LocalDate licenciaVencimiento;
    
    @Column(name = "telefono", nullable = false, length = 50)
    private String telefono;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ACTIVO";
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
