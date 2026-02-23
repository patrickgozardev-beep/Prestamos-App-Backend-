package com.prestamos.prestamosapp.model;

import com.prestamos.prestamosapp.dto.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "tipo_prestamo_id", nullable = false)
    private TipoPrestamo tipoPrestamo;

    @Column(name="monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "interes_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal interesPorcentaje;

    @Column(name = "monto_total", precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name="fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name="fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "estado", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPrestamo estado;

    @Column(name = "fecha_creado")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizado")
    private LocalDateTime fechaActualizacion;

    @Column(name="cantidad_cuotas")
    private int cantidadCuotas;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "prestamo_padre_id")
    private Prestamo prestamoPadre;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}