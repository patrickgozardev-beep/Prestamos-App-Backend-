package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByUsuarioId(Integer usuarioId);

    List<Cliente> findByNombreCompletoContaining(String nombre);

    List<Cliente> findByDni(String dni);
}
