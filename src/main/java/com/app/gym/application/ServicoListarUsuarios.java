package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoListarUsuarios {

    private final RepositorioContaUsuario repositorioContaUsuario;

    /**
     * Cria o serviço para listar todos os usuários cadastrados.
     */
    public ServicoListarUsuarios(RepositorioContaUsuario repositorioContaUsuario) {
        this.repositorioContaUsuario = repositorioContaUsuario;
    }

    /**
     * Retorna todos os usuários existentes no banco.
     */
    public List<ContaUsuario> listarTodosUsuarios() {
        return repositorioContaUsuario.findAll();
    }
}

