package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class ContaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private boolean admin;
    @Lob
    private byte[] fotoPerfil;
    private String tipoMimeFotoPerfil;

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

    public byte[] getFotoPerfil() { return fotoPerfil; }

    public String getTipoMimeFotoPerfil() { return tipoMimeFotoPerfil; }

    public boolean possuiFotoPerfil() {
        return fotoPerfil != null && fotoPerfil.length > 0 && tipoMimeFotoPerfil != null;
    }

    public void atualizarSenha(String novaSenha) {
        this.password = novaSenha;
    }

    public void atualizarFotoPerfil(byte[] foto, String tipoMime) {
        this.fotoPerfil = foto;
        this.tipoMimeFotoPerfil = tipoMime;
    }
}
