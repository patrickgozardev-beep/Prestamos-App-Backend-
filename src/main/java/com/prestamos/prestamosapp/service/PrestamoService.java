package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.dto.EstadoPrestamo;
import com.prestamos.prestamosapp.dto.PrestamoDTO;
import com.prestamos.prestamosapp.model.*;
import com.prestamos.prestamosapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepo;
    private final CronogramaPagoRepository cronogramaRepo;
    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final TipoPrestamoRepository tipoPrestamoRepo;

    public PrestamoService(PrestamoRepository prestamoRepo,
                           CronogramaPagoRepository cronogramaRepo,UsuarioRepository usuarioRepo,
                           ClienteRepository clienteRepo,TipoPrestamoRepository tipoPrestamoRepo) {
        this.prestamoRepo = prestamoRepo;
        this.cronogramaRepo = cronogramaRepo;
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.tipoPrestamoRepo = tipoPrestamoRepo;
    }

    @Transactional
    public Prestamo crearPrestamoDiario(PrestamoDTO dto) {

        //Buscar entidades relacionadas
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        TipoPrestamo tipoPrestamo = tipoPrestamoRepo.findById(dto.getTipoPrestamoId())
                .orElseThrow(() -> new RuntimeException("Tipo de préstamo no encontrado"));
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        //Crear objeto Prestamo y setear campos del DTO
        Prestamo prestamo = Prestamo.builder()
                .cliente(cliente)
                .tipoPrestamo(tipoPrestamo)
                .usuario(usuario)
                .monto(dto.getMonto())
                .interesPorcentaje(dto.getInteresPorcentaje())
                .fechaInicio(dto.getFechaInicio())
                .cantidadCuotas(dto.getCantidadCuotas())
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        BigDecimal total = calcularMontoTotal(prestamo.getMonto(), prestamo.getInteresPorcentaje());
        prestamo.setMontoTotal(total);

        // Guardar préstamo
        Prestamo guardado = prestamoRepo.save(prestamo);

        //Generar cronograma diario
        List<CronogramaPago> cronograma = new ArrayList<>();
        LocalDate fecha = prestamo.getFechaInicio();

        int cuotas = prestamo.getCantidadCuotas();
        BigDecimal cuotaMonto = total.divide(BigDecimal.valueOf(prestamo.getCantidadCuotas()), 2, RoundingMode.HALF_UP);

        int numero = 1;

        while (numero <= cuotas) {

            if (fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {

                CronogramaPago c = CronogramaPago.builder()
                        .prestamo(guardado)
                        .numeroCuota(numero)
                        .fechaPago(fecha)
                        .monto(cuotaMonto)
                        .montoPagado(BigDecimal.ZERO)
                        .estado(EstadoPago.PENDIENTE)
                        .build();

                cronograma.add(c);
                numero++;
            }

            fecha = fecha.plusDays(1);
        }

        cronogramaRepo.saveAll(cronograma);

        guardado.setFechaFin(fecha.minusDays(1)); // última fecha válida

        return guardado;
    }

    @Transactional
    public Prestamo crearPrestamoSemanal(PrestamoDTO dto) {

        // Buscar entidades relacionadas
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        TipoPrestamo tipoPrestamo = tipoPrestamoRepo.findById(dto.getTipoPrestamoId())
                .orElseThrow(() -> new RuntimeException("Tipo de préstamo no encontrado"));
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear objeto Prestamo y setear campos del DTO
        Prestamo prestamo = Prestamo.builder()
                .cliente(cliente)
                .tipoPrestamo(tipoPrestamo)
                .usuario(usuario)
                .monto(dto.getMonto())
                .interesPorcentaje(dto.getInteresPorcentaje())
                .fechaInicio(dto.getFechaInicio())
                .cantidadCuotas(1)
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        // Calcular monto total
        BigDecimal total = calcularMontoTotal(prestamo.getMonto(), prestamo.getInteresPorcentaje());
        prestamo.setMontoTotal(total);

        // Guardar préstamo
        Prestamo guardado = prestamoRepo.save(prestamo);

        // Generar cronograma semanal
        LocalDate fechaPago = prestamo.getFechaInicio().plusDays(7);
        if (fechaPago.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fechaPago = fechaPago.plusDays(1); // saltar domingo
        }

        CronogramaPago c = CronogramaPago.builder()
                .prestamo(guardado)
                .numeroCuota(1)
                .fechaPago(fechaPago)
                .monto(total)
                .montoPagado(BigDecimal.ZERO)
                .estado(EstadoPago.PENDIENTE)
                .build();

        cronogramaRepo.save(c);

        guardado.setFechaFin(fechaPago);

        return guardado;
    }

    @Transactional
    public Prestamo reprogramarPrestamo(Integer prestamoId, Integer nuevasCuotas) {

        Prestamo anterior = prestamoRepo.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Calcular saldo pendiente
        BigDecimal saldoPendiente = calcularSaldoPendiente(anterior);

        if (saldoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No hay saldo pendiente");
        }

        // Inactivar cronogramas pendientes del préstamo anterior
        inactivarCronogramasPendientes(anterior);

        // Marcar préstamo anterior como REPROGRAMADO
        anterior.setEstado(EstadoPrestamo.REPROGRAMADO);
        prestamoRepo.save(anterior);

        // Aplicar 20% interes
        BigDecimal interes = saldoPendiente.multiply(new BigDecimal("0.20"));
        BigDecimal nuevoMontoTotal = saldoPendiente.add(interes);

        // Crear nuevo préstamo
        Prestamo nuevo = new Prestamo();
        nuevo.setCliente(anterior.getCliente());
        nuevo.setPrestamoPadre(anterior);
        nuevo.setMonto(saldoPendiente);
        nuevo.setMontoTotal(nuevoMontoTotal);
        nuevo.setCantidadCuotas(nuevasCuotas);
        nuevo.setFechaInicio(anterior.getFechaFin().plusDays(1));
        nuevo.setEstado(EstadoPrestamo.ACTIVO);

        Prestamo guardado = prestamoRepo.save(nuevo);

        // Crear nuevos cronogramas
        crearCronogramasReprogramados(guardado);

        return guardado;
    }

    private BigDecimal calcularSaldoPendiente(Prestamo prestamo) {

        List<CronogramaPago> cronogramas =
                cronogramaRepo.findByPrestamoId(prestamo.getId());

        return cronogramas.stream()
                .map(c -> c.getMonto().subtract(c.getMontoPagado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void crearCronogramasReprogramados(Prestamo prestamo) {

        BigDecimal cuota = prestamo.getMontoTotal()
                .divide(new BigDecimal(prestamo.getCantidadCuotas()), 2, RoundingMode.HALF_UP);

        LocalDate fecha = prestamo.getFechaInicio();

        for (int i = 1; i <= prestamo.getCantidadCuotas(); i++) {

            fecha = fecha.plusDays(1);

            // Si cae domingo, saltarlo
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fecha = fecha.plusDays(1);
            }

            CronogramaPago c = CronogramaPago.builder()
                    .prestamo(prestamo)
                    .numeroCuota(i)
                    .fechaPago(fecha)
                    .monto(cuota)
                    .montoPagado(BigDecimal.ZERO)
                    .estado(EstadoPago.PENDIENTE)
                    .build();

            cronogramaRepo.save(c);
        }

        prestamo.setFechaFin(fecha);
    }

    private void inactivarCronogramasPendientes(Prestamo prestamo) {

        List<CronogramaPago> cronogramas =
                cronogramaRepo.findByPrestamoId(prestamo.getId());

        for (CronogramaPago c : cronogramas) {

            if (c.getEstado() != EstadoPago.PAGADO) {
                c.setEstado(EstadoPago.INACTIVO);
                cronogramaRepo.save(c);
            }
        }
    }

    private BigDecimal calcularMontoTotal(BigDecimal monto, BigDecimal interesPorcentaje) {
        BigDecimal interesDecimal = interesPorcentaje
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return monto.add(monto.multiply(interesDecimal));
    }

    public List<Prestamo> prestamosPorCliente(Integer clienteId){
        return prestamoRepo.findByClienteId(clienteId);
    }

    public List<Prestamo> prestamosPorUsuario(Integer usuarioId){
        return prestamoRepo.findByUsuarioId(usuarioId);
    }

}
