package com.prestamos.prestamosapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PrestamosappApplicationTests {

	@Test
    void generarPassword() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String passwordPlano = "1234";
        String passwordCodificado = encoder.encode(passwordPlano);

        System.out.println("Password codificado: " + passwordCodificado);
    }


}
