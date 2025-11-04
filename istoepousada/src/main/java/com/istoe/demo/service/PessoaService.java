package com.istoe.demo.service;

import com.istoe.demo.repository.PessoaRepository;
import com.istoe.demo.response.Pessoa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public Optional<Pessoa> buscarPorId(Long id) {
        return pessoaRepository.findById(id);
    }

    public List<Pessoa> buscarPorNome(String nome) {
        return pessoaRepository.findByNome(nome);
    }

    public Optional<Pessoa> buscarPorCpf(String cpf) {
        return pessoaRepository.findByCpf(cpf);
    }

    public List<Pessoa> buscarPorNomeOuCpf(String termo) {
        return pessoaRepository.findByNomeOrCpf(termo);
    }

    public List<Pessoa> listarTodos() {
        return pessoaRepository.findAll();
    }

    public List<Pessoa> listarHospedados() {
        return pessoaRepository.findHospedados();
    }

    @Transactional
    public Pessoa cadastrar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public Pessoa atualizar(Pessoa pessoa) {
        if (pessoa.id() == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo para atualização");
        }
        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void marcarComoHospedado(Long id) {
        pessoaRepository.setHospedado(id, true);
    }

    @Transactional
    public void marcarComoNaoHospedado(Long id) {
        pessoaRepository.setHospedado(id, false);
    }

    @Transactional
    public void registrarHospedagem(Long id) {
        pessoaRepository.incrementarHospedagem(id);
        pessoaRepository.setHospedado(id, true);
    }

    @Transactional
    public void finalizarHospedagem(Long id) {
        pessoaRepository.setHospedado(id, false);
    }

    public boolean possuiVinculoComEmpresa(Long pessoaId) {
        return pessoaRepository.possuiVinculoComEmpresa(pessoaId);
    }

    public int contarEmpresasVinculadas(Long pessoaId) {
        return pessoaRepository.contarEmpresasVinculadas(pessoaId);
    }

    public boolean isVinculadaComEmpresa(Long pessoaId, Long empresaId) {
        return pessoaRepository.isVinculadaComEmpresa(pessoaId, empresaId);
    }

    @Transactional
    public void removerTodosVinculosComEmpresas(Long pessoaId) {
        pessoaRepository.removerTodosVinculosComEmpresas(pessoaId);
    }

    @Transactional
    public void deletar(Long id) {
        pessoaRepository.deleteById(id);
    }
}
