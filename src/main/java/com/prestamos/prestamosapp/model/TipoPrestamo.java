package com.prestamos.prestamosapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_prestamo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="nombre",unique = true)
    private String nombre; // DIARIO o SEMANAL
}
