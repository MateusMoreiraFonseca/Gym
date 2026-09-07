package com.app.gym.domain;

public enum IntensidadeTreino {
    LEVE(5, "Leve"),
    MODERADA(8, "Moderada"),
    INTENSA(11, "Intensa");

    private final int kcalPorMinuto;
    private final String descricao;

    IntensidadeTreino(int kcalPorMinuto, String descricao) {
        this.kcalPorMinuto = kcalPorMinuto;
        this.descricao = descricao;
    }

    public int getKcalPorMinuto() { return kcalPorMinuto; }
    public String getDescricao() { return descricao; }
}
