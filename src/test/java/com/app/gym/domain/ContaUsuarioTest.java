package com.app.gym.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContaUsuarioTest {

    @Test
    public void deveCriarContaUsuarioComDadosValidos() {
        // Given
        String username = "joao.silva";
        String password = "senhaEncriptada123";
        boolean admin = false;

        // When
        ContaUsuario conta = new ContaUsuario(username, password, admin);

        // Then
        assertEquals(username, conta.getUsername());
        assertEquals(password, conta.getPassword());
        assertFalse(conta.isAdmin());
    }

    @Test
    public void deveCriarContaAdministrador() {
        // Given
        String username = "admin";
        String password = "senhaAdminEncriptada123";
        boolean admin = true;

        // When
        ContaUsuario conta = new ContaUsuario(username, password, admin);

        // Then
        assertEquals(username, conta.getUsername());
        assertEquals(password, conta.getPassword());
        assertTrue(conta.isAdmin());
    }

    @Test
    public void deveRetornarIdNull_QuandoCriadaSemPeristencia() {
        // Given
        ContaUsuario conta = new ContaUsuario("user", "pass", false);

        // When & Then
        assertNull(conta.getId());
    }
}