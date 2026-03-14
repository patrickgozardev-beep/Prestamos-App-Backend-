package com.prestamos.prestamosapp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data

public class CronogramaPagoDTO {
    private int id;
    private int numeroCuota;
    private BigDecimal monto;
    private BigDecimal montoPagado;
    private BigDecimal montoPendiente;
    private LocalDate fechaVencimiento;
    private String estado;
    private LocalDateTime fechaPagado;
}

