package com.app.gym.domain;

public enum NivelAtividade {
    SEDENTARIO(1.2, "Sedentário"),
    LEVEMENTE_ATIVO(1.375, "Levemente ativo"),
    MODERADAMENTE_ATIVO(1.55, "Moderadamente ativo"),
    MUITO_ATIVO(1.725, "Muito ativo"),
    EXTREMAMENTE_ATIVO(1.9, "Extremamente ativo");

    private final double fator;
    private final String descricao;

    NivelAtividade(double fator, String descricao) {
        this.fator = fator;
        this.descricao = descricao;
    }

    public double getFator() { return fator; }
    public String getDescricao() { return descricao; }
}
