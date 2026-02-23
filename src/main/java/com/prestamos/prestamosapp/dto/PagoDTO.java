package com.prestamos.prestamosapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoDTO {
    private Integer cronogramaId;      // Identifica a qué cuota se paga
    private BigDecimal monto;          // Monto del pago
    private String metodo;             // Puede ser efectivo, yape, etc.
    private String foto;               // URL o path de la foto del comprobante


}