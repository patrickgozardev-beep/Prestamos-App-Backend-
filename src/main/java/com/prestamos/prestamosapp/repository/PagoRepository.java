package com.prestamos.prestamosapp.repository;

import com.prestamos.prestamosapp.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Buscar pagos por cronograma
    List<Pago> findByCronogramaId(Integer cronogramaId);

    // Buscar pagos por prestamo
    List<Pago> findByPrestamoId(Integer prestamoId);

    // Opcional: buscar pagos por metodo
    List<Pago> findByMetodo(String metodo);

    // Opcional: buscar pagos por monto exacto
    List<Pago> findByMonto(BigDecimal monto);
}