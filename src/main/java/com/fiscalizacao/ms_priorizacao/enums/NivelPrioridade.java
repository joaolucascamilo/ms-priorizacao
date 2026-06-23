package com.fiscalizacao.ms_priorizacao.enums;

public enum NivelPrioridade {
    CRITICA,
    ALTA,
    MEDIA,
    BAIXA;

    // Converte score final em nível
    public static NivelPrioridade fromScore(int score) {
        if (score >= 65) return CRITICA;
        if (score >= 40) return ALTA;
        if (score >= 20) return MEDIA;
        return BAIXA;
    }
}
