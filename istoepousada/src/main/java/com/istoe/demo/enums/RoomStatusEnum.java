package com.istoe.demo.enums;

import lombok.Getter;

@Getter
public enum RoomStatusEnum {
    OCUPADO(1),
    DISPONIVEL(2),
    RESERVADO(3),
    LIMPEZA(4),
    DIARIA_ENCERRADA(5),
    MANUTENCAO(6);

    private final int codigo;

    RoomStatusEnum(int codigo) {
        this.codigo = codigo;
    }

    public static RoomStatusEnum fromCodigo(int codigo) {
        for (RoomStatusEnum status : RoomStatusEnum.values()) {
            if (status.getCodigo() == codigo) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
