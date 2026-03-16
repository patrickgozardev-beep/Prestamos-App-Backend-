package com.prestamos.prestamosapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

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
        rol = "admin";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol));
    }

    @Override
    public boolean isEnabled() {
        return this.activo; // Usa tu columna 'activo'
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public String getUsername() { return this.username; }

    @Override
    public String getPassword() { return this.password; }
}
