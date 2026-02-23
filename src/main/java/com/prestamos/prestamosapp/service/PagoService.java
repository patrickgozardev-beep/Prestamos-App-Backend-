package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.dto.MetodoPago;
import com.prestamos.prestamosapp.dto.PagoDTO;
import com.prestamos.prestamosapp.model.CronogramaPago;
import com.prestamos.prestamosapp.model.Pago;
import com.prestamos.prestamosapp.model.Prestamo;
import com.prestamos.prestamosapp.repository.CronogramaPagoRepository;
import com.prestamos.prestamosapp.repository.PagoRepository;
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

    public PagoService(PagoRepository pagoRepo, CronogramaPagoRepository cronogramaRepo) {
        this.pagoRepo = pagoRepo;
        this.cronogramaRepo = cronogramaRepo;
    }

    @Transactional
    public Pago registrarPago(PagoDTO dto) {
        // Traer el cronograma seleccionado
        CronogramaPago cronograma = cronogramaRepo.findById(dto.getCronogramaId())
                .orElseThrow(() -> new RuntimeException("Cronograma no encontrado"));

        BigDecimal monto = dto.getMonto();
        MetodoPago metodo = MetodoPago.valueOf(dto.getMetodo().toUpperCase());
        String foto = dto.getFoto();

        //Registrar el pago en la cuota actual
        Pago pago = registrarPagoEnCronograma(cronograma, monto, metodo, foto);

        //Si hay excedente, distribuirlo en los cronogramas futuros
        BigDecimal excedente = calcularExcedente(cronograma, monto);
        if (excedente.compareTo(BigDecimal.ZERO) > 0) {
            distribuirExcedente(cronograma.getPrestamo(), excedente);
        }

        return pago;
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
    public void distribuirExcedente(Prestamo prestamo, BigDecimal excedente) {
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
        } else if (pagado.compareTo(total) >= 0) {
            cronograma.setEstado(EstadoPago.PAGADO);
        } else {
            cronograma.setEstado(EstadoPago.PARCIAL);
        }
    }

    // Calcula si hubo excedente en la cuota actual
    private BigDecimal calcularExcedente(CronogramaPago cronograma, BigDecimal monto) {
        BigDecimal faltante = cronograma.getMonto().subtract(cronograma.getMontoPagado() != null ? cronograma.getMontoPagado() : BigDecimal.ZERO);
        return monto.subtract(faltante).max(BigDecimal.ZERO);
    }
}