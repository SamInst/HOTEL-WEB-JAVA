package com.istoe.demo.response;

import java.time.LocalDateTime;

public record RelatorioResponse(
        Long id,
        LocalDateTime dataHora,
        ObjetoResponse tipoPagamento,
        String relatorio,
        Long pernoiteId,
        Long entradaId,
        Double valor,
        Long quartoId,
        ObjetoResponse funcionario
) {}