package com.prestamos.prestamosapp.service;

import com.prestamos.prestamosapp.dto.ClienteDTO;
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

    public Cliente guardar(ClienteDTO dto) {
        // 1. Obtener el usuario autenticado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validación mínima de existencia (Solo si es actualización)
        if (dto.getId() != null) {
            if (!clienteRepository.existsById(dto.getId())) {
                throw new RuntimeException("No se puede actualizar: El cliente con ID " + dto.getId() + " no existe.");
            }
        }

        // 3. Mapeo y Guardado
        // Al no haber validación de DNI, se creará o actualizará directamente
        Cliente cliente = Cliente.builder()
                .id(dto.getId())
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .telefono(dto.getTelefono())
                .googleMapsLink(dto.getGoogleMapsLink())
                .dniPdf(dto.getDniPdf())
                .usuario(usuario)
                .build();

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
