package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "registros_treino")
public class RegistroTreino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false)
    private Treino treino;

    private LocalDate data;
    private boolean concluido;
    private int totalRepeticoes;
    private int duracaoMinutos;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private IntensidadeTreino intensidade;
    private int kcalGastas;

    protected RegistroTreino() {}

    public RegistroTreino(Treino treino, LocalDate data, boolean concluido,
                          int totalRepeticoes, int duracaoMinutos, IntensidadeTreino intensidade) {
        this.treino = treino;
        this.data = data;
        this.concluido = concluido;
        this.totalRepeticoes = totalRepeticoes;
        this.duracaoMinutos = duracaoMinutos;
        this.intensidade = intensidade;
        this.kcalGastas = concluido ? duracaoMinutos * intensidade.getKcalPorMinuto() : 0;
    }

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public boolean isConcluido() { return concluido; }
    public int getTotalRepeticoes() { return totalRepeticoes; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public IntensidadeTreino getIntensidade() { return intensidade; }
    public int getKcalGastas() { return kcalGastas; }

    public void atualizar(boolean concluido, int totalRepeticoes,
                          int duracaoMinutos, IntensidadeTreino intensidade) {
        this.concluido = concluido;
        this.totalRepeticoes = totalRepeticoes;
        this.duracaoMinutos = duracaoMinutos;
        this.intensidade = intensidade;
        this.kcalGastas = concluido ? duracaoMinutos * intensidade.getKcalPorMinuto() : 0;
    }
}
