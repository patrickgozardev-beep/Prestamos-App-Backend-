package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.dto.PagoDTO;
import com.prestamos.prestamosapp.model.Pago;
import com.prestamos.prestamosapp.repository.PagoRepository;
import com.prestamos.prestamosapp.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final PagoRepository pagoRepo;

    public PagoController(PagoService pagoService, PagoRepository pagoRepo) {
        this.pagoService = pagoService;
        this.pagoRepo = pagoRepo;
    }

    // Registrar un nuevo pago (maneja excedentes automáticamente por tu Service)
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPago(@RequestBody PagoDTO dto) {
        try {
            Pago nuevoPago = pagoService.registrarPago(dto);
            return ResponseEntity.ok(nuevoPago);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Listar todos los pagos realizados a un préstamo específico
    @GetMapping("/prestamo/{prestamoId}")
    public ResponseEntity<List<Pago>> listarPagosPorPrestamo(@PathVariable Integer prestamoId) {
        // Buscamos los pagos a través de la relación cronograma -> prestamo
        List<Pago> pagos = pagoRepo.findByCronograma_Prestamo_IdOrderByFechaPagoDesc(prestamoId);
        return ResponseEntity.ok(pagos);
    }

    // Opcional: Listar pagos de una cuota específica
    @GetMapping("/cronograma/{cronogramaId}")
    public ResponseEntity<List<Pago>> listarPagosPorCuota(@PathVariable Integer cronogramaId) {
        List<Pago> pagos = pagoRepo.findByCronogramaIdOrderByFechaPagoDesc(cronogramaId);
        return ResponseEntity.ok(pagos);
    }

    // Endpoint para: OBTENER detalle de un pago único
    // GET /api/pagos/10
    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    // Endpoint para: ELIMINAR un pago (usando la lógica de reversión que hicimos)
    // DELETE /api/pagos/10
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Integer id) {
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }



}