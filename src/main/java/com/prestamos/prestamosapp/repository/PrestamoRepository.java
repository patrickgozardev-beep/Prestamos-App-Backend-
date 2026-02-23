package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    List<Prestamo> findByClienteId(Integer clienteId);

    List<Prestamo> findByUsuarioId(Integer usuarioId);

    List<Prestamo> findByEstado(String estado);
}
