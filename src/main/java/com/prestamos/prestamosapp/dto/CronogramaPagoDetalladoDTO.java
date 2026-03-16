package com.prestamos.prestamosapp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CronogramaPagoDetalladoDTO extends CronogramaPagoDTO {

    private int prestamoId;
    private String nombreCliente;
    public CronogramaPagoDetalladoDTO(int id, int prestamoId, String nombreCliente, int numeroCuota, LocalDate fechaVencimiento,
                                      BigDecimal monto, BigDecimal montoPagado, BigDecimal montoPendiente,
                                      String estado, LocalDateTime fechaPagado) {
        this.setId(id);
        this.prestamoId = prestamoId;
        this.nombreCliente = nombreCliente;
        this.setNumeroCuota(numeroCuota);
        this.setFechaVencimiento(fechaVencimiento);
        this.setMonto(monto);
        this.setMontoPagado(montoPagado);
        this.setMontoPendiente(montoPendiente);
        this.setEstado(estado);
        this.setFechaPagado(fechaPagado);
    }
}