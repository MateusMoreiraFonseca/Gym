package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServiçoSementeAdministrador implements ApplicationRunner {

    private final RepositorioContaUsuario userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * Cria o serviço responsável por garantir a existência do usuário administrador inicial.
     */
    public ServiçoSementeAdministrador(RepositorioContaUsuario userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Garante que o administrador padrão exista no banco de dados ao iniciar a aplicação.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (userAccountRepository.findByUsername(adminUsername).isEmpty()) {
            ContaUsuario admin = new ContaUsuario(adminUsername, passwordEncoder.encode(adminPassword), true);
            userAccountRepository.save(admin);
        }
    }
}
