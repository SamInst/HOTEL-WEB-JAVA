package com.istoe.demo.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Pessoa(
        Long id,
        LocalDateTime dataHoraCadastro,
        String nome,
        LocalDate dataNascimento,
        String cpf,
        String rg,
        String email,
        String telefone,
        Long fkPais,
        Long fkEstado,
        Long fkMunicipio,
        String endereco,
        String complemento,
        Boolean hospedado,
        Integer vezesHospedado,
        Boolean clienteNovo,
        String cep,
        Integer idade,
        String bairro,
        Short sexo,
        String numero,
        List<Empresa> empresasVinculadas
) {
    public Pessoa {
        empresasVinculadas = empresasVinculadas != null ?
                List.copyOf(empresasVinculadas) : List.of();
    }

    // Construtor para criação (sem ID, sem empresas)
    public Pessoa(String nome, LocalDate dataNascimento, String cpf, String rg,
                  String email, String telefone, Long fkPais, Long fkEstado,
                  Long fkMunicipio, String endereco, String complemento,
                  String cep, Integer idade, String bairro, Short sexo, String numero) {
        this(null, LocalDateTime.now(), nome, dataNascimento, cpf, rg, email,
                telefone, fkPais, fkEstado, fkMunicipio, endereco, complemento,
                false, 0, true, cep, idade, bairro, sexo, numero, List.of());
    }

    public Pessoa withId(Long id) {
        return new Pessoa(id, this.dataHoraCadastro, this.nome, this.dataNascimento,
                this.cpf, this.rg, this.email, this.telefone, this.fkPais,
                this.fkEstado, this.fkMunicipio, this.endereco, this.complemento,
                this.hospedado, this.vezesHospedado, this.clienteNovo,
                this.cep, this.idade, this.bairro, this.sexo, this.numero,
                this.empresasVinculadas);
    }

    public Pessoa withEmpresas(List<Empresa> empresas) {
        return new Pessoa(this.id, this.dataHoraCadastro, this.nome, this.dataNascimento,
                this.cpf, this.rg, this.email, this.telefone, this.fkPais,
                this.fkEstado, this.fkMunicipio, this.endereco, this.complemento,
                this.hospedado, this.vezesHospedado, this.clienteNovo,
                this.cep, this.idade, this.bairro, this.sexo, this.numero, empresas);
    }

    public Pessoa comHospedado(Boolean hospedado) {
        return new Pessoa(this.id, this.dataHoraCadastro, this.nome, this.dataNascimento,
                this.cpf, this.rg, this.email, this.telefone, this.fkPais,
                this.fkEstado, this.fkMunicipio, this.endereco, this.complemento,
                hospedado, this.vezesHospedado, this.clienteNovo,
                this.cep, this.idade, this.bairro, this.sexo, this.numero,
                this.empresasVinculadas);
    }

    public Pessoa incrementarHospedagem() {
        return new Pessoa(this.id, this.dataHoraCadastro, this.nome, this.dataNascimento,
                this.cpf, this.rg, this.email, this.telefone, this.fkPais,
                this.fkEstado, this.fkMunicipio, this.endereco, this.complemento,
                this.hospedado, this.vezesHospedado + 1, false,
                this.cep, this.idade, this.bairro, this.sexo, this.numero,
                this.empresasVinculadas);
    }

    public boolean possuiEmpresas() {
        return !empresasVinculadas.isEmpty();
    }

    public int totalEmpresas() {
        return empresasVinculadas.size();
    }
}
