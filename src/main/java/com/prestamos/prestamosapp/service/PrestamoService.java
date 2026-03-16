package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.dto.EstadoPrestamo;
import com.prestamos.prestamosapp.dto.MetricasDashboardDTO;
import com.prestamos.prestamosapp.dto.PrestamoDTO;
import com.prestamos.prestamosapp.model.*;
import com.prestamos.prestamosapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepo;
    private final CronogramaPagoRepository cronogramaRepo;
    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final TipoPrestamoRepository tipoPrestamoRepo;
    private final PagoRepository pagoRepo;

    public PrestamoService(PrestamoRepository prestamoRepo,
                           CronogramaPagoRepository cronogramaRepo,UsuarioRepository usuarioRepo,
                           ClienteRepository clienteRepo,TipoPrestamoRepository tipoPrestamoRepo,PagoRepository pagoRepo) {
        this.prestamoRepo = prestamoRepo;
        this.cronogramaRepo = cronogramaRepo;
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.tipoPrestamoRepo = tipoPrestamoRepo;
        this.pagoRepo = pagoRepo;
    }

    @Transactional
    public Prestamo crearPrestamoDiario(PrestamoDTO dto) {

        //Buscar entidades relacionadas
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        TipoPrestamo tipoPrestamo = tipoPrestamoRepo.findById(dto.getTipoPrestamoId())
                .orElseThrow(() -> new RuntimeException("Tipo de préstamo no encontrado"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));

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
        LocalDate fecha = guardado.getFechaInicio().plusDays(1);

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

        // 1. Buscar entidades relacionadas
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        TipoPrestamo tipoPrestamo = tipoPrestamoRepo.findById(dto.getTipoPrestamoId())
                .orElseThrow(() -> new RuntimeException("Tipo de préstamo no encontrado"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));

        // 2. Crear objeto Prestamo (usamos dto.getCantidadCuotas())
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

        // 3. Calcular montos
        BigDecimal total = calcularMontoTotal(prestamo.getMonto(), prestamo.getInteresPorcentaje());
        prestamo.setMontoTotal(total);

        BigDecimal montoPorCuota = total.divide(
                BigDecimal.valueOf(prestamo.getCantidadCuotas()), 2, RoundingMode.HALF_UP);

        // 4. Guardar préstamo inicial
        Prestamo guardado = prestamoRepo.save(prestamo);

        // 5. Generar cronograma semanal
        List<CronogramaPago> cronograma = new ArrayList<>();

        // Empezamos a contar 7 días después de la fecha de inicio
        LocalDate fechaCorriente = prestamo.getFechaInicio();

        for (int i = 1; i <= guardado.getCantidadCuotas(); i++) {
            // Sumamos una semana (7 días) para la siguiente cuota
            fechaCorriente = fechaCorriente.plusWeeks(1);

            // Si cae domingo, se pasa al lunes
            if (fechaCorriente.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fechaCorriente = fechaCorriente.plusDays(1);
            }

            CronogramaPago c = CronogramaPago.builder()
                    .prestamo(guardado)
                    .numeroCuota(i)
                    .fechaPago(fechaCorriente)
                    .monto(montoPorCuota)
                    .montoPagado(BigDecimal.ZERO)
                    .estado(EstadoPago.PENDIENTE)
                    .build();

            cronograma.add(c);
        }

        cronogramaRepo.saveAll(cronograma);

        // 6. Actualizar fecha de fin con la última cuota generada
        guardado.setFechaFin(fechaCorriente);

        return guardado;
    }

    @Transactional
    public Prestamo reprogramarPrestamo(Integer prestamoId, Integer nuevasCuotas, BigDecimal interesIngresado) {

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
        BigDecimal montoInteres = saldoPendiente.multiply(interesIngresado.divide(new BigDecimal("100")));
        BigDecimal nuevoMontoTotal = saldoPendiente.add(montoInteres);

        // Crear nuevo préstamo
        Prestamo nuevo = new Prestamo();
        nuevo.setCliente(anterior.getCliente());
        nuevo.setUsuario(anterior.getUsuario());
        nuevo.setTipoPrestamo(anterior.getTipoPrestamo());
        nuevo.setPrestamoPadre(anterior);
        nuevo.setMonto(saldoPendiente);
        nuevo.setInteresPorcentaje(interesIngresado);
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
        return prestamoRepo.findByClienteIdCustomOrder(clienteId);
    }

    public List<Prestamo> prestamosPorUsuario(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));

        return prestamoRepo.findByUsuarioIdCustomOrder(usuario.getId());
    }

    public Optional<Prestamo> prestamosPorId (Integer prestamoId){
        return prestamoRepo.findById(prestamoId);
    }

    public MetricasDashboardDTO obtenerMetricasDashboard() {
        MetricasDashboardDTO metricas = prestamoRepo.obtenerResumenMetricas();

        return metricas;
    }

    @Transactional
    public void eliminarPrestamo(Integer id) {
        // 1. Verificar existencia
        Prestamo prestamo = prestamoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("El préstamo no existe"));

        // 2. Obtener las cuotas del cronograma
        List<CronogramaPago> cronogramas = cronogramaRepo.findByPrestamoId(id);

        // 3. BORRAR PAGOS ASOCIADOS (Si existe la entidad Pago)
        // Debemos borrar los pagos antes que las cuotas por la llave foránea
        for (CronogramaPago cuota : cronogramas) {
            // Asumiendo que existe una relación o un repositorio para los cobros realizados
            List<Pago> pagosDeCuota = pagoRepo.findByCronogramaId(cuota.getId());
            pagoRepo.deleteAll(pagosDeCuota);
        }

        // 4. Borrar cronogramas
        cronogramaRepo.deleteAll(cronogramas);

        // 5. Borrar el préstamo
        prestamoRepo.delete(prestamo);
    }

    public String generarLinkWhatsApp(Prestamo prestamo) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Construcción del mensaje
        sb.append("Hola ").append(prestamo.getCliente().getNombres()).append(",\n");
        sb.append("Se generó el préstamo de S/ ").append(String.format("%.2f", prestamo.getMontoTotal())).append("\n");
        sb.append("Deberá pagar en las siguientes fechas:\n\n");

        for (CronogramaPago cronograma : prestamo.getCronogramas()) {
            sb.append("Cuota #").append(cronograma.getNumeroCuota()).append(": ")
                    .append(cronograma.getFechaPago().format(formatter))
                    .append(" // S/ ").append(String.format("%.2f", cronograma.getMonto()))
                    .append("\n");
        }

        String mensajeFull = sb.toString();

        // Codificar el mensaje para URL
        String mensajeCodificado = URLEncoder.encode(mensajeFull, StandardCharsets.UTF_8)
                .replace("+", "%20"); // Wa.me prefiere %20 sobre +

        // Limpiar el teléfono (debe ser solo números con código de país)
        // Ejemplo: 51999888777 (sin el +)
        String telefono = prestamo.getCliente().getTelefono().replaceAll("[^0-9]", "");
        if (!telefono.startsWith("51")) telefono = "51" + telefono;

        return "https://wa.me/" + telefono + "?text=" + mensajeCodificado;
    }

}
