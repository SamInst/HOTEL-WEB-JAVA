package com.istoe.demo.request;

public record RelatorioRequest(
        Long fkTipoPagamento,
        String relatorio,
        Double valor,
        Long quartoId
) {}