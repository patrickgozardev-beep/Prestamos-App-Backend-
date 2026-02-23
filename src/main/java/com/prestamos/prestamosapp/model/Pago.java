package com.prestamos.prestamosapp.model;

import com.prestamos.prestamosapp.dto.EstadoPrestamo;
import com.prestamos.prestamosapp.dto.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "cronograma_id")
    private CronogramaPago cronograma;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "metodo", length = 50)
    @Enumerated(EnumType.STRING)
    private MetodoPago metodo;

    @Column(name = "foto_pago", length = 255)
    private String fotoPago;


}