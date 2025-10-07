package com.istoe.demo.response;

import java.time.LocalDate;

public record FuncionarioResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        LocalDate dataNascimento,
        LocalDate dataAdmissao,
        CargoResponse cargo,
        SituacaoFuncionarioResponse situacao
) {
    public record CargoResponse(
            Long id,
            String descricao
    ) {}

    public record SituacaoFuncionarioResponse(
            Long id,
            String descricao
    ) {}
}