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
@Table(name = "registros_caloricos")
public class RegistroCalorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private ContaUsuario usuario;

    private LocalDate data;
    private int kcalIngeridas;
    private int kcalGastas;

    protected RegistroCalorico() {}

    public RegistroCalorico(ContaUsuario usuario, LocalDate data, int kcalIngeridas, int kcalGastas) {
        this.usuario = usuario;
        this.data = data;
        this.kcalIngeridas = kcalIngeridas;
        this.kcalGastas = kcalGastas;
    }

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public int getKcalIngeridas() { return kcalIngeridas; }
    public int getKcalGastas() { return kcalGastas; }

    public void atualizar(int kcalIngeridas, int kcalGastas) {
        this.kcalIngeridas = kcalIngeridas;
        this.kcalGastas = kcalGastas;
    }
}
