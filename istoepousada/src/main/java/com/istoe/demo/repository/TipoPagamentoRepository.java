package com.istoe.demo.repository;

import com.istoe.demo.response.ObjetoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TipoPagamentoRepository {
    private final JdbcTemplate jdbcTemplate;

    public TipoPagamentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ObjetoResponse> tipoPagamentoEnum() {
        return jdbcTemplate.query(
                "select id, descricao from tipo_pagamento",
                ObjetoResponse.ROW_MAPPER);
    }

    public ObjetoResponse findById(Long id) {
        return jdbcTemplate.queryForObject(
                "select id, descricao from tipo_pagamento where id = ?",
                ObjetoResponse.ROW_MAPPER, id);
    }
}
