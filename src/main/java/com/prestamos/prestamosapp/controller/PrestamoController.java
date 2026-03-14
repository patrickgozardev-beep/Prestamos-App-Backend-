package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.dto.MetricasDashboardDTO;
import com.prestamos.prestamosapp.dto.PrestamoDTO;
import com.prestamos.prestamosapp.model.Prestamo;
import com.prestamos.prestamosapp.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    // Crear préstamo diario
    @PostMapping("/diario")
    public Prestamo crearPrestamoDiario(@RequestBody PrestamoDTO dto){
        return prestamoService.crearPrestamoDiario(dto);
    }

    // Crear préstamo semanal
    @PostMapping("/semanal")
    public Prestamo crearPrestamoSemanal(@RequestBody PrestamoDTO dto){
        return prestamoService.crearPrestamoSemanal(dto);
    }

    // Reprogramar préstamo
    @PostMapping("/{prestamoId}/reprogramar")
    public Prestamo reprogramarPrestamo(
            @PathVariable Integer prestamoId,
            @RequestParam Integer nuevasCuotas,
            @RequestParam BigDecimal interes){

        return prestamoService.reprogramarPrestamo(prestamoId, nuevasCuotas, interes);
    }

    // Ver préstamos por cliente
    @GetMapping("/cliente/{clienteId}")
    public List<Prestamo> prestamosPorCliente(@PathVariable Integer clienteId){
        return prestamoService.prestamosPorCliente(clienteId);
    }

    // Ver préstamos por usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Prestamo> prestamosPorUsuario(@PathVariable Integer usuarioId){
        return prestamoService.prestamosPorUsuario(usuarioId);
    }

    @GetMapping("/{prestamoId}")
    public Optional<Prestamo> listarPorId(@PathVariable Integer prestamoId){
        return prestamoService.prestamosPorId(prestamoId);
    }

    @GetMapping("/dashboard/resumen")
    public ResponseEntity<MetricasDashboardDTO> getResumen() {
        return ResponseEntity.ok(prestamoService.obtenerMetricasDashboard());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            prestamoService.eliminarPrestamo(id);
            return ResponseEntity.ok().body("Préstamo y todo su historial de pagos eliminados.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el préstamo: " + e.getMessage());
        }
    }
}