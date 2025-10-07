package com.istoe.demo.enums;

import lombok.Getter;

@Getter
public enum StatusPernoiteEnum {
    ATIVO(0),
    DIARIA_ENCERRADA(1),
    FINALIZADO(2),
    CANCELADO(3),
    FINALIZADO_PAGAMENTO_PENDENTE(4);


    private final int value;

    StatusPernoiteEnum(int value) {
        this.value = value;
    }

}
