package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import com.app.gym.domain.TipoRecorrenciaTreino;
import com.app.gym.domain.Treino;
import com.app.gym.domain.IntensidadeTreino;
import com.app.gym.domain.RegistroTreino;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ServicoTreino {
    private final RepositorioTreino repositorioTreino;
    private final RepositorioRegistroTreino repositorioRegistro;

    public ServicoTreino(RepositorioTreino repositorioTreino, RepositorioRegistroTreino repositorioRegistro) {
        this.repositorioTreino = repositorioTreino;
        this.repositorioRegistro = repositorioRegistro;
    }

    @Transactional
    public Treino cadastrar(ContaUsuario usuario, String nome, String descricao,
                            TipoRecorrenciaTreino tipo, DayOfWeek diaDaSemana, LocalDate dataUnica) {
        return cadastrar(usuario, nome, descricao, tipo,
                diaDaSemana == null ? Set.of() : Set.of(diaDaSemana), dataUnica);
    }

    @Transactional
    public Treino cadastrar(ContaUsuario usuario, String nome, String descricao,
                                TipoRecorrenciaTreino tipo, Set<DayOfWeek> diasDaSemana, LocalDate dataUnica) {
        return cadastrar(usuario, nome, descricao, tipo, diasDaSemana, dataUnica, null);
    }

    @Transactional
    public Treino cadastrar(ContaUsuario usuario, String nome, String descricao,
                            TipoRecorrenciaTreino tipo, Set<DayOfWeek> diasDaSemana,
                            LocalDate dataUnica, LocalDate dataInicio) {
        if (nome == null || nome.isBlank() || tipo == null) {
            throw new IllegalArgumentException("Nome e recorrência são obrigatórios.");
        }
        if (tipo == TipoRecorrenciaTreino.DIA_DA_SEMANA && (diasDaSemana == null || diasDaSemana.isEmpty())) {
            throw new IllegalArgumentException("Selecione pelo menos um dia da semana.");
        }
        if (tipo == TipoRecorrenciaTreino.DATA_UNICA && dataUnica == null) {
            throw new IllegalArgumentException("Informe a data do treino.");
        }
        LocalDate dataPersistida = tipo == TipoRecorrenciaTreino.DATA_UNICA ? dataUnica : null;
        Set<DayOfWeek> diasPersistidos = tipo == TipoRecorrenciaTreino.DIA_DA_SEMANA ? diasDaSemana : Set.of();
        LocalDate inicioPersistido = tipo == TipoRecorrenciaTreino.DATA_UNICA ? null : dataInicio;
        return repositorioTreino.save(new Treino(usuario, nome.trim(), descricao, tipo,
            diasPersistidos, dataPersistida, inicioPersistido));
    }

    @Transactional(readOnly = true)
    public List<Treino> listarDoDia(ContaUsuario usuario, LocalDate data) {
        return repositorioTreino.findByUsuario(usuario).stream()
                .filter(treino -> treino.aconteceEm(data))
                .toList();
    }

    @Transactional
    public RegistroTreino atualizarStatus(ContaUsuario usuario, Treino treino, LocalDate data,
                                           boolean concluido, int totalRepeticoes,
                                           int duracaoMinutos, IntensidadeTreino intensidade) {
        if ((concluido && (totalRepeticoes <= 0 || duracaoMinutos <= 0 || intensidade == null))
                || !repositorioTreino.findById(treino.getId()).map(t -> t.getUsuario().getId().equals(usuario.getId())).orElse(false)) {
            throw new IllegalArgumentException("Treino inválido ou informe total de repetições, duração e intensidade.");
        }
        RegistroTreino registro = repositorioRegistro.findByTreinoAndData(treino, data)
                .orElseGet(() -> new RegistroTreino(treino, data, concluido, totalRepeticoes, duracaoMinutos, intensidade));
        registro.atualizar(concluido, totalRepeticoes, duracaoMinutos, intensidade);
        return repositorioRegistro.save(registro);
    }

    @Transactional(readOnly = true)
    public RegistroTreino obterRegistro(Treino treino, LocalDate data) {
        return repositorioRegistro.findByTreinoAndData(treino, data).orElse(null);
    }
}
