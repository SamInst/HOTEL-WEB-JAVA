package com.istoe.demo.enums;

import lombok.Getter;

@Getter
public enum TipoQuartoEnum {
    QUARTO_INDIVIDUAL(1),
    QUARTO_DUPLO(2),
    QUARTO_TRIPLO(3),
    QUARTO_QUADRUPLO(4),
    QUARTO_QUINTUPLO(5);

    private final int valor;

    TipoQuartoEnum(int valor) {
        this.valor = valor;
    }

    public static TipoQuartoEnum fromCodigo(int codigo) {
        for (TipoQuartoEnum status : TipoQuartoEnum.values()) {
            if (status.getValor() == codigo) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }

}
