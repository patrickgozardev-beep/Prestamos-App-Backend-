package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.dto.CronogramaPagoDTO;
import com.prestamos.prestamosapp.model.CronogramaPago;
import com.prestamos.prestamosapp.service.CronogramaPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cronogramas")
public class CronogramaPagoController {

    private final CronogramaPagoService cronogramaService;

    public CronogramaPagoController(CronogramaPagoService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    @GetMapping("/prestamo/{prestamoId}")
    public ResponseEntity<List<CronogramaPagoDTO>> listarPorPrestamo(@PathVariable Integer prestamoId) {

        List<CronogramaPagoDTO> cronograma = cronogramaService.obtenerPorPrestamo(prestamoId);
        return ResponseEntity.ok(cronograma);
    }

    @GetMapping("/proximos-cobros")
    public ResponseEntity<List<CronogramaPago>> obtenerProximosCobros() {
        List<CronogramaPago> proximos = cronogramaService.obtenerCobrosHoyYManana();
        return ResponseEntity.ok(proximos);
    }

}