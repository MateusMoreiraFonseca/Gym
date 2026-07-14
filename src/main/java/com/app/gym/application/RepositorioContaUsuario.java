package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioContaUsuario extends JpaRepository<ContaUsuario, Long> {
    /**
     * Busca uma conta de usuário pelo nome de usuário informado.
     */
    Optional<ContaUsuario> findByUsername(String username);
}
