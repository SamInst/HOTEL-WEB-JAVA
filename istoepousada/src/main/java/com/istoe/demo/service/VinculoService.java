package com.istoe.demo.service;

import com.istoe.demo.repository.EmpresaRepository;
import com.istoe.demo.repository.PessoaRepository;
import com.istoe.demo.response.Empresa;
import com.istoe.demo.response.Pessoa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VinculoService {

    private final PessoaRepository pessoaRepository;
    private final EmpresaRepository empresaRepository;

    public VinculoService(PessoaRepository pessoaRepository,
                          EmpresaRepository empresaRepository) {
        this.pessoaRepository = pessoaRepository;
        this.empresaRepository = empresaRepository;
    }

    public Optional<Pessoa> buscarPessoaComEmpresas(Long pessoaId) {
        return pessoaRepository.findById(pessoaId);
    }

    public Optional<Empresa> buscarEmpresaComPessoas(Long empresaId) {
        return empresaRepository.findById(empresaId);
    }

    public List<Pessoa> listarTodasPessoasComEmpresas() {
        return pessoaRepository.findAll();
    }

    public List<Empresa> listarTodasEmpresasComPessoas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public void criarVinculo(Long empresaId, Long pessoaId) {
        if (empresaRepository.findById(empresaId).isEmpty()) {
            throw new IllegalArgumentException("Empresa não encontrada com ID: " + empresaId);
        }
        if (pessoaRepository.findById(pessoaId).isEmpty()) {
            throw new IllegalArgumentException("Pessoa não encontrada com ID: " + pessoaId);
        }
        empresaRepository.vincularPessoas(empresaId, List.of(pessoaId));
    }

    @Transactional
    public void criarVinculos(Long empresaId, List<Long> pessoaIds) {
        if (empresaRepository.findById(empresaId).isEmpty()) {
            throw new IllegalArgumentException("Empresa não encontrada com ID: " + empresaId);
        }
        for (Long pessoaId : pessoaIds) {
            if (pessoaRepository.findById(pessoaId).isEmpty()) {
                throw new IllegalArgumentException("Pessoa não encontrada com ID: " + pessoaId);
            }
        }
        empresaRepository.vincularPessoas(empresaId, pessoaIds);
    }

    @Transactional
    public void removerVinculo(Long empresaId, Long pessoaId) {
        empresaRepository.desvincularPessoa(empresaId, pessoaId);
    }

    @Transactional
    public void removerTodosVinculosEmpresa(Long empresaId) {
        empresaRepository.desvincularTodasPessoas(empresaId);
    }

    @Transactional
    public void removerTodosVinculosPessoa(Long pessoaId) {
        pessoaRepository.removerTodosVinculosComEmpresas(pessoaId);
    }

    public boolean verificarVinculo(Long empresaId, Long pessoaId) {
        return pessoaRepository.isVinculadaComEmpresa(pessoaId, empresaId);
    }

    public List<Empresa> buscarEmpresasDaPessoa(Long pessoaId) {
        return pessoaRepository.findById(pessoaId)
                .map(Pessoa::empresasVinculadas)
                .orElse(List.of());
    }
}
