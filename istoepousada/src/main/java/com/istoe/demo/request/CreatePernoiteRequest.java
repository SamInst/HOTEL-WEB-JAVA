package com.istoe.demo.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreatePernoiteRequest(
        LocalDate dataEntrada,
        LocalDate dataSaida,
        LocalTime horaChegada,
        LocalTime horaSaida,
        Long quarto,
        List<HospedeRequest> hospedes,
        List<PagamentoRequest> pagamentoRequestList
) {}
