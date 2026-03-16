package com.prestamos.prestamosapp.controller;

import com.prestamos.prestamosapp.dto.AuthRequest;
import com.prestamos.prestamosapp.dto.AuthResponse;
import com.prestamos.prestamosapp.model.Usuario;
import com.prestamos.prestamosapp.security.JwtUtil;
import com.prestamos.prestamosapp.service.DetallesUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DetallesUsuarioService detallesUsuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            // 1. Autenticamos y obtenemos el objeto de autenticación
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            Usuario usuario = (Usuario) authentication.getPrincipal();

            // 3. Generamos el token usando los datos del objeto que ya tenemos en memoria
            // (Sin necesidad de llamar a detallesUsuarioService de nuevo)
            final String jwt = jwtUtil.generateToken(usuario);

            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
        }
    }
}
