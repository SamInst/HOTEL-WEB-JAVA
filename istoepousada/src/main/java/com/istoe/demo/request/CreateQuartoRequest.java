package com.istoe.demo.request;

public record CreateQuartoRequest(
        String descricao,
        Integer quantidadePessoas,
        Integer statusCodigo,      // 1..6 (mapeia para RoomStatusEnum)
        Integer qtdCamaCasal,
        Integer qtdCamaSolteiro,
        Integer qtdRede,
        Integer qtdBeliche,
        Long categoriaId
) {}
