package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.dto.ClienteDTO;
import com.prestamos.prestamosapp.model.Cliente;
import com.prestamos.prestamosapp.service.ClienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public Cliente crearCliente(@RequestBody ClienteDTO cliente){
        return clienteService.guardar(cliente);
    }

    @PutMapping("/{id}")
    public Cliente actualizarCliente(@PathVariable Integer id, @RequestBody ClienteDTO dto) {
        dto.setId(id);
        return clienteService.guardar(dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarCliente(@PathVariable Integer id){
        clienteService.eliminar(id);
    }

    @GetMapping("/{id}")
    public Cliente obtenerClientePorId(@PathVariable Integer id) {
        return clienteService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    @GetMapping("/buscar")
    public List<Cliente> buscarClientes(@RequestParam String busqueda){
        return clienteService.buscarClientes( busqueda);
    }

    @GetMapping("/usuario")
    public List<Cliente> listarPorUsuario(){
        return clienteService.listarPorUsuario();
    }

    @GetMapping
    public List<Cliente> listaClientes(){
        return clienteService.listarClientesPorUsuario();
    }


}