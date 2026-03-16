package com.prestamos.prestamosapp.dto;

import java.math.BigDecimal;

public record MetricasDashboardDTO(
        BigDecimal capitalVivo,       // Saldo de capital pendiente
        BigDecimal interesesPendientes, // Intereses que faltan cobrar
        BigDecimal montoRecuperado    // Suma real de la tabla Pagos
) {}