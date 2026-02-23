package com.prestamos.prestamosapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name="username",unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "rol")
    private String rol;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_creado")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizado")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist(){
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        activo = true;
        rol = "USER";
    }
}
