package com.prestamos.prestamosapp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PrestamoDTO {
    private Integer clienteId;
    private Integer tipoPrestamoId;
    private BigDecimal monto;
    private BigDecimal interesPorcentaje;
    private LocalDate fechaInicio;
    private Integer cantidadCuotas;
    private Integer usuarioId;
}