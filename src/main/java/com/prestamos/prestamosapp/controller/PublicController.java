package com.prestamos.prestamosapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Servidor de Préstamos despertando correctamente");
        response.put("server_time", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(response);
    }
}