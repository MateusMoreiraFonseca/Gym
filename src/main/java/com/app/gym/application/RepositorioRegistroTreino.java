package com.app.gym.application;

import com.app.gym.domain.RegistroTreino;
import com.app.gym.domain.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RepositorioRegistroTreino extends JpaRepository<RegistroTreino, Long> {
    Optional<RegistroTreino> findByTreinoAndData(Treino treino, LocalDate data);
}
