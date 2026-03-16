package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.CronogramaPagoDTO;
import com.prestamos.prestamosapp.dto.CronogramaPagoDetalladoDTO;
import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.model.CronogramaPago;
import com.prestamos.prestamosapp.model.Usuario;
import com.prestamos.prestamosapp.repository.CronogramaPagoRepository;
import com.prestamos.prestamosapp.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class CronogramaPagoService {

    private final CronogramaPagoRepository cronogramaRepo;
    private final UsuarioRepository usuarioRepo;

    public CronogramaPagoService(CronogramaPagoRepository cronogramaRepo,UsuarioRepository usuarioRepo) {
        this.cronogramaRepo = cronogramaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<CronogramaPagoDTO> obtenerPorPrestamo(Integer prestamoId) {
        List<CronogramaPago> cuotas = cronogramaRepo.findByPrestamoId(prestamoId);
        LocalDate hoy = LocalDate.now();
        boolean huboCambios = false;

        for (CronogramaPago cuota : cuotas) {
            // Lógica de Mora
            if (cuota.getEstado() != EstadoPago.PAGADO &&
                    cuota.getFechaPago().isBefore(hoy) &&
                    cuota.getEstado() != EstadoPago.ATRASADO) {

                cuota.setEstado(EstadoPago.ATRASADO);
                huboCambios = true;
            }
        }

        if (huboCambios) {
            cronogramaRepo.saveAll(cuotas);
        }

        // Convertimos la lista de entidades a lista de DTOs
        return cuotas.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public CronogramaPagoDTO marcarComoPagado(Integer id) {
        CronogramaPago cuota = cronogramaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        cuota.setEstado(EstadoPago.PAGADO);
        cuota.setFechaPagado(LocalDateTime.now());
        cuota.setMontoPagado(cuota.getMonto()); // Asumimos pago total aquí

        return convertirADTO(cronogramaRepo.save(cuota));
    }

    // Método helper para la conversión
    private CronogramaPagoDTO convertirADTO(CronogramaPago entidad) {
        CronogramaPagoDTO dto = new CronogramaPagoDTO();
        dto.setId(entidad.getId());
        dto.setNumeroCuota(entidad.getNumeroCuota());
        dto.setFechaVencimiento(entidad.getFechaPago());
        dto.setMonto(entidad.getMonto());

        // Manejo de nulos para montos
        BigDecimal pagado = entidad.getMontoPagado() != null ? entidad.getMontoPagado() : BigDecimal.ZERO;
        dto.setMontoPagado(pagado);

        // Cálculo de monto pendiente: Monto Total - Monto Pagado
        dto.setMontoPendiente(entidad.getMonto().subtract(pagado));

        dto.setEstado(entidad.getEstado().name());
        dto.setFechaPagado(entidad.getFechaPagado());

        return dto;
    }

    public List<CronogramaPagoDetalladoDTO> obtenerCobrosHoyYManana() {
        LocalDate inicioHoy = LocalDate.now();
        LocalDate finManana = inicioHoy.plusDays(1);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));

        return cronogramaRepo.findProximosCobros(inicioHoy, inicioHoy,usuario.getId());
    }
}