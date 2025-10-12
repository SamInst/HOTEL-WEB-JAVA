package com.istoe.demo.repository;

import com.istoe.demo.response.LocalidadeResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LocalidadeRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocalidadeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todos os países.
     */
    public List<LocalidadeResponse> listarPaises() {
        String sql = """
            SELECT id, descricao
            FROM public.paises
            ORDER BY descricao ASC
        """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new LocalidadeResponse(
                        rs.getLong("id"),
                        rs.getString("descricao")
                ));
    }

    /**
     * Lista os estados de um país específico.
     *
     * @param fkPais ID do país
     */
    public List<LocalidadeResponse> listarEstadosPorPais(Long fkPais) {
        String sql = """
            SELECT id, descricao
            FROM public.estados
            WHERE fk_pais = ?
            ORDER BY descricao ASC
        """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new LocalidadeResponse(
                        rs.getLong("id"),
                        rs.getString("descricao")
                ), fkPais);
    }

    /**
     * Lista os municípios de um estado específico.
     *
     * ⚠️ Observação:
     * O campo na tabela foi definido como "fk_municipio", mas ele referencia "estados(id)".
     * Se for realmente uma FK de estado, o nome ideal seria "fk_estado".
     *
     * @param fkEstado ID do estado
     */
    public List<LocalidadeResponse> listarMunicipiosPorEstado(Long fkEstado) {
        String sql = """
            SELECT id, descricao
            FROM public.municipios
            WHERE fk_municipio = ?
            ORDER BY descricao ASC
        """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new LocalidadeResponse(
                        rs.getLong("id"),
                        rs.getString("descricao")
                ), fkEstado);
    }
}
