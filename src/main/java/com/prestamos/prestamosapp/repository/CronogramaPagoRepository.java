package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Integer> {

    List<CronogramaPago> findByPrestamoId(Integer prestamoId);
    List<CronogramaPago> findByFechaPago(LocalDate fechaPago);
    List<CronogramaPago> findByPagadoFalse();
    List<CronogramaPago> findByPrestamoIdAndMontoPagadoLessThanOrderByFechaPagoAsc(Integer prestamoId, BigDecimal monto);

}
