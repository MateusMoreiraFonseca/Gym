package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class PerfilFitness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private ContaUsuario usuario;

    private int idade;
    private double altura;
    private double pesoAtual;
    private double pesoMeta;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    private NivelAtividade nivelAtividade;

    protected PerfilFitness() {}

    public PerfilFitness(ContaUsuario usuario, int idade, double altura, double pesoAtual,
                         double pesoMeta, Sexo sexo, NivelAtividade nivelAtividade) {
        this.usuario = usuario;
        this.idade = idade;
        this.altura = altura;
        this.pesoAtual = pesoAtual;
        this.pesoMeta = pesoMeta;
        this.sexo = sexo;
        this.nivelAtividade = nivelAtividade;
    }

    public Long getId() { return id; }
    public int getIdade() { return idade; }
    public double getAltura() { return altura; }
    public double getPesoAtual() { return pesoAtual; }
    public double getPesoMeta() { return pesoMeta; }
    public Sexo getSexo() { return sexo; }
    public NivelAtividade getNivelAtividade() { return nivelAtividade; }

    public void atualizar(int idade, double altura, double pesoAtual, double pesoMeta,
                          Sexo sexo, NivelAtividade nivelAtividade) {
        this.idade = idade;
        this.altura = altura;
        this.pesoAtual = pesoAtual;
        this.pesoMeta = pesoMeta;
        this.sexo = sexo;
        this.nivelAtividade = nivelAtividade;
    }
}
