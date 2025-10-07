package com.istoe.demo.service;

import com.istoe.demo.repository.RelatorioRepository;
import com.istoe.demo.request.RelatorioRequest;
import com.istoe.demo.response.RelatorioResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public RelatorioResponse create(RelatorioRequest request) {
        return relatorioRepository.save(request);
    }

    public RelatorioResponse update(Long id, RelatorioRequest request) {
        if (!relatorioRepository.existsById(id)) {
            throw new RuntimeException("Relatório não encontrado com ID: " + id);
        }
        return relatorioRepository.update(id, request);
    }

    public RelatorioResponse findById(Long id) {
        if (!relatorioRepository.existsById(id)) {
            throw new RuntimeException("Relatório não encontrado com ID: " + id);
        }
        return relatorioRepository.findById(id);
    }

    public List<RelatorioResponse> findByFilters(LocalDate dataInicio, LocalDate dataFim, Long tipoPagamentoId, Long quartoId, Long pernoiteId) {
        return relatorioRepository.findByFilters(dataInicio, dataFim, tipoPagamentoId, quartoId, pernoiteId);
    }

    public void delete(Long id) {
        if (!relatorioRepository.existsById(id)) {
            throw new RuntimeException("Relatório não encontrado com ID: " + id);
        }
        relatorioRepository.deleteById(id);
    }
}