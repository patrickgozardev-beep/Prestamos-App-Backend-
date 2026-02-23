package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.EstadoPago;
import com.prestamos.prestamosapp.model.CronogramaPago;
import com.prestamos.prestamosapp.repository.CronogramaPagoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CronogramaPagoService {

    private final CronogramaPagoRepository cronogramaRepo;

    public CronogramaPagoService(CronogramaPagoRepository cronogramaRepo) {
        this.cronogramaRepo = cronogramaRepo;
    }

    public List<CronogramaPago> obtenerPorPrestamo(Integer prestamoId){
        return cronogramaRepo.findByPrestamoId(prestamoId);
    }

    public List<CronogramaPago> pagosDeHoy(LocalDate hoy){
        return cronogramaRepo.findByFechaPago(hoy);
    }

    public CronogramaPago marcarComoPagado(Integer id){
        CronogramaPago cronogramaPago = cronogramaRepo.findById(id).orElseThrow();
        cronogramaPago.setEstado(EstadoPago.PAGADO);
        return cronogramaRepo.save(cronogramaPago);
    }

    public List<CronogramaPago> pendientes(){
        return cronogramaRepo.findByPagadoFalse();
    }
}
