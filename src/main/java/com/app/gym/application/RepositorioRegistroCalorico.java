package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.RegistroCalorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositorioRegistroCalorico extends JpaRepository<RegistroCalorico, Long> {
    Optional<RegistroCalorico> findByUsuarioAndData(ContaUsuario usuario, LocalDate data);

    List<RegistroCalorico> findByUsuarioAndDataBetweenOrderByDataAsc(ContaUsuario usuario, LocalDate inicio, LocalDate fim);

    List<RegistroCalorico> findByUsuarioOrderByDataAsc(ContaUsuario usuario);
}
