package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.dto.EstadoPrestamo;
import com.prestamos.prestamosapp.dto.MetodoPago;
import com.prestamos.prestamosapp.dto.PagoDTO;
import com.prestamos.prestamosapp.model.CronogramaPago;
import com.prestamos.prestamosapp.model.Pago;
import com.prestamos.prestamosapp.model.Prestamo;
import com.prestamos.prestamosapp.repository.CronogramaPagoRepository;
import com.prestamos.prestamosapp.repository.PagoRepository;
import com.prestamos.prestamosapp.repository.PrestamoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final CronogramaPagoRepository cronogramaRepo;
    private final PrestamoRepository prestamoRepo;

    public PagoService(PagoRepository pagoRepo, CronogramaPagoRepository cronogramaRepo, PrestamoRepository prestamoRepo) {
        this.pagoRepo = pagoRepo;
        this.cronogramaRepo = cronogramaRepo;
        this.prestamoRepo = prestamoRepo;
    }

    @Transactional
    public Pago registrarPago(PagoDTO dto) {
        // Traer el cronograma seleccionado
        CronogramaPago cronograma = cronogramaRepo.findById(dto.getCronogramaId())
                .orElseThrow(() -> new RuntimeException("Cronograma no encontrado"));

        Prestamo prestamo = cronograma.getPrestamo();

        BigDecimal saldoPendienteTotal = calcularSaldoPendienteTotal(prestamo.getId());
        if (dto.getMonto().compareTo(saldoPendienteTotal) > 0) {
            throw new RuntimeException("El monto ingresado (S/ " + dto.getMonto() +
                    ") excede el saldo total pendiente del préstamo (S/ " + saldoPendienteTotal + ")");
        }

        BigDecimal montoRecibido = dto.getMonto();
        MetodoPago metodo = MetodoPago.valueOf(dto.getMetodo().toUpperCase());
        String foto = dto.getFoto();

        BigDecimal pagadoHastaAhora = cronograma.getMontoPagado() != null ? cronograma.getMontoPagado() : BigDecimal.ZERO;
        BigDecimal faltanteCuotaActual = cronograma.getMonto().subtract(pagadoHastaAhora);

        // 2. Determinamos cuánto va para esta cuota y cuánto sobra
        BigDecimal montoParaEstaCuota = montoRecibido.min(faltanteCuotaActual);
        BigDecimal excedente = montoRecibido.subtract(montoParaEstaCuota).max(BigDecimal.ZERO);

        //Registrar el pago en la cuota actual
        Pago pagoPrincipal = registrarPagoEnCronograma(cronograma, montoParaEstaCuota, metodo, foto);
        //Si hay excedente, distribuirlo en los cronogramas futuros

        if (excedente.compareTo(BigDecimal.ZERO) > 0) {
            distribuirExcedente(cronograma.getPrestamo(), excedente, metodo);
        }
        verificarYFinalizarPrestamo(cronograma.getPrestamo());
        return pagoPrincipal;
    }

    // Registrar pago en un solo cronograma
    @Transactional
    public Pago registrarPagoEnCronograma(CronogramaPago cronograma, BigDecimal monto, MetodoPago metodo, String foto) {
        Pago pago = Pago.builder()
                .cronograma(cronograma)
                .monto(monto)
                .metodo(metodo)
                .fotoPago(foto)
                .fechaPago(LocalDateTime.now())
                .build();

        pagoRepo.save(pago);

        // Actualizar monto pagado del cronograma
        BigDecimal nuevoMontoPagado = cronograma.getMontoPagado() == null
                ? monto
                : cronograma.getMontoPagado().add(monto);

        cronograma.setMontoPagado(nuevoMontoPagado);
        actualizarEstadoCronograma(cronograma);
        cronogramaRepo.save(cronograma);

        return pago;
    }

    // Distribuir excedente a los siguientes cronogramas pendientes
    @Transactional
    public void distribuirExcedente(Prestamo prestamo, BigDecimal excedente, MetodoPago metodo) {
        List<CronogramaPago> futuros = cronogramaRepo
                .findByPrestamoIdAndMontoPagadoLessThanOrderByFechaPagoAsc(prestamo.getId(), prestamo.getMonto());

        for (CronogramaPago c : futuros) {
            if (excedente.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal faltante = c.getMonto().subtract(c.getMontoPagado());
            if (faltante.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal pagoParaEstaCuota = excedente.min(faltante);

            // Crear pago parcial o completo para esta cuota
            Pago pago = Pago.builder()
                    .cronograma(c)
                    .monto(pagoParaEstaCuota)
                    .metodo(metodo)
                    .fechaPago(LocalDateTime.now())
                    .build();
            pagoRepo.save(pago);

            // Actualizar cronograma
            c.setMontoPagado(c.getMontoPagado().add(pagoParaEstaCuota));
            actualizarEstadoCronograma(c);
            cronogramaRepo.save(c);

            excedente = excedente.subtract(pagoParaEstaCuota);
        }
    }

    // Actualiza el estado del cronograma según monto pagado
    private void actualizarEstadoCronograma(CronogramaPago cronograma) {
        BigDecimal pagado = cronograma.getMontoPagado();
        BigDecimal total = cronograma.getMonto();

        if (pagado.compareTo(BigDecimal.ZERO) == 0) {
            cronograma.setEstado(EstadoPago.PENDIENTE);
            cronograma.setFechaPagado(null);
        } else if (pagado.compareTo(total) >= 0) {
            cronograma.setEstado(EstadoPago.PAGADO);
            cronograma.setFechaPagado(LocalDateTime.now());
        } else {
            cronograma.setEstado(EstadoPago.PARCIAL);
            cronograma.setFechaPagado(null);
        }
    }

    @Transactional
    public void eliminarPago(Integer pagoId) {
        Pago pago = pagoRepo.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("El pago no existe"));

        CronogramaPago cronograma = pago.getCronograma();
        Prestamo prestamo = cronograma.getPrestamo(); // Obtenemos el préstamo

        // 2. Revertimos el monto pagado del cronograma
        BigDecimal nuevoMontoPagado = cronograma.getMontoPagado().subtract(pago.getMonto());
        cronograma.setMontoPagado(nuevoMontoPagado.max(BigDecimal.ZERO));

        // 3. Actualizamos el estado de la cuota (PAGADO -> PARCIAL/PENDIENTE)
        actualizarEstadoCronograma(cronograma);
        cronogramaRepo.save(cronograma);

        // 4. Borramos el pago
        pagoRepo.delete(pago);

        // 5. CRÍTICO: Recalcular el estado del préstamo
        actualizarEstadoPrestamo(prestamo);

        System.out.println("Pago eliminado. El préstamo ahora está: " + prestamo.getEstado());
    }

    private void actualizarEstadoPrestamo(Prestamo prestamo) {
        // Buscamos todas las cuotas del préstamo
        List<CronogramaPago> cuotas = cronogramaRepo.findByPrestamoId(prestamo.getId());

        // Verificamos si hay alguna cuota que no esté totalmente pagada
        boolean tienePendientes = cuotas.stream()
                .anyMatch(c -> c.getEstado() != EstadoPago.PAGADO);

        if (tienePendientes) {
            // Si hay pendientes y el préstamo estaba como PAGADO, lo reactivamos
            if (prestamo.getEstado() == EstadoPrestamo.PAGADO) {
                prestamo.setEstado(EstadoPrestamo.ACTIVO);
                prestamoRepo.save(prestamo);
            }
        } else {
            // Si no hay pendientes, lo marcamos como PAGADO
            prestamo.setEstado(EstadoPrestamo.PAGADO);
            prestamoRepo.save(prestamo);
        }
    }

    private void verificarYFinalizarPrestamo(Prestamo prestamo) {
        // Buscamos si existe alguna cuota que NO esté pagada
        boolean tieneCuotasPendientes = cronogramaRepo.findByPrestamoId(prestamo.getId())
                .stream()
                .anyMatch(c -> c.getEstado() != EstadoPago.PAGADO);

        // Si no tiene ninguna pendiente, el préstamo se marca como PAGADO
        if (!tieneCuotasPendientes) {
            prestamo.setEstado(EstadoPrestamo.PAGADO);
            // Nota: Asegúrate de tener el repositorio de préstamos inyectado o usa el del cronograma
            prestamoRepo.save(prestamo);
            System.out.println("¡Préstamo " + prestamo.getId() + " finalizado por pago completo!");
        }
    }

    public Pago obtenerPorId(Integer id) {
        return pagoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    public BigDecimal calcularSaldoPendienteTotal(Integer prestamoId) {
        List<CronogramaPago> cuotas = cronogramaRepo.findByPrestamoId(prestamoId);
        return cuotas.stream()
                .map(c -> c.getMonto().subtract(c.getMontoPagado() != null ? c.getMontoPagado() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}