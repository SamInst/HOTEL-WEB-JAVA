package com.istoe.demo.request;

public record UpdateQuartoRequest(
        String descricao,
        Integer quantidadePessoas,
        Integer statusCodigo,
        Integer qtdCamaCasal,
        Integer qtdCamaSolteiro,
        Integer qtdRede,
        Integer qtdBeliche,
        Long categoriaId
) {}
