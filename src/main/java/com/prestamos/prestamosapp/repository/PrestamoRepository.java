package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.dto.MetricasDashboardDTO;
import com.prestamos.prestamosapp.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    List<Prestamo> findByClienteId(Integer clienteId);

    List<Prestamo> findByUsuarioId(Integer usuarioId);

    List<Prestamo> findByEstado(String estado);

    @Query("SELECT p FROM Prestamo p WHERE p.cliente.id = :clienteId " +
            "ORDER BY CASE p.estado " +
            "  WHEN 'ACTIVO' THEN 1 " +
            "  WHEN 'REPROGRAMADO' THEN 2 " +
            "  WHEN 'PAGADO' THEN 3 " +
            "  ELSE 4 END, p.id DESC") // Usamos p.id o p.fechaCreacion para desempatar
    List<Prestamo> findByClienteIdCustomOrder(@Param("clienteId") Integer clienteId);

    @Query("SELECT p FROM Prestamo p WHERE p.usuario.id = :usuarioId " +
            "ORDER BY CASE p.estado " +
            "  WHEN 'ACTIVO' THEN 1 " +
            "  WHEN 'REPROGRAMADO' THEN 2 " +
            "  WHEN 'PAGADO' THEN 3 " +
            "  ELSE 4 END, p.id DESC")
    List<Prestamo> findByUsuarioIdCustomOrder(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT new com.prestamos.prestamosapp.dto.MetricasDashboardDTO(" +
            "COALESCE(SUM(p.montoTotal), 0) - COALESCE((SELECT SUM(c.montoPagado) FROM CronogramaPago c WHERE c.prestamo.estado = 'ACTIVO'), 0), " +
            "COALESCE(SUM(p.montoTotal - p.monto), 0), " +
            "COALESCE((SELECT SUM(pa.monto) FROM Pago pa), 0)) " +
            "FROM Prestamo p WHERE p.estado = 'ACTIVO'")
    MetricasDashboardDTO obtenerResumenMetricas();
}
