package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.model.TipoPrestamo;
import com.prestamos.prestamosapp.repository.ClienteRepository;
import com.prestamos.prestamosapp.repository.TipoPrestamoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class TipoPrestamoService {
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public TipoPrestamoService(TipoPrestamoRepository tipoPrestamoRepository) {
        this.tipoPrestamoRepository = tipoPrestamoRepository;
    }

    public List<TipoPrestamo> listarTodos(){
        return tipoPrestamoRepository.findAll();
    }


}
