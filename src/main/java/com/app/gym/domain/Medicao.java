package com.app.gym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicoes")
public class Medicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private ContaUsuario usuario;

    private double peso;
    private double altura;
    private double imc;
    private LocalDateTime dataMedicao;

    protected Medicao() {
    }

    public Medicao(ContaUsuario usuario, double peso, double altura, double imc) {
        this.usuario = usuario;
        this.peso = peso;
        this.altura = altura;
        this.imc = imc;
        this.dataMedicao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ContaUsuario getUsuario() {
        return usuario;
    }

    public double getPeso() {
        return peso;
    }

    public double getAltura() {
        return altura;
    }

    public double getImc() {
        return imc;
    }

    public LocalDateTime getDataMedicao() {
        return dataMedicao;
    }
}
