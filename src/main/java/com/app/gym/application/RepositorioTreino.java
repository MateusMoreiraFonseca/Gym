package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioTreino extends JpaRepository<Treino, Long> {
    List<Treino> findByUsuario(ContaUsuario usuario);
}
