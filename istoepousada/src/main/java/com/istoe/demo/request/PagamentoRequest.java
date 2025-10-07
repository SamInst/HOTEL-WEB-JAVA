package com.istoe.demo.request;

public record PagamentoRequest(
        String descricao,
        Integer tipoPagamento,
        Float valorPagamento
){}
