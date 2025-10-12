package com.istoe.demo.service;

import com.istoe.demo.repository.LocalidadeRepository;
import com.istoe.demo.response.LocalidadeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnderecoService {

    private final LocalidadeRepository localidadeRepository;

    public EnderecoService(LocalidadeRepository localidadeRepository) {
        this.localidadeRepository = localidadeRepository;
    }

    public List<LocalidadeResponse> listarPaises() {
        return localidadeRepository.listarPaises();
    }

    public List<LocalidadeResponse> listarEstados(Long fkPais) {
        return localidadeRepository.listarEstadosPorPais(fkPais);
    }

    public List<LocalidadeResponse> listarMunicipios(Long fkEstado) {
        return localidadeRepository.listarMunicipiosPorEstado(fkEstado);
    }
}

