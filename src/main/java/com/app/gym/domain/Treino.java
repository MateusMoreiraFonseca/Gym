package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "treinos")
public class Treino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private ContaUsuario usuario;

    private String nome;
    private String descricao;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private TipoRecorrenciaTreino tipoRecorrencia;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private DayOfWeek diaDaSemana;
    @jakarta.persistence.ElementCollection
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @jakarta.persistence.CollectionTable(name = "treino_dias_semana", joinColumns = @jakarta.persistence.JoinColumn(name = "treino_id"))
    private Set<DayOfWeek> diasDaSemana = new HashSet<>();
    private LocalDate dataUnica;
    private LocalDate dataInicio;
    protected Treino() {
    }

    public Treino(ContaUsuario usuario, String nome, String descricao,
                  TipoRecorrenciaTreino tipoRecorrencia, DayOfWeek diaDaSemana, LocalDate dataUnica) {
        this(usuario, nome, descricao, tipoRecorrencia,
                diaDaSemana == null ? Set.of() : Set.of(diaDaSemana), dataUnica);
    }

    public Treino(ContaUsuario usuario, String nome, String descricao,
                  TipoRecorrenciaTreino tipoRecorrencia, Set<DayOfWeek> diasDaSemana, LocalDate dataUnica) {
        this(usuario, nome, descricao, tipoRecorrencia, diasDaSemana, dataUnica, null);
    }

    public Treino(ContaUsuario usuario, String nome, String descricao,
                  TipoRecorrenciaTreino tipoRecorrencia, Set<DayOfWeek> diasDaSemana,
                  LocalDate dataUnica, LocalDate dataInicio) {
        this.usuario = usuario;
        this.nome = nome;
        this.descricao = descricao;
        this.tipoRecorrencia = tipoRecorrencia;
        this.diaDaSemana = diasDaSemana.stream().findFirst().orElse(null);
        this.diasDaSemana = new HashSet<>(diasDaSemana);
        this.dataUnica = dataUnica;
        this.dataInicio = dataInicio;
    }

    public Long getId() { return id; }
    public ContaUsuario getUsuario() { return usuario; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public TipoRecorrenciaTreino getTipoRecorrencia() { return tipoRecorrencia; }
    public DayOfWeek getDiaDaSemana() { return diaDaSemana; }
    public Set<DayOfWeek> getDiasDaSemana() { return Set.copyOf(diasDaSemana); }
    public LocalDate getDataUnica() { return dataUnica; }
    public LocalDate getDataInicio() { return dataInicio; }
    public boolean aconteceEm(LocalDate data) {
        if (dataInicio != null && data.isBefore(dataInicio)) return false;
        return switch (tipoRecorrencia) {
            case DIARIO -> true;
            case DIA_DA_SEMANA -> diasDaSemana.contains(data.getDayOfWeek()) || diaDaSemana == data.getDayOfWeek();
            case DATA_UNICA -> data.equals(dataUnica);
        };
    }
}
