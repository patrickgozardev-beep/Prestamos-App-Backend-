package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.dto.CronogramaPagoDetalladoDTO;
import com.prestamos.prestamosapp.model.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Integer> {

    List<CronogramaPago> findByPrestamoId(Integer prestamoId);
    List<CronogramaPago> findByFechaPago(LocalDate fechaPago);
    List<CronogramaPago> findByPrestamoIdAndMontoPagadoLessThanOrderByFechaPagoAsc(Integer prestamoId, BigDecimal monto);
    @Query("SELECT new com.prestamos.prestamosapp.dto.CronogramaPagoDetalladoDTO(" +
            "c.id, " +
            "c.prestamo.id, " +
            "c.prestamo.cliente.nombres as nombreCliente, " +
            "c.numeroCuota, " +
            "c.fechaPago, " +
            "c.monto, " +
            "c.montoPagado, " +
            "(c.monto - c.montoPagado), " +
            "CAST(c.estado as string), " +
            "c.fechaPagado) " +
            "FROM CronogramaPago c " +
            "WHERE c.fechaPago BETWEEN :inicio AND :fin " +
            "AND c.estado IN (com.prestamos.prestamosapp.dto.EstadoPago.PENDIENTE, " +
            "com.prestamos.prestamosapp.dto.EstadoPago.PARCIAL) " +
            "ORDER BY c.fechaPago ASC")
    List<CronogramaPagoDetalladoDTO> findProximosCobros(LocalDate inicio, LocalDate fin);
}
