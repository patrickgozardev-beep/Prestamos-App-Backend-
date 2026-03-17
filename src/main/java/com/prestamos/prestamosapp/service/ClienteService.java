package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.exception.BadRequestException;
import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.model.Usuario;
import com.prestamos.prestamosapp.repository.ClienteRepository;
import com.prestamos.prestamosapp.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepo;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepo) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepo = usuarioRepo;
    }

    public Cliente guardar(Cliente cliente) {
        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new BadRequestException("El cliente con DNI " + cliente.getDni() + " ya está registrado.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));

        cliente.setUsuario(usuario);
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> obtenerPorId(Integer id){
        return clienteRepository.findById(id);
    }

    public void eliminar(Integer id){
        clienteRepository.deleteById(id);
    }

    public List<Cliente> listarPorUsuario(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));
        return clienteRepository.findByUsuarioId(usuario.getId());
    }

    public List<Cliente> listarClientesPorUsuario() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return clienteRepository.findByUsuarioId(usuario.getId());
    }

    public List<Cliente> buscarClientes( String busqueda){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB"));


        return clienteRepository.buscarClientes(usuario.getId(), busqueda);
    }

}
