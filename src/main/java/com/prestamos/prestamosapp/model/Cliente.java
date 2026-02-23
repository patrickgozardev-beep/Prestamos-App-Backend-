package com.prestamos.prestamosapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombres")
    private String nombres;

    @Column(name="dni", unique = true, length = 8)
    private String dni;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "google_maps_link")
    private String googleMapsLink;

    @Column(name = "dni_pdf")
    private String dniPdf;

    @Column(name = "fecha_creado")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizado")
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

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
