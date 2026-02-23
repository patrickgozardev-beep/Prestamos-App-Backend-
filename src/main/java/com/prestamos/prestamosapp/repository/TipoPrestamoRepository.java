package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.model.TipoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPrestamoRepository extends JpaRepository<TipoPrestamo, Integer> {


}
