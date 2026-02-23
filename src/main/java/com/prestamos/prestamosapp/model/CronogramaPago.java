package com.prestamos.prestamosapp.model;

import com.prestamos.prestamosapp.dto.EstadoPago;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cronograma_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;

    @Column(name = "numero_cuota")
    private int numeroCuota;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "estado", length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoPago estado; // PENDIENTE, PAGADO, ATRASADO

    @Column(name = "fecha_pagado")
    private LocalDateTime fechaPagado;

    @Column(name = "monto_pagado", precision = 12, scale = 2)
    private BigDecimal montoPagado;
}