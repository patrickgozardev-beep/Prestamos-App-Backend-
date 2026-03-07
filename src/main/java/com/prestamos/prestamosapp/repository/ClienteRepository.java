package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByUsuarioId(Integer usuarioId);

    @Query("""
    SELECT c FROM Cliente c
    WHERE c.usuario.id = :usuarioId
    AND (
        LOWER(c.nombres) LIKE LOWER(CONCAT('%', :busqueda, '%'))
        OR c.dni LIKE CONCAT('%', :busqueda, '%')
    )
    """)
    List<Cliente> buscarClientes(
            @Param("usuarioId") Integer usuarioId,
            @Param("busqueda") String busqueda
    );

}
