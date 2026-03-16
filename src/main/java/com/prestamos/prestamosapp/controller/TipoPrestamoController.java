package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.model.TipoPrestamo;
import com.prestamos.prestamosapp.service.ClienteService;
import com.prestamos.prestamosapp.service.TipoPrestamoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/tipo_prestamo")

public class TipoPrestamoController {

    private final TipoPrestamoService tipoPrestamoService;

    public TipoPrestamoController(TipoPrestamoService tipoPrestamoService) {
        this.tipoPrestamoService = tipoPrestamoService;
    }

    @GetMapping
    public List<TipoPrestamo> listaTipoPrestamo(){
        return tipoPrestamoService.listarTodos();
    }

}
