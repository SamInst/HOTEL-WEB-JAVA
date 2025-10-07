package com.istoe.demo.controller;

import com.istoe.demo.repository.TipoPagamentoRepository;
import com.istoe.demo.response.ObjetoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipos-pagamentos")
public class TipoPagamentoController {
    private final TipoPagamentoRepository tipoPagamentoRepository;

    public TipoPagamentoController(TipoPagamentoRepository tipoPagamentoRepository) {
        this.tipoPagamentoRepository = tipoPagamentoRepository;
    }

    @GetMapping
    public List<ObjetoResponse> tipoPagamentoEnum() {
        return tipoPagamentoRepository.tipoPagamentoEnum();
    }
}
