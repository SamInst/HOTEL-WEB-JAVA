package com.istoe.demo.request;

import java.util.List;

public record UpdateDiariaRequest(
        Long quarto,
        List<HospedeRequest> hospedes,
        List<PagamentoRequest> pagamentoRequestList
) { }
