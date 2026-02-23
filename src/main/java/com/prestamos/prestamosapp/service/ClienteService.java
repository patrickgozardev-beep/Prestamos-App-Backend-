package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente guardar(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarPorUsuario(Integer usuarioId){
        return clienteRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Cliente> obtenerPorId(Integer id){
        return clienteRepository.findById(id);
    }

    public void eliminar(Integer id){
        clienteRepository.deleteById(id);
    }
}
