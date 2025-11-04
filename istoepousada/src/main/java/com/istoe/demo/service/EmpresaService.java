package com.istoe.demo.service;

import com.istoe.demo.repository.EmpresaRepository;
import com.istoe.demo.response.Empresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Optional<Empresa> buscarPorId(Long id) {
        return empresaRepository.findById(id);
    }

    public List<Empresa> buscarPorNome(String nome) {
        return empresaRepository.findByNome(nome);
    }

    public Optional<Empresa> buscarPorCnpj(String cnpj) {
        return empresaRepository.findByCnpj(cnpj);
    }

    public List<Empresa> buscarPorNomeOuCnpj(String termo) {
        return empresaRepository.findByNomeOrCnpj(termo);
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public Empresa cadastrar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa atualizar(Empresa empresa) {
        if (empresa.id() == null) {
            throw new IllegalArgumentException("ID da empresa não pode ser nulo para atualização");
        }
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void vincularPessoa(Long empresaId, Long pessoaId) {
        empresaRepository.vincularPessoas(empresaId, List.of(pessoaId));
    }

    @Transactional
    public void vincularPessoas(Long empresaId, List<Long> pessoaIds) {
        empresaRepository.vincularPessoas(empresaId, pessoaIds);
    }

    @Transactional
    public void desvincularPessoa(Long empresaId, Long pessoaId) {
        empresaRepository.desvincularPessoa(empresaId, pessoaId);
    }

    @Transactional
    public void desvincularTodasPessoas(Long empresaId) {
        empresaRepository.desvincularTodasPessoas(empresaId);
    }

    @Transactional
    public void deletar(Long id) {
        empresaRepository.deleteById(id);
    }
}
