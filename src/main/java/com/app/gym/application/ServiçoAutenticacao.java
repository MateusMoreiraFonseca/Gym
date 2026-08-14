package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiçoAutenticacao {
    private final RepositorioContaUsuario userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cria o serviço de autenticação com acesso ao repositório e ao codificador de senhas.
     */
    public ServiçoAutenticacao(RepositorioContaUsuario userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra uma nova conta de usuário após validar a disponibilidade do nome de usuário.
     */
    public ContaUsuario cadastrar(String username, String password) {
        return cadastrar(username, password, false);
    }

    /**
     * Registra uma nova conta de usuário após validar a disponibilidade do nome de usuário.
     * Permite definir se o usuário será ADMIN.
     */
    public ContaUsuario cadastrar(String username, String password, boolean isAdmin) {
        // Validar inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode ser vazio ou nulo");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser vazia ou nula");
        }
        
        if (userAccountRepository.findByUsername(username).isPresent()) {
            throw new UsuarioJaExistenteException("Usuário já existe");
        }
        ContaUsuario usuario = new ContaUsuario(username, passwordEncoder.encode(password), isAdmin);
        return userAccountRepository.save(usuario);
    }


    /**
     * Busca uma conta de usuário pelo nome informado.
     */
    public Optional<ContaUsuario> buscarPorNomeUsuario(String username) {
        return userAccountRepository.findByUsername(username);
    }
}
