package com.istoe.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Empresa(
        Long id,

        @JsonProperty("razao_social")
        String razaoSocial,

        @JsonProperty("nome_fantasia")
        String nomeFantasia,

        String cnpj,

        @JsonProperty("inscricao_estadual")
        String inscricaoEstadual,

        @JsonProperty("inscricao_municipal")
        String inscricaoMunicipal,

        String telefone,
        String email,
        String endereco,
        String cep,
        String numero,
        String complemento,

        @JsonProperty("fk_pais")
        Long fkPais,

        @JsonProperty("fk_estado")
        Long fkEstado,

        @JsonProperty("fk_municipio")
        Long fkMunicipio,

        String bairro,

        @JsonProperty("tipo_empresa")
        String tipoEmpresa,

        Boolean ativa,

        @JsonProperty("pessoasVinculadas")
        List<Pessoa> pessoas
) {
    public Empresa withId(Long newId) {
        return new Empresa(
                newId, razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, inscricaoMunicipal,
                telefone, email, endereco, cep, numero, complemento,
                fkPais, fkEstado, fkMunicipio, bairro, tipoEmpresa, ativa, pessoas
        );
    }

    public Empresa withPessoas(List<Pessoa> newPessoas) {
        return new Empresa(
                id, razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, inscricaoMunicipal,
                telefone, email, endereco, cep, numero, complemento,
                fkPais, fkEstado, fkMunicipio, bairro, tipoEmpresa, ativa, newPessoas
        );
    }
}