package com.app.gym.application;

/**
 * Indica que uma tentativa de cadastro foi rejeitada porque o nome de usuário já está em uso.
 */
public class UsuarioJaExistenteException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem que será exibida ao usuário.
     */
    public UsuarioJaExistenteException(String message) {
        super(message);
    }
}
