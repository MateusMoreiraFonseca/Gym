package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.NivelAtividade;
import com.app.gym.domain.PerfilFitness;
import com.app.gym.domain.RegistroCalorico;
import com.app.gym.domain.Sexo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServicoCalorico {
    private final RepositorioPerfilFitness repositorioPerfil;
    private final RepositorioRegistroCalorico repositorioRegistro;

    public ServicoCalorico(RepositorioPerfilFitness repositorioPerfil,
                           RepositorioRegistroCalorico repositorioRegistro) {
        this.repositorioPerfil = repositorioPerfil;
        this.repositorioRegistro = repositorioRegistro;
    }

    @Transactional
    public PerfilFitness salvarPerfil(ContaUsuario usuario, int idade, double altura, double pesoAtual,
                                      double pesoMeta, Sexo sexo, NivelAtividade atividade) {
        validarPerfil(idade, altura, pesoAtual, pesoMeta, sexo, atividade);
        PerfilFitness perfil = repositorioPerfil.findByUsuario(usuario).orElseGet(
                () -> new PerfilFitness(usuario, idade, altura, pesoAtual, pesoMeta, sexo, atividade));
        perfil.atualizar(idade, altura, pesoAtual, pesoMeta, sexo, atividade);
        return repositorioPerfil.save(perfil);
    }

    @Transactional(readOnly = true)
    public PerfilFitness obterPerfil(ContaUsuario usuario) {
        return repositorioPerfil.findByUsuario(usuario).orElse(null);
    }

    public double metabolismoMifflin(PerfilFitness p) {
        double base = 10 * p.getPesoAtual() + 6.25 * (p.getAltura() * 100) - 5 * p.getIdade();
        return base + (p.getSexo() == Sexo.MASCULINO ? 5 : -161);
    }

    public double metabolismoHarris(PerfilFitness p) {
        if (p.getSexo() == Sexo.MASCULINO) {
            return 88.362 + 13.397 * p.getPesoAtual() + 4.799 * (p.getAltura() * 100) - 5.677 * p.getIdade();
        }
        return 447.593 + 9.247 * p.getPesoAtual() + 3.098 * (p.getAltura() * 100) - 4.330 * p.getIdade();
    }

    public double metabolismoMedio(PerfilFitness p) {
        return (metabolismoMifflin(p) + metabolismoHarris(p)) / 2;
    }

    public double gastoTotalDiario(PerfilFitness p) {
        return metabolismoMedio(p) * p.getNivelAtividade().getFator();
    }

    public double metaCalorica(PerfilFitness p) {
        double ajuste = p.getPesoAtual() > p.getPesoMeta() ? -500 : p.getPesoAtual() < p.getPesoMeta() ? 500 : 0;
        return gastoTotalDiario(p) + ajuste;
    }

    @Transactional
    public RegistroCalorico salvarRegistro(ContaUsuario usuario, LocalDate data, int ingeridas, int gastas) {
        if (ingeridas < 0 || gastas < 0) {
            throw new IllegalArgumentException("As calorias não podem ser negativas.");
        }
        RegistroCalorico registro = repositorioRegistro.findByUsuarioAndData(usuario, data)
                .orElseGet(() -> new RegistroCalorico(usuario, data, ingeridas, gastas));
        registro.atualizar(ingeridas, gastas);
        return repositorioRegistro.save(registro);
    }

    @Transactional(readOnly = true)
    public RegistroCalorico obterRegistro(ContaUsuario usuario, LocalDate data) {
        return repositorioRegistro.findByUsuarioAndData(usuario, data).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<RegistroCalorico> historicoCalorico(ContaUsuario usuario, String periodo, LocalDate hoje) {
        return switch (periodo) {
            case "semana" -> repositorioRegistro.findByUsuarioAndDataBetweenOrderByDataAsc(
                    usuario, hoje.minusDays(6), hoje);
            case "todo" -> repositorioRegistro.findByUsuarioOrderByDataAsc(usuario);
            default -> repositorioRegistro.findByUsuarioAndDataBetweenOrderByDataAsc(
                    usuario, hoje.withDayOfMonth(1), hoje);
        };
    }

    @Transactional(readOnly = true)
    public MediaCalorica mediaCalorica(ContaUsuario usuario, String periodo, LocalDate hoje) {
        List<RegistroCalorico> registros = historicoCalorico(usuario, periodo, hoje);
        double mediaIngeridas = registros.stream().mapToInt(RegistroCalorico::getKcalIngeridas).average().orElse(0);
        double mediaGastas = registros.stream().mapToInt(RegistroCalorico::getKcalGastas).average().orElse(0);
        return new MediaCalorica(Math.round(mediaIngeridas), Math.round(mediaGastas), registros.size());
    }

    public record MediaCalorica(long ingeridas, long gastas, int quantidadeRegistros) {}

    public String situacao(PerfilFitness perfil, RegistroCalorico registro) {
        if (registro == null) return "Sem registro calórico";
        double saldo = registro.getKcalIngeridas()
                - (metaCalorica(perfil) + registro.getKcalGastas());
        if (Math.abs(saldo) < 1) return "Meta atingida";
        return saldo < 0 ? "Déficit de " + Math.round(-saldo) + " kcal" : "Superávit de " + Math.round(saldo) + " kcal";
    }

    private void validarPerfil(int idade, double altura, double peso, double meta, Sexo sexo, NivelAtividade atividade) {
        if (idade < 13 || idade > 120 || altura <= 0 || altura > 3 || peso <= 0 || peso > 500
                || meta <= 0 || meta > 500 || sexo == null || atividade == null) {
            throw new IllegalArgumentException("Informe dados válidos para calcular seu metabolismo.");
        }
    }
}
