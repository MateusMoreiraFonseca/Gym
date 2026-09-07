package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioMedicao extends JpaRepository<Medicao, Long> {
    List<Medicao> findByUsuarioOrderByDataMedicaoDesc(ContaUsuario usuario);
}
