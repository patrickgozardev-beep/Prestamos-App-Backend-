package com.prestamos.prestamosapp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClienteDTO {
    private Integer id;
    private String nombres;
    private String dni;
    private String telefono;
    private String googleMapsLink;
    private String dniPdf;

}
