package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.dto.MetricasDashboardDTO;
import com.prestamos.prestamosapp.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    List<Prestamo> findByClienteId(Integer clienteId);

    List<Prestamo> findByUsuarioId(Integer usuarioId);

    List<Prestamo> findByEstado(String estado);

    @Query("SELECT new com.prestamos.prestamosapp.dto.MetricasDashboardDTO(" +
            "COALESCE(SUM(p.montoTotal), 0) - COALESCE((SELECT SUM(c.montoPagado) FROM CronogramaPago c WHERE c.prestamo.estado = 'ACTIVO'), 0), " +
            "COALESCE(SUM(p.montoTotal - p.monto), 0), " +
            "COALESCE((SELECT SUM(pa.monto) FROM Pago pa), 0)) " +
            "FROM Prestamo p WHERE p.estado = 'ACTIVO'")
    MetricasDashboardDTO obtenerResumenMetricas();
}
