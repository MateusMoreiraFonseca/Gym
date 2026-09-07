package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.PerfilFitness;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioPerfilFitness extends JpaRepository<PerfilFitness, Long> {
    Optional<PerfilFitness> findByUsuario(ContaUsuario usuario);
}
