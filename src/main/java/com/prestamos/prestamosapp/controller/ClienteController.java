package com.prestamos.prestamosapp.controller;

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
    public Cliente crearCliente(@RequestBody Cliente cliente){
        return clienteService.guardar(cliente);
    }

    @PutMapping("/{id}")
    public Cliente actualizarCliente(@PathVariable Integer id, @RequestBody Cliente cliente){

        Optional<Cliente> clienteExistente = clienteService.obtenerPorId(id);

        if(clienteExistente.isEmpty()){
            throw new RuntimeException("Cliente no encontrado");
        }

        Cliente c = clienteExistente.get();

        c.setNombres(cliente.getNombres());
        c.setDni(cliente.getDni());
        c.setTelefono(cliente.getTelefono());
        c.setGoogleMapsLink(cliente.getGoogleMapsLink());
        c.setDniPdf(cliente.getDniPdf());

        return clienteService.guardar(c);
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
    public List<Cliente> buscarClientes(
            @RequestParam Integer usuarioId,
            @RequestParam String busqueda){

        return clienteService.buscarClientes(usuarioId, busqueda);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Cliente> listarPorUsuario(@PathVariable Integer usuarioId){
        return clienteService.listarPorUsuario(usuarioId);
    }

    @GetMapping
    public List<Cliente> listaClientes(){
        return clienteService.listarClientes();
    }


}