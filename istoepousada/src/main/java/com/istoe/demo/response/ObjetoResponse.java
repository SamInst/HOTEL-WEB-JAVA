package com.istoe.demo.response;

import org.springframework.jdbc.core.RowMapper;

public record ObjetoResponse(
        Long id,
        String descricao
) {
    // RowMapper como membro estático dentro do record
    public static final RowMapper<ObjetoResponse> ROW_MAPPER = (rs, rowNum) ->
            new ObjetoResponse(
                    rs.getLong("id"),
                    rs.getString("descricao") // aqui precisa ser o nome da coluna real
            );
}
