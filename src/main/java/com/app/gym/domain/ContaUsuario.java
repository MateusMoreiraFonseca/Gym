package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ContaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private boolean admin;

    /**
     * Cria uma instância vazia de conta de usuário para uso pelo framework de persistência.
     */
    public ContaUsuario() {
    }

    /**
     * Cria uma nova conta de usuário com credenciais e definição de perfil administrativo.
     */
    public ContaUsuario(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    /**
     * Retorna o identificador da conta de usuário.
     */
    public Long getId() { return id; }

    /**
     * Retorna o nome de usuário associado à conta.
     */
    public String getUsername() { return username; }

    /**
     * Retorna a senha criptografada da conta.
     */
    public String getPassword() { return password; }

    /**
     * Informa se a conta possui privilégios de administrador.
     */
    public boolean isAdmin() { return admin; }
}
