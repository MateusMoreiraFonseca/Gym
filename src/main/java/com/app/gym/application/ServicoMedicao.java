package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.Medicao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoMedicao {
    private final RepositorioMedicao repositorioMedicao;

    public ServicoMedicao(RepositorioMedicao repositorioMedicao) {
        this.repositorioMedicao = repositorioMedicao;
    }

    public double calcularImc(double peso, double altura) {
        validarMedidas(peso, altura);
        return peso / (altura * altura);
    }

    @Transactional
    public Medicao registrar(ContaUsuario usuario, double peso, double altura) {
        double imc = calcularImc(peso, altura);
        return repositorioMedicao.save(new Medicao(usuario, peso, altura, imc));
    }

    @Transactional(readOnly = true)
    public List<Medicao> listar(ContaUsuario usuario) {
        return repositorioMedicao.findByUsuarioOrderByDataMedicaoDesc(usuario);
    }

    public String classificacaoImc(double imc) {
        if (imc < 18.5) {
            return "Abaixo do peso";
        }
        if (imc < 25) {
            return "Peso normal";
        }
        if (imc < 30) {
            return "Sobrepeso";
        }
        return "Obesidade";
    }

    private void validarMedidas(double peso, double altura) {
        if (!Double.isFinite(peso) || peso <= 0 || peso > 500) {
            throw new IllegalArgumentException("O peso deve estar entre 0 e 500 kg.");
        }
        if (!Double.isFinite(altura) || altura <= 0 || altura > 3) {
            throw new IllegalArgumentException("A altura deve estar entre 0 e 3 metros.");
        }
    }
}
