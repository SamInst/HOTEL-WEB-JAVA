package com.istoe.demo.repository;

import com.istoe.demo.request.RelatorioRequest;
import com.istoe.demo.response.FuncionarioResponse;
import com.istoe.demo.response.ObjetoResponse;
import com.istoe.demo.response.RelatorioResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RelatorioRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TipoPagamentoRepository tipoPagamentoRepository;

    public RelatorioRepository(JdbcTemplate jdbcTemplate, TipoPagamentoRepository tipoPagamentoRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.tipoPagamentoRepository = tipoPagamentoRepository;
    }

    public RelatorioResponse save(RelatorioRequest request) {
        String sql = """
                INSERT INTO relatorio (
                           data_hora,
                           fk_tipo_pagamento,
                           relatorio,
                           valor,
                           quarto_id)
                VALUES (now(), ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setObject(1, request.fkTipoPagamento());
            ps.setString(2, request.relatorio());
            ps.setObject(3, request.valor());
            ps.setObject(4, request.quartoId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findById(id);
    }

    public RelatorioResponse update(Long id, RelatorioRequest request) {
        String sql = """
                UPDATE relatorio SET
                fk_tipo_pagamento = ?,
                relatorio = ?,
                valor = ?,
                quarto_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                request.fkTipoPagamento(),
                request.relatorio(),
                request.valor(),
                request.quartoId(),
                id);
        return findById(id);
    }

    public RelatorioResponse updatePernoite(Long id, Long pernoiteId) {
        String sql = """
                UPDATE relatorio SET
                pernoite_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                pernoiteId,
                id);
        return findById(id);
    }

    public RelatorioResponse updateDayUse(Long id, Long dayUseId) {
        String sql = """
                UPDATE relatorio SET
                entrada_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                dayUseId,
                id);
        return findById(id);
    }

    public RelatorioResponse findById(Long id) {
        String sql = """
                SELECT
                r.id,
                r.data_hora,
                r.relatorio,
                r.pernoite_id,
                r.entrada_id,
                r.valor,
                r.quarto_id,
                tp.id           as tipo_pagamento_id,
                tp.descricao    as tipo_pagamento_descricao,
                f.id            as funcionario_id,
                f.nome_completo as nome_funcionario
                FROM relatorio r
                LEFT JOIN tipo_pagamento tp ON r.fk_tipo_pagamento = tp.id
                LEFT JOIN public.funcionario f on f.id = r.fk_funcionario
                WHERE r.id = ?
                """;

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new RelatorioRowMapper());
    }

    public List<RelatorioResponse> findByFilters(LocalDate dataInicio, LocalDate dataFim, Long tipoPagamentoId, Long quartoId, Long pernoiteId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                r.id,
                r.data_hora,
                r.relatorio,
                r.pernoite_id,
                r.entrada_id,
                r.valor,
                r.quarto_id,
                tp.id           as tipo_pagamento_id,
                tp.descricao    as tipo_pagamento_descricao,
                f.id            as funcionario_id,
                f.nome_completo as nome_funcionario
                FROM relatorio r
                LEFT JOIN tipo_pagamento tp ON r.fk_tipo_pagamento = tp.id
                LEFT JOIN public.funcionario f on f.id = r.fk_funcionario
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (dataInicio != null) {
            sql.append(" AND DATE(r.data_hora) >= ?");
            params.add(dataInicio);
        }

        if (dataFim != null) {
            sql.append(" AND DATE(r.data_hora) <= ?");
            params.add(dataFim);
        }

        if (tipoPagamentoId != null) {
            sql.append(" AND r.fk_tipo_pagamento = ?");
            params.add(tipoPagamentoId);
        }

        if (quartoId != null) {
            sql.append(" AND r.quarto_id = ?");
            params.add(quartoId);
        }

        if (pernoiteId != null) {
            sql.append(" AND r.pernoite_id = ?");
            params.add(pernoiteId);
        }

        sql.append(" order by id desc");

        return jdbcTemplate.query(sql.toString(), params.toArray(), new RelatorioRowMapper());
    }


    public void deleteById(Long id) {
        String sql = "DELETE FROM relatorio WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM relatorio WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private class RelatorioRowMapper implements RowMapper<RelatorioResponse> {
        @Override
        public RelatorioResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long tipoPagamentoId = rs.getLong("tipo_pagamento_id");
            ObjetoResponse tipoPagamento = tipoPagamentoRepository.findById(tipoPagamentoId);

            ObjetoResponse funcionario = new ObjetoResponse(
                    rs.getLong("funcionario_id"),
                    rs.getString("nome_funcionario")
            );

            return new RelatorioResponse(
                    rs.getLong("id"),
                    rs.getTimestamp("data_hora") != null ? rs.getTimestamp("data_hora").toLocalDateTime() : null,
                    tipoPagamento,
                    rs.getString("relatorio"),
                    (Long) rs.getObject("pernoite_id"),
                    (Long) rs.getObject("entrada_id"),
                    (Double) rs.getObject("valor"),
                    (Long) rs.getObject("quarto_id"),
                    funcionario
            );
        }
    }
}