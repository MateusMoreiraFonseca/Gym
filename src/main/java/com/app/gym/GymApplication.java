package com.app.gym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GymApplication {
    /**
     * Inicia a aplicação Spring Boot com o contexto necessário para o funcionamento do sistema.
     */
    public static void main(String[] args) {
        SpringApplication.run(GymApplication.class, args);
    }
}
